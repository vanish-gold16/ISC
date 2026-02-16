package org.example.isc.main.common;

import jakarta.validation.Valid;
import org.example.isc.main.common.dto.RegistrationRequest;
import org.example.isc.main.common.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationRequest") RegistrationRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if(bindingResult.hasErrors()) return "public/auth/register";

        try{
            userService.register(request);
        }
        catch(IllegalStateException ex){
            bindingResult.rejectValue("email", "email.exists", ex.getMessage());
            return "public/auth/register";
        }

        redirectAttributes.addFlashAttribute("success", "Account created. Now sign in");
        return "redirect:/auth/login";
    }
}
