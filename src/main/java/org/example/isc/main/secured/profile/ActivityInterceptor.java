package org.example.isc.main.secured.profile;

import jakarta.servlet.http.HttpServletRequest;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Component
public class ActivityInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public ActivityInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())){
            String username = authentication.getName();
            userRepository.updateLastActivityByUsername(username, LocalDateTime.now());
        }
        return true;
    }

}
