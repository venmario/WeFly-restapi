package com.example.wefly_app.config;

import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.util.TemplateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Slf4j
public class JwtFilter implements Filter {
    private TemplateResponse templateResponse;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    public JwtFilter(RestTemplateBuilder restTemplateBuilder) {
        this.templateResponse = new TemplateResponse();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            try {
//                Long userId = Long.valueOf(jwt.getClaim("id"));
                request.setAttribute("userId", jwt.getClaim("id"));
                request.setAttribute("test", jwt.getClaimAsString("user_name"));
            } catch (Exception e) {
                log.error("Error JWT filter: " + e);
                templateResponse.error("token data process error : " + e);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

}
