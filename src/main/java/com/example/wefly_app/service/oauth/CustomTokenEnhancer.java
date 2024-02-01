package com.example.wefly_app.service.oauth;

import com.example.wefly_app.entity.User;
import com.example.wefly_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomTokenEnhancer implements TokenEnhancer {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Map<String, Object> additionalInfo = new HashMap<>();

        User user = getUser(authentication);
        additionalInfo.put("id", user.getId());
        additionalInfo.put("full_name", user.getFullName());
        additionalInfo.put("phone_number", user.getPhoneNumber());
        additionalInfo.put("date_of_birth", user.getDateOfBirth().toString());

        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }

    private User getUser(OAuth2Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}
