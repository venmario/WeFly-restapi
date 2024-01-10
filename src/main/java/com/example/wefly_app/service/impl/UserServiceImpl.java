package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.Role;
import com.example.wefly_app.entity.User;
import com.example.wefly_app.repository.RoleRepository;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.LoginModel;
import com.example.wefly_app.request.RegisterGoogleModel;
import com.example.wefly_app.request.RegisterModel;
import com.example.wefly_app.request.UpdateUserModel;
import com.example.wefly_app.service.UserService;
import com.example.wefly_app.service.oauth.Oauth2UserDetailsService;
import com.example.wefly_app.util.PasswordValidatorUtil;
import com.example.wefly_app.util.TemplateResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Value("${BASEURL}")
    private String baseUrl;
    @Autowired
    RoleRepository repoRole;
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    @Autowired
    UserRepository repoUser;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    public TemplateResponse templateResponse;
    @Autowired
    public PasswordValidatorUtil passwordValidatorUtil = new PasswordValidatorUtil();
    @Autowired
    private Oauth2UserDetailsService userDetailsService;


    @Override
    public Map login(LoginModel loginModel) {
        try {
            Map<String, Object> map = new HashMap<>();

            User checkUser = userRepository.findOneByUsername(loginModel.getUsername());

            if ((checkUser != null) && (encoder.matches(loginModel.getPassword(), checkUser.getPassword()))) {
                if (!checkUser.isEnabled()) {
                    return templateResponse.error("User is not enable, check your email");
                }
            }
            if (checkUser == null) {
                return templateResponse.error("Incorrect ");
            }
            if (!(encoder.matches(loginModel.getPassword(), checkUser.getPassword()))) {
                return templateResponse.error("wrong password");
            }
            String url = baseUrl + "/oauth/token?username=" + loginModel.getUsername() +
                    "&password=" + loginModel.getPassword() +
                    "&grant_type=password" +
                    "&client_id=my-client-web" +
                    "&client_secret=password";
            ResponseEntity<Map> response = restTemplateBuilder.build().exchange(url, HttpMethod.POST, null, new
                    ParameterizedTypeReference<Map>() {
                    });

            if (response.getStatusCode() == HttpStatus.OK) {
                User user = userRepository.findOneByUsername(loginModel.getUsername());
                List<String> roles = new ArrayList<>();

                for (Role role : user.getRoles()) {
                    roles.add(role.getName());
                }
                //save token
//                checkUser.setAccessToken(response.getBody().get("access_token").toString());
//                checkUser.setRefreshToken(response.getBody().get("refresh_token").toString());
//                userRepository.save(checkUser);

                map.put("access_token", response.getBody().get("access_token"));
                map.put("token_type", response.getBody().get("token_type"));
                map.put("refresh_token", response.getBody().get("refresh_token"));
                map.put("expires_in", response.getBody().get("expires_in"));
                map.put("scope", response.getBody().get("scope"));
                map.put("jti", response.getBody().get("jti"));
                map.put("message","Success");
                map.put("code",200);

                return map;
            } else {
                return templateResponse.error("user not found");
            }
        } catch (HttpStatusCodeException e) {
            e.printStackTrace();
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return templateResponse.error("invalid login : " + e.getMessage());
            }
            return templateResponse.error(e);
        } catch (Exception e) {
            e.printStackTrace();

            return templateResponse.error(e);
        }
    }

    @Override
    public Map registerManual (RegisterModel objModel) {
        Map map = new HashMap();
        try {
            String[] roleNames = {"ROLE_USER", "ROLE_USER_O", "ROLE_USER_OD"}; // admin
            User user = new User();
            user.setUsername(objModel.getUsername().toLowerCase());
            user.setFullName(objModel.getFullName());
            user.setPhoneNumber(objModel.getPhoneNumber());

//            if (objModel.getPassword().isEmpty()) return templateResponse.error("Password is required");
            if (!passwordValidatorUtil.validatePassword(objModel.getPassword())) {
                return templateResponse.error(passwordValidatorUtil.getMessage());
            }
            String password = encoder.encode(objModel.getPassword().replaceAll("\\s+", ""));
            List<Role> r = repoRole.findByNameIn(roleNames);
            user.setRoles(r);
            user.setPassword(password);
            User obj = repoUser.save(user);

            return templateResponse.success(obj);

        } catch (Exception e) {
            logger.error("Eror registerManual=", e);
            return templateResponse.error("eror:"+e);
        }

    }

    @Override
    public Map registerByGoogle(RegisterGoogleModel objModel) {
        Map map = new HashMap();
        try {
            String[] roleNames = {"ROLE_USER", "ROLE_USER_O", "ROLE_USER_OD"};
            User user = new User();
            user.setUsername(objModel.getUsername().toLowerCase());
            user.setFullName(objModel.getFullName());
            user.setEnabled(true);

            String password = encoder.encode(objModel.getPassword().replaceAll("\\s+", ""));
            List<Role> r = repoRole.findByNameIn(roleNames);
            user.setRoles(r);
            user.setPassword(password);
            User obj = repoUser.save(user);
            return templateResponse.success(obj);

        } catch (Exception e) {
            logger.error("Error register with google=", e);
            return templateResponse.error("error:"+e);
        }
    }

    @Transactional
    @Override
    public Map<Object, Object> update(UpdateUserModel request) {
        try {
            log.info("Update User");
            ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attribute.getRequest().getAttribute("userId");
            System.out.println("userID = " + userId);
            System.out.println("user name = " + attribute.getRequest().getAttribute("test"));
            if (userId == null) return templateResponse.error("user id null");
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            if (!checkDataDBUser.isPresent()) return templateResponse.error("unidentified token user");
            if (!request.getFullName().isEmpty()) checkDataDBUser.get().setFullName(request.getFullName());
            if (!request.getCity().isEmpty()) checkDataDBUser.get().setCity(request.getCity());
            if (request.getDateOfBirth() != null) checkDataDBUser.get().setDateOfBirth(request.getDateOfBirth());

            log.info("Update User Success");
            return templateResponse.success(userRepository.save(checkDataDBUser.get()));
        } catch (Exception e) {
            log.error("Update User Error: " + e.getMessage());
            return templateResponse.error("Update User: " + e.getMessage());
        }
    }

    @Override
    public Map<Object, Object> delete(User request) {
        try {
            log.info("Delete User");
            ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attribute.getRequest().getAttribute("userId");
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            if (!checkDataDBUser.isPresent()) return templateResponse.error("unidentified token user");

            log.info("User Deleted");
            checkDataDBUser.get().setDeletedDate(new Date());
            return templateResponse.success(userRepository.save(checkDataDBUser.get()));
        } catch (Exception e) {
            log.error("Delete User Error: " + e.getMessage());
            return templateResponse.error("Delete User : " + e.getMessage());
        }
    }

    @Override
    public Map<Object, Object> getById(Long id) {
        try {
            log.info("Get User");
            if (id == null) return templateResponse.error("Id is required");
            Optional<User> checkDataDBUser = userRepository.findById(id);
            if (!checkDataDBUser.isPresent()) return templateResponse.error("User not Found");

            log.info("User Found");
            return templateResponse.success(checkDataDBUser.get());
        } catch (Exception e) {
            log.error("Get User Error: " + e.getMessage());
            return templateResponse.error("Get User: " + e.getMessage());
        }
    }

    @Override
    public Map getDetailProfile(Principal principal) {
        User idUser = getUserIdToken(principal, userDetailsService);
        try {
            User obj = userRepository.save(idUser);
            return templateResponse.success(obj);
        } catch (Exception e){
            return templateResponse.error(e);
        }
    }

    private User getUserIdToken(Principal principal, Oauth2UserDetailsService userDetailsService) {
        UserDetails user = null;
        String username = principal.getName();
        if (!StringUtils.isEmpty(username)) {
            user = userDetailsService.loadUserByUsername(username);
        }

        if (null == user) {
            throw new UsernameNotFoundException("User not found");
        }
        User idUser = userRepository.findOneByUsername(user.getUsername());
        if (null == idUser) {
            throw new UsernameNotFoundException("User name not found");
        }
        return idUser;
    }

    @Override
    public Map<Object, Object> getIdByUserName(String username) {
        try {
            log.info("Get Id");
            User user = userRepository.findOneByUsername(username);
            if (user == null) return templateResponse.error("User not found");
            return templateResponse.success(user);
        } catch (Exception e) {
            return templateResponse.error(e);
        }
    }

}

