package org.example.isc.config;

import org.example.isc.main.enums.RoleEnum;
import org.example.isc.main.secured.models.User;
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

    public SecurityConfig(UserRepository repository) {
        this.repository = repository;
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
                                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                                // public
                                .requestMatchers("/", "/landing", "/auth/**", "/error").permitAll()
                                // admin
                                .requestMatchers("/admin/**").hasAnyRole(RoleEnum.ADMIN.name())
                                // others only after login
                                .anyRequest().authenticated()
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

