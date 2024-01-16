package com.example.wefly_app.service;


import com.example.wefly_app.entity.User;
import com.example.wefly_app.request.user.*;

import java.io.IOException;
import java.util.Map;

public interface UserService {
    Map<Object, Object> registerManual(ManualRegisterModel request) ;
    Map<String, Object> loginByGoogle(LoginGoogleModel request) throws IOException;
    Map<String, Object> login(LoginModel request);
    Map<Object, Object> accountActivation(String request);
    Map<Object, Object> forgotPasswordRequest(ForgotPasswordModel request);
    Map<Object, Object> changePassword(ChangePasswordModel request);
    Map<Object, Object> checkOtpValidity(String request);
    Map<Object, Object> delete(User request);
    Map<Object, Object> update(UpdateUserModel request);
    Map<Object, Object> getById(Long id);
//    Map<Object, Object> getIdByUserName(String username);
//    Map<Object, Object> getDetailProfile(Principal principal);
}

