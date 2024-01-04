package com.example.wefly_app.controller;

import com.example.wefly_app.entity.User;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.GoogleReq;
import com.example.wefly_app.request.LoginModel;
import com.example.wefly_app.service.UserService;
import com.example.wefly_app.util.TemplateResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/user-login/")
@Slf4j
public class LoginController {
    @Autowired
    public UserService serviceReq;
    @Autowired
    private UserRepository userRepository;

    @Value("${expired.token.password.minute:}")//FILE_SHOW_RUL
    private int expiredToken;

    @Autowired
    public TemplateResponse response;

    @Value("${BASEURL:}")//FILE_SHOW_RUL
    private String BASEURL;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${AUTHURL:}")//FILE_SHOW_RUL
    private String AUTHURL;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Value("${APPNAME:}")//FILE_SHOW_RUL
    private String APPNAME;


    @PostMapping("/login")
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map> login(@Valid @RequestBody LoginModel objModel) {
        Map map = serviceReq.login(objModel);

        return new ResponseEntity<Map>(map, HttpStatus.OK);
    }

    @PostMapping("/signin_google")
    @ResponseBody
    public ResponseEntity<Map> repairGoogleSigninAction(@RequestBody GoogleReq parameters) throws IOException {

        Map<String, Object> map123 = new HashMap<>();
        if (StringUtils.isEmpty(parameters.getAccessToken())) {
            return new ResponseEntity<Map>(response.error("Token is required."), HttpStatus.OK);
        }
        String accessToken = parameters.getAccessToken();

        // step 1 : set token : google akan memvalidasi token yang kita kirim
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        System.out.println("access_token user=" + accessToken);

        // step 2 : get informasi akn dikonversi bentuk objek
        Oauth2 oauth2 = new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(
                "Oauth2").build();

        // step 3 : oauth2 akan diolah oleh Userinfoplus : goodle (DTO)
        Userinfoplus profile = null;
        try {
            //get information dari token google
            profile = oauth2.userinfo().get().execute();
        } catch (GoogleJsonResponseException e) {
            return new ResponseEntity<Map>(response.error(e.getDetails()), HttpStatus.BAD_GATEWAY);
        }
        profile.toPrettyString();

        // step 4 : kita hanya email, full name  dari DTO Userinfoplus dan kita chek ke db, untuk validasi
        User user = userRepository.findOneByUsername(profile.getEmail());
        if (null != user) {
            if (!user.isEnabled()) {
                return new ResponseEntity<Map>(response.error("Your Account is disable. Please check your email for activation."), HttpStatus.OK);
            }
            String oldPassword = user.getPassword();
            String pass = "Password123";
            if (!passwordEncoder.matches(pass, oldPassword)) {
                System.out.println("update password berhasil");
                user.setPassword(passwordEncoder.encode(pass));
                userRepository.save(user);
            }
            //step 5 : login seperti biasa
            String url = AUTHURL + "?username=" + profile.getEmail() +
                    "&password=" + pass +
                    "&grant_type=password" +
                    "&client_id=my-client-web" +
                    "&client_secret=password";
            ResponseEntity<Map> response123 = restTemplateBuilder.build().exchange(url, HttpMethod.POST, null, new
                    ParameterizedTypeReference<Map>() {});

            if (response123.getStatusCode() == HttpStatus.OK) {
                userRepository.save(user);

                map123.put("access_token", response123.getBody().get("access_token"));
                map123.put("token_type", response123.getBody().get("token_type"));
                map123.put("refresh_token", response123.getBody().get("refresh_token"));
                map123.put("expires_in", response123.getBody().get("expires_in"));
                map123.put("scope", response123.getBody().get("scope"));
                map123.put("jti", response123.getBody().get("jti"));
                map123.put("status", 200);
                map123.put("message", "success");
                map123.put("type", "login");
                System.out.println("masuk 3");
                //update old password : wajib
//                user.setPassword(oldPassword);
                User datUser = userRepository.save(user);
                map123.put("user", datUser);
                return new ResponseEntity<Map>(response.success(map123), HttpStatus.OK);

            }
        } else {
            return new ResponseEntity<Map>(response.error("User is not registered please contact admin"), HttpStatus.OK);
        }
        return new ResponseEntity<Map>(map123, HttpStatus.OK);
    }


}

