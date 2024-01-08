package com.example.wefly_app.service;


import com.example.wefly_app.entity.User;
import com.example.wefly_app.request.LoginModel;
import com.example.wefly_app.request.RegisterGoogleModel;
import com.example.wefly_app.request.RegisterModel;
import com.example.wefly_app.request.UpdateUserModel;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public interface UserService {
    Map registerManual(RegisterModel objModel) ;
    Map registerMerchant (User request, UUID merchantId);
    Map registerByGoogle(RegisterGoogleModel objModel);
    public Map login(LoginModel objLogin);
    Map<Object, Object> getById(Long id);
    Map<Object, Object> delete(User request);
    Map<Object, Object> update(UpdateUserModel request);
    Map<Object, Object> getIdByUserName(String username);
    Map<Object, Object> getDetailProfile(Principal principal);
}

