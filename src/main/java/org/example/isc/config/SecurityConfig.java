package org.example.isc.config;

import org.example.isc.main.enums.RoleEnum;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.profile.service.ActivityService;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository repository;
    private final ActivityService activityService;

    public SecurityConfig(UserRepository repository, ActivityService activityService) {
        this.repository = repository;
        this.activityService = activityService;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests(
                        auth -> auth
                                // static
                                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico", "/favicon.png").permitAll()
                                // public
                                .requestMatchers("/", "/landing", "/auth/**", "/error").permitAll()
                                // admin
                                .requestMatchers("/admin/**").hasAnyRole(RoleEnum.ADMIN.name())
                                .requestMatchers("/ws/**").authenticated()
                                // + добавить CSRF исключение для /ws/** или использовать SockJS
                                // others only after login
                                .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/logout")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            if (authentication != null) {
                                String username = authentication.getName();
                                repository.findByUsernameIgnoreCase(username)
                                        .ifPresent(activityService::leave);
                            }
                            response.sendRedirect("/auth/login?logout");
                        })
                        .permitAll()
                )
                .formLogin(form -> form
                        // redirect to login
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .successHandler((request, response, authentication) -> {
                            Object onboardingFlag = request.getSession(false) == null
                                    ? null
                                    : request.getSession(false).getAttribute("POST_REGISTER_ONBOARDING");

                            if (Boolean.TRUE.equals(onboardingFlag)) {
                                request.getSession(false).removeAttribute("POST_REGISTER_ONBOARDING");
                                response.sendRedirect("/onboarding");
                                return;
                            }

                            response.sendRedirect("/home");
                        })
                        .failureUrl("/auth/login?error=true")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("very-secret-key")
                        .tokenValiditySeconds(60 * 60 * 24 * 30)
                        .rememberMeParameter("remember-me")
                );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return new UserDetailsService(){
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
                String principal = username == null ? "" : username.trim();

                User user = repository.findByUsernameIgnoreCase(principal)
                        .or(() -> repository.findByEmailIgnoreCase(principal))
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                Set<SimpleGrantedAuthority> roles = Collections.singleton(user.getRole().toAuthority());
                return new org.springframework.security.core.userdetails.User(
                        user.getUsername(), user.getPasswordHash(), roles
                );
            }
        };
    }



}

