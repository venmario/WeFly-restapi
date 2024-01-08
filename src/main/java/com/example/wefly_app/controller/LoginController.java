package com.example.wefly_app.controller;

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
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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

    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/oauth2_sample");

    private static FileDataStoreFactory dataStoreFactory;

    private static HttpTransport httpTransport;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email");

    private static Oauth2 oauth2;
    private static GoogleClientSecrets clientSecrets;


    @PostMapping("/login")
//    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map> login(@Valid @RequestBody LoginModel objModel) {
        Map map = serviceReq.login(objModel);

        return new ResponseEntity<Map>(map, HttpStatus.OK);
    }

    @GetMapping("/signin_google")
    @ResponseBody
    public ResponseEntity<Map> repairGoogleSigninAction() throws IOException {

        Map<String, Object> map123 = new HashMap<>();
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

            Credential credential = authorize();
            oauth2 = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
                    "Oauth2").build();
        } catch (IOException e) {
            return new ResponseEntity<Map>(response.error(e.getMessage()), HttpStatus.BAD_GATEWAY);
        } catch (Throwable t) {
            return new ResponseEntity<Map>(response.error(t.getMessage()), HttpStatus.BAD_GATEWAY);
        }

        Userinfoplus profile = null;
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

        } else {
            RegisterGoogleModel registerModel = new RegisterGoogleModel();
            registerModel.setFullName(profile.getName());
            registerModel.setUsername(profile.getEmail());
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

    private static Credential authorize() throws Exception {
        // load client secrets
        clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(Objects.requireNonNull(OAuth2Sample.class.getResourceAsStream("/client_secret.json"))));
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/ "
                    + "into oauth2-cmdline-sample/src/main/resources/client_secrets.json");
            System.exit(1);
        }
        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES).build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }


}

