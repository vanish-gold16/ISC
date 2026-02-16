package org.example.isc.config;

import org.example.isc.main.enums.RoleEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
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
                                .requestMatchers("/", "/landing", "/error", "/auth/**").permitAll()
                                // admin
                                .requestMatchers("/admin/**").hasAnyRole(RoleEnum.ADMIN.name())
                                // others only after login
                                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // redirect to login
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/auth/login?logout=true")
                );
        return http.build();
    }

}

