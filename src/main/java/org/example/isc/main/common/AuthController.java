package org.example.isc.main.common;

import jakarta.validation.Valid;
import org.example.isc.main.common.dto.RegistrationRequest;
import org.example.isc.main.common.service.UserService;
import org.example.isc.main.enums.RoleEnum;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.Set;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String getLogin() {
        return "public/auth/login";
    }

    @GetMapping("/register")
    public String getRegistration(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "public/auth/register";
    }

    @GetMapping("/register/success")
    public String getRegistrationSuccess() {
        return "public/auth/register-success";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationRequest") RegistrationRequest request,
            BindingResult bindingResult) {
        if(bindingResult.hasErrors()) return "public/auth/register";

        try{
            userService.register(request);
        }
        catch(IllegalStateException ex){
            bindingResult.rejectValue("email", "email.exists", ex.getMessage());
            return "public/auth/register";
        }


        return "redirect:/auth/register/success";
    }

    // TODO
    private void forceAutoLogin(String username, String password){
        Set<SimpleGrantedAuthority> roles = Collections.singleton(
                RoleEnum.USER.toAuthority()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, password, roles
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
