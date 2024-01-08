package com.example.wefly_app.controller;

import com.example.wefly_app.entity.User;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.RegisterGoogleModel;
import com.example.wefly_app.request.RegisterModel;
import com.example.wefly_app.service.UserService;
import com.example.wefly_app.util.EmailSender;
import com.example.wefly_app.util.EmailTemplate;
import com.example.wefly_app.util.SimpleStringUtils;
import com.example.wefly_app.util.TemplateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/v1/user-register/")
public class RegisterController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    public EmailSender emailSender;
    @Autowired
    public EmailTemplate emailTemplate;
//    @Autowired
//    public MerchantService merchantService;

    @Value("${expired.token.password.minute:}")//FILE_SHOW_RUL
    private int expiredToken;

    @Autowired
    public UserService serviceReq;

    @Autowired
    public SimpleStringUtils simpleStringUtils;

    @Autowired
    public TemplateResponse templateResponse;
    @PostMapping("/register-user")
    public ResponseEntity<Map> saveRegisterManual(@Valid @RequestBody RegisterModel objModel) throws RuntimeException {
        Map map = new HashMap();

        User user = userRepository.checkExistingEmail(objModel.getUsername());
        if (null != user) {
            return new ResponseEntity<Map>(templateResponse.error("Username sudah ada"), HttpStatus.OK);

        }
        map = serviceReq.registerManual(objModel);

        return new ResponseEntity<Map>(map, HttpStatus.OK);
    }

    @PostMapping("/register-google")
    public ResponseEntity<Map> saveRegisterByGoogle(@Valid @RequestBody RegisterGoogleModel objModel) throws RuntimeException {
        Map map = new HashMap();

        User user = userRepository.checkExistingEmail(objModel.getUsername());
        if (null != user) {
            return new ResponseEntity<Map>(templateResponse.error("Username sudah ada"), HttpStatus.OK);

        }
        map = serviceReq.registerByGoogle(objModel);
//        Map mapRegister =  sendEmailegister(objModel);
        return new ResponseEntity<Map>(map, HttpStatus.OK);

    }

    @PostMapping("/send-otp")//send OTP
    public Map sendEmailegister(
            @RequestBody RegisterModel user) {
        String message = "Thanks, please check your email for activation.";

        if (user.getUsername() == null) return templateResponse.error("No email provided");
        User found = userRepository.findOneByUsername(user.getUsername());
        if (found == null) return templateResponse.error("Email not registered"); //throw new BadRequest

        String template = emailTemplate.getRegisterTemplate();
        String fullname = found.getFullName();
        if (StringUtils.isEmpty(found.getOtp())) {
            User search;
            String otp;
            do {
                otp = SimpleStringUtils.randomString(6, true);
                search = userRepository.findOneByOTP(otp); // need to be fixed later for performance purpose
            } while (search != null);
            Date dateNow = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateNow);
            calendar.add(Calendar.MINUTE, expiredToken);
            Date expirationDate = calendar.getTime();

            found.setOtp(otp);
            found.setOtpExpiredDate(expirationDate);
            template = template.replaceAll("\\{\\{USERNAME}}", (fullname== null ? found.getUsername() : fullname));
            template = template.replaceAll("\\{\\{VERIFY_TOKEN}}",  otp);
            userRepository.save(found);
        } else {
            template = template.replaceAll("\\{\\{USERNAME}}", (fullname== null ? found.getUsername() : fullname));
            template = template.replaceAll("\\{\\{VERIFY_TOKEN}}",  found.getOtp());
        }
        emailSender.sendAsync(found.getUsername(), "Register", template);
        return templateResponse.success(message);
    }

    @GetMapping("/register-confirm-otp/{token}")
    public ResponseEntity<Map> saveRegisterManual(@PathVariable(value = "token") String tokenOtp) throws RuntimeException {



        User user = userRepository.findOneByOTP(tokenOtp);
        if (null == user) {
            return new ResponseEntity<Map>(templateResponse.error("OTP tidak ditemukan"), HttpStatus.OK);
        }

        if(user.isEnabled()){
            return new ResponseEntity<Map>(templateResponse.error("Account is active, go to login page"), HttpStatus.OK);
        }
        String today = simpleStringUtils.convertDateToString(new Date());

        String dateToken = simpleStringUtils.convertDateToString(user.getOtpExpiredDate());
        if(Long.parseLong(today) > Long.parseLong(dateToken)){
            return new ResponseEntity<Map>(templateResponse.error("OTP expired. Please get new OTP."), HttpStatus.OK);
        }
        //update user
        user.setEnabled(true);
        userRepository.save(user);

        return new ResponseEntity<Map>(templateResponse.success("Success, go to login page"), HttpStatus.OK);
    }


}
