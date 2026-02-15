package org.example.isc.main.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public String getLogin(){
        return "public/auth/login";
    }

    @GetMapping("/register")
    public String getRegistration(){
        return "public/auth/register";
    }

}
