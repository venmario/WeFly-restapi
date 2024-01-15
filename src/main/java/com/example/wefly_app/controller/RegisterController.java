package com.example.wefly_app.controller;

import com.example.wefly_app.request.ManualRegisterModel;
import com.example.wefly_app.request.OtpRequestModel;
import com.example.wefly_app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/v1/user-register/")
public class RegisterController {
    @Autowired
    public UserService serviceReq;

    @PostMapping("/register-user")
    public ResponseEntity<Map> saveRegisterManual(@Valid @RequestBody ManualRegisterModel objModel) {
        return new ResponseEntity<>(serviceReq.registerManual(objModel), HttpStatus.OK);
    }

    @GetMapping("/register-confirm-otp/{token}")
    public ResponseEntity<Map> saveRegisterManual(@PathVariable(value = "token") OtpRequestModel otp) {
        return new ResponseEntity<>(serviceReq.accountActivation(otp), HttpStatus.OK);
    }

    //    @PostMapping("/send-otp")//send OTP
//    public Map sendEmailegister(
//            @Valid @RequestBody ForgotPasswordModel user) {
//        String message = "Thanks, please check your email for activation.";
//
//        if (user.getEmail() == null) return templateResponse.error("No email provided");
//        User found = userRepository.findOneByUsername(user.getEmail());
//        if (found == null) return templateResponse.error("Email not registered"); //throw new BadRequest
//
//        String template = emailTemplate.getRegisterTemplate();
//        String fullname = found.getFullName();
//        if (StringUtils.isEmpty(found.getOtp())) {
//            User search;
//            String otp;
//            do {
//                otp = SimpleStringUtils.randomString(6, true);
//                search = userRepository.findOneByOTP(otp); // need to be fixed later for performance purpose
//            } while (search != null);
//            Date dateNow = new Date();
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(dateNow);
//            calendar.add(Calendar.MINUTE, expiredToken);
//            Date expirationDate = calendar.getTime();
//
//            found.setOtp(otp);
//            found.setOtpExpiredDate(expirationDate);
//            template = template.replaceAll("\\{\\{USERNAME}}", (fullname== null ? found.getUsername() : fullname));
//            template = template.replaceAll("\\{\\{VERIFY_TOKEN}}",  otp);
//            userRepository.save(found);
//        } else {
//            template = template.replaceAll("\\{\\{USERNAME}}", (fullname== null ? found.getUsername() : fullname));
//            template = template.replaceAll("\\{\\{VERIFY_TOKEN}}",  found.getOtp());
//        }
//        emailSender.sendAsync(found.getUsername(), "Register", template);
//        return templateResponse.success(message);
//    }


}
