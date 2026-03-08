package org.example.isc.main.secured.profile;

import jakarta.servlet.http.HttpServletRequest;
import org.example.isc.main.secured.profile.service.ActivityService;
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
    private final ActivityService activityService;

    public ActivityInterceptor(UserRepository userRepository, ActivityService activityService) {
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        if(request.getUserPrincipal() == null) return true;

        String username = request.getUserPrincipal().getName();
        userRepository.findByUsernameIgnoreCase(username)
                .ifPresent(activityService::touch);
        return true;
    }

}
