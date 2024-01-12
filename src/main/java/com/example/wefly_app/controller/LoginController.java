package com.example.wefly_app.controller;

import com.example.wefly_app.entity.Provider;
import com.example.wefly_app.entity.User;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.LoginModel;
import com.example.wefly_app.request.RegisterGoogleModel;
import com.example.wefly_app.request.RegisterModel;
import com.example.wefly_app.service.UserService;
import com.example.wefly_app.test.OAuth2Sample;
import com.example.wefly_app.util.TemplateResponse;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REDIRECT_URI;

@RestController
@RequestMapping("/v1/user-login/")
@Slf4j
public class LoginController {
    @Autowired
    public UserService serviceReq;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegisterController registerController;

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
//    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map> login(@Valid @RequestBody LoginModel objModel) {
        Map map = serviceReq.login(objModel);

        return new ResponseEntity<Map>(map, HttpStatus.OK);
    }

    @PostMapping({"/signin_google/{accessToken}", "/signin_google/{accessToken}/"})
    @ResponseBody
    public ResponseEntity<Map> repairGoogleSigninAction(@PathVariable ("accessToken") String token) throws IOException {
        if (StringUtils.isEmpty(token)) {
            return new ResponseEntity<Map>(response.error("Token is required."), HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> map123 = new HashMap<>();
        GoogleCredential credential = new GoogleCredential().setAccessToken(token);
        Oauth2 oauth2 = new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(
                "Oauth2").build();
        Userinfoplus profile;
        try {
            profile = oauth2.userinfo().get().execute();
        } catch (GoogleJsonResponseException e) {
            return new ResponseEntity<Map>(response.error(e.getDetails()), HttpStatus.BAD_GATEWAY);
        }
        profile.toPrettyString();
        String pass = "Password123";
        User user = userRepository.findOneByUsername(profile.getEmail());
        if (null != user) {
            if (!user.isEnabled()) {
                return new ResponseEntity<Map>(response.error("Your Account is disable. Please check your email for activation."), HttpStatus.OK);
            }
            String oldPassword = user.getPassword();
            if (!passwordEncoder.matches(pass, oldPassword)) {
                System.out.println("update password berhasil");
                user.setPassword(passwordEncoder.encode(pass));
                userRepository.save(user);
            }
            user.setProvider(Provider.GOOGLE);

        } else {
            RegisterGoogleModel registerModel = new RegisterGoogleModel();
            registerModel.setFullName(profile.getName());
            registerModel.setEmail(profile.getEmail());
            registerModel.setPassword(pass);
            serviceReq.registerByGoogle(registerModel);
            user = userRepository.findOneByUsername(profile.getEmail());
            log.info("register success!");
        }
        String url = AUTHURL + "?username=" + profile.getEmail() +
                "&password=" + pass +
                "&grant_type=password" +
                "&client_id=my-client-web" +
                "&client_secret=password";
        ResponseEntity<Map> response123 = restTemplateBuilder.build().exchange(url, HttpMethod.POST, null, new
                ParameterizedTypeReference<Map>() {
                });

        if (response123.getStatusCode() == HttpStatus.OK) {

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
            map123.put("user", user);
            return new ResponseEntity<Map>(response.success(map123), HttpStatus.OK);
        }
        return new ResponseEntity<Map>(map123, HttpStatus.OK);
    }


}

