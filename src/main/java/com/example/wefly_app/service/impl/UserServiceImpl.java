package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.enums.Provider;
import com.example.wefly_app.entity.Role;
import com.example.wefly_app.entity.User;
import com.example.wefly_app.repository.RoleRepository;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.user.*;
import com.example.wefly_app.service.UserService;
import com.example.wefly_app.util.*;
import com.example.wefly_app.util.exception.IncorrectUserCredentialException;
import com.example.wefly_app.util.exception.SpringTokenServerException;
import com.example.wefly_app.util.exception.UserDisabledException;
import com.example.wefly_app.util.exception.ValidationException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
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
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.EntityExistsException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${BASEURL}")
    private String baseUrl;
    @Value("${frontend.email.activation}")
    private String activationUrl;
    @Autowired
    RoleRepository repoRole;
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    public EmailTemplate emailTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public EmailSender emailSender;

    @Autowired
    public SimpleStringUtils simpleStringUtils;

    @Value("${OTP_EXPIRED_TIME}")//FILE_SHOW_RUL
    private int expiredToken;

    @Value("${AUTHURL}")//FILE_SHOW_RUL
    private String AUTHURL;

    @Autowired
    public TemplateResponse templateResponse;

    @Autowired
    public PasswordValidatorUtil passwordValidatorUtil = new PasswordValidatorUtil();


    @Override
    public Map<String, Object> login(LoginModel loginModel) {
        log.info("login");
        try {
            User checkUser = userRepository.findOneByUsername(loginModel.getEmail());
            if (checkUser == null) {
                throw new IncorrectUserCredentialException("Login credential don't match an account in our system");
            }
            if (encoder.matches(loginModel.getPassword(), checkUser.getPassword())) {
                if (!checkUser.isEnabled()) {
                    throw new UserDisabledException("User is disabled, please check your email to activate your account");
                }
            }
            if (!(encoder.matches(loginModel.getPassword(), checkUser.getPassword()))) {
                throw new IncorrectUserCredentialException("Login credential don't match an account in our system");
            }
            checkUser.setProvider(Provider.LOCAL);
            userRepository.save(checkUser);

            return getToken(loginModel.getEmail(), loginModel.getPassword());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    @Transactional
    @Override
    public Map<Object, Object> registerManual (ManualRegisterModel request) {
        log.info("Register Manual");
        try {
            User checkExistingUsername = userRepository.checkExistingUsername(request.getEmail());
            if (null != checkExistingUsername) {
                log.error("Error registerManual = Email already registered");
                throw new EntityExistsException("Email already registered");
            }
            String[] roleNames = {"ROLE_USER", "ROLE_USER_O", "ROLE_USER_OD"}; // admin
            User user = new User();
            user.setUsername(request.getEmail().toLowerCase());
            user.setFullName(request.getFullName());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setDateOfBirth(request.getDateOfBirth());

            if (passwordValidatorUtil.passwordValidation(request.getPassword())) {
                throw new ValidationException(passwordValidatorUtil.getMessage());
            }
            String password = encoder.encode(request.getPassword().replaceAll("\\s+", ""));
            List<Role> r = repoRole.findByNameIn(roleNames);
            user.setRoles(r);
            user.setPassword(password);

            String template = emailTemplate.getRegisterTemplate();
            String fullname = user.getFullName();
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

            user.setOtp(otp);
            user.setOtpExpiredDate(expirationDate);
            template = template.replaceAll("\\{\\{USERNAME}}", (fullname== null ? user.getUsername() : fullname));
            template = template.replaceAll("\\{\\{VERIFY_TOKEN}}",  activationUrl + otp);
            emailSender.sendAsync(user.getUsername(), "Register", template);
            userRepository.save(user);

            log.info("register success!");
            return templateResponse.success("Please check email for activation");
        } catch (Exception e) {
            log.error("Error registerManual = ", e);
            throw e;
        }

    }

    @Transactional
    @Override
    public Map<Object, Object> accountActivation(String request) {
        log.info("Account Activation");
        if (request == null) {
            throw new ValidationException("OTP is required");
        }
        User user = userRepository.findOneByOTP(request);
        if (user == null) {
            log.error("Error account activation = OTP is not valid");
            throw new ValidationException("OTP is not valid");
        }
        if(user.isEnabled()){
            log.error("Error account activation = Account is active, go to login page");
            throw new ValidationException("Account is active, go to login page");
        }
        String today = simpleStringUtils.convertDateToString(new Date());

        String dateToken = simpleStringUtils.convertDateToString(user.getOtpExpiredDate());
        if(Long.parseLong(today) > Long.parseLong(dateToken)){
            log.error("Error account activation = OTP expired");
            throw new ValidationException("OTP expired. Please get new OTP.");
        }

        user.setEnabled(true);
        user.setOtp(null);
        user.setOtpExpiredDate(null);
        user.setUpdatedDate(new Date());
        userRepository.save(user);
        log.info("Account Activation Success");
        return templateResponse.success("Account Activation Success");
    }

    @Override
    public Map<String, Object> loginByGoogle(String request) throws IOException {
        log.info("Login By Google");
        try {
            GoogleCredential credential = new GoogleCredential().setAccessToken(request);
            Oauth2 oauth2 = new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(
                    "Oauth2").build();
            Userinfoplus profile;
            profile = oauth2.userinfo().get().execute();
            profile.toPrettyString();
            String pass = "Password123";
            User user = userRepository.findOneByUsername(profile.getEmail());
            if (user == null) {
                RegisterGoogleModel registerModel = new RegisterGoogleModel();
                registerModel.setEmail(profile.getEmail());
                registerModel.setFullName(profile.getName());
                registerModel.setPassword(pass);
                registerByGoogle(registerModel);
                log.info("register by google success!");
            } else {
                if (!user.isEnabled()) {
                    throw new UserDisabledException("User is disabled, please check your email to activate your account");
                }
                String oldPassword = user.getPassword();
                if (!passwordEncoder.matches(pass, oldPassword)) {
                    log.info("update password success!");
                    user.setPassword(passwordEncoder.encode(pass));
                }
                user.setProvider(Provider.GOOGLE);
                userRepository.save(user);
            }
            log.info("get : " + profile.getEmail());

            return getToken(profile.getEmail(), pass);
        } catch (Exception e) {
            log.error("Error loginByGoogle = ", e);
            throw e;
        }
    }

    @Transactional
    public void registerByGoogle(RegisterGoogleModel objModel) {
        log.info("Register By Google");
        try {
            String[] roleNames = {"ROLE_USER", "ROLE_USER_O", "ROLE_USER_OD"};
            User user = new User();
            user.setUsername(objModel.getEmail().toLowerCase());
            user.setFullName(objModel.getFullName());
            user.setEnabled(true);

            String password = encoder.encode(objModel.getPassword().replaceAll("\\s+", ""));
            List<Role> r = repoRole.findByNameIn(roleNames);
            user.setRoles(r);
            user.setPassword(password);
            userRepository.save(user);

            log.info("register Google success!");
        } catch (Exception e) {
            log.error("Error register with google=", e);
            throw e;
        }
    }

    @Transactional
    @Override
    public Map<Object, Object> forgotPasswordRequest(ForgotPasswordModel request) {
        log.info("Forgot Password OTP Request");
        User checkUser = userRepository.findOneByUsername(request.getEmail());
        if (checkUser == null){
            throw new IncorrectUserCredentialException("User credential don't match an account in our system");
        }

        String template = emailTemplate.getResetPassword();
        if (checkUser.getOtp() == null) {
            User search;
            String otp;
            do {
                otp = SimpleStringUtils.randomString(4, true);
                search = userRepository.findOneByOTP(otp);
            } while (search != null);
            Date dateNow = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateNow);
            calendar.add(Calendar.MINUTE, expiredToken);
            Date expirationDate = calendar.getTime();

            checkUser.setOtp(otp);
            checkUser.setOtpExpiredDate(expirationDate);
            template = template.replaceAll("\\{\\{PASS_TOKEN}}", otp);
            template = template.replaceAll("\\{\\{USERNAME}}", (checkUser.getUsername() == null ? "UserName"
                    :
                    checkUser.getUsername()));

            userRepository.save(checkUser);
        } else {
            template = template.replaceAll("\\{\\{USERNAME}}", (checkUser.getUsername() == null ? "" +
                    "UserName"
                    :
                    checkUser.getUsername()));
            template = template.replaceAll("\\{\\{PASS_TOKEN}}", checkUser.getOtp());
        }
        emailSender.sendAsync(checkUser.getUsername(), "WeFly - Forget Password", template);
        log.info("Forgot Password OTP Request Success");
        return templateResponse.success("Please check email for reset password");
    }

    @Transactional
    @Override
    public Map<Object, Object> changePassword(ChangePasswordModel request) {
        log.info("Change Password");
        try {
            User user = userRepository.findOneByOTP(request.getOtp());
            if (user == null) {
                log.error("Error change password = OTP is not valid");
                throw new ValidationException("OTP is not valid");
            }

            if (passwordValidatorUtil.passwordValidation(request.getNewPassword())) {
                log.error("Error change password = password not meet criteria");
                throw new ValidationException(passwordValidatorUtil.getMessage());
            }
            if (!request.getNewPassword().matches(request.getConfirmPassword())) {
                log.error("Error change password = Confirm Password not Match");
                throw new ValidationException("Confirm Password not Match");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword().replaceAll("\\s+", "")));
            user.setOtpExpiredDate(null);
            user.setOtp(null);
            user.setUpdatedDate(new Date());

            userRepository.save(user);
            log.info("change password success");
            return templateResponse.success("Reset Password Succeed");
        } catch (Exception e) {
            log.error("Error change password = ", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> checkOtpValidity(String request) {
        try {
            log.info("Check OTP Validity");
            if (request == null) throw new ValidationException("OTP is required");
            User user = userRepository.findOneByOTP(request);
            if (user == null) throw new ValidationException("OTP is not valid");

            log.info("Check OTP Validity Success");
            return templateResponse.success("OTP is valid");
        } catch (Exception e) {
            log.error("Check Token Validity Error: " + e.getMessage());
            return templateResponse.error("Check Token Validity Error: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public Map<Object, Object> update(UpdateUserModel request) {
        log.info("Update User");
        try {
            ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attribute.getRequest().getAttribute("userId");
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            log.info("Update User : " + userId);
            if (!checkDataDBUser.isPresent()) {
                log.error("Update User Error: unidentified, user not found");
                throw new IncorrectUserCredentialException("unidentified token user");
            }
            int count = 0;
            if (request.getFullName() != null && !request.getFullName().isEmpty() ) {
                checkDataDBUser.get().setFullName(request.getFullName());
                count++;
            }
            if (request.getCity() != null && !request.getCity().isEmpty()) {
                checkDataDBUser.get().setCity(request.getCity());
                count++;
            }
            if (request.getDateOfBirth() != null && !request.getDateOfBirth().toString().isEmpty()) {
                checkDataDBUser.get().setDateOfBirth(request.getDateOfBirth());
                count++;
            }
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
                checkDataDBUser.get().setPhoneNumber(request.getPhoneNumber());
                count++;
            }
            if (request.getGender() != null && !request.getGender().isEmpty()) {
                checkDataDBUser.get().setGender(request.getGender());
                count++;
            }
            if (count > 0) {
                checkDataDBUser.get().setUpdatedDate(new Date());
                log.info("User Updated");
                return templateResponse.success(userRepository.save(checkDataDBUser.get()));
            } else {
                log.info("Update User : no data updated");
                return templateResponse.success("no data updated");
            }

        } catch (Exception e) {
            log.error("Update User Error: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    @Override
    public Map<Object, Object> delete(User request) {
        try {
            log.info("Delete User");
            ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attribute.getRequest().getAttribute("userId");
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            if (!checkDataDBUser.isPresent()) {
                log.error("Delete User Error: unidentified, user not found");
                throw new IncorrectUserCredentialException("unidentified token user");
            }
            checkDataDBUser.get().setDeletedDate(new Date());

            log.info("User Deleted");
            return templateResponse.success(userRepository.save(checkDataDBUser.get()));
        } catch (Exception e) {
            log.error("Delete User Error: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Map<Object, Object> getByIdUser() {
        try {
            log.info("Get User");
            ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attribute.getRequest().getAttribute("userId");
            System.out.println("userId : " + userId);
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            if (!checkDataDBUser.isPresent()) {
                log.error("User Error: unidentified, user not found");
                throw new IncorrectUserCredentialException("unidentified token user");
            }

            log.info("User Found");
            return templateResponse.success(checkDataDBUser.get());
        } catch (Exception e) {
            log.error("Get User Error: " + e.getMessage());
            return templateResponse.error("Get User: " + e.getMessage());
        }
    }

    public Map<String, Object> getToken(String email, String password) {
        log.info("Request token to spring server");
        String url = AUTHURL + "?username=" + email +
                "&password=" + password +
                "&grant_type=password" +
                "&client_id=my-client-web" +
                "&client_secret=password";
        log.info("URL : " + url);
        ResponseEntity<Map<String, Object>> response123 = restTemplateBuilder.build().exchange(url,
                HttpMethod.POST, null, new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> response = new HashMap<>();
        log.info("Response token from spring server");
        if (response123.getStatusCode() == HttpStatus.OK) {
            //save token
//                checkUser.setAccessToken(response.getBody().get("access_token").toString());
//                checkUser.setRefreshToken(response.getBody().get("refresh_token").toString());
//                userRepository.save(checkUser);
            response.put("access_token", Objects.requireNonNull(response123.getBody()).get("access_token"));
            response.put("token_type", response123.getBody().get("token_type"));
            response.put("refresh_token", response123.getBody().get("refresh_token"));
            response.put("expires_in", response123.getBody().get("expires_in"));
            response.put("scope", response123.getBody().get("scope"));
            response.put("jti", response123.getBody().get("jti"));
            response.put("status", 200);
            response.put("message", "success");
            response.put("type", "login");
            log.info("Request Token Succeed");
            return response;
        } else {
            log.error("Error while getting token from server");
            throw new SpringTokenServerException("Error while getting token from server");
        }
    }

}

