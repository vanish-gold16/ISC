package org.example.isc.main.secured.profile.controller;

import jakarta.validation.Valid;
import org.example.isc.main.common.dto.ProfileSetupForm;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.UserProfile;
import org.example.isc.main.secured.repositories.UserProfileRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/onboarding")
public class OnboardingController {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;

    public OnboardingController(UserRepository userRepository, UserProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @GetMapping
    public String form(Model model, Authentication authentication){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Registrated user not found: " + authentication.getName()));

        UserProfile profile = me.getProfile();
        ProfileSetupForm form = new ProfileSetupForm();

        if(profile != null){
            form.setBio(profile.getBio());
            form.setCountry(profile.getCountry());
            form.setCity(profile.getCity());
            form.setCurrentStudy(profile.getCurrentStudy());
            form.setOccupationEnum(profile.getOccupationEnum());
            if(profile.getAvatarUrl() == null) form.setAvatarUrl("/images/private/profile/common-profile.png");
            else form.setAvatarUrl(profile.getAvatarUrl());
            if(profile.getCoverUrl() != null) form.setCoverUrl(profile.getCoverUrl());
            form.setBirthDate(profile.getBirthDate());
        }

        model.addAttribute("form", form);

        return "/private/onboarding";
    }

    @PostMapping
    public String submit(
            @ModelAttribute("form") @Valid ProfileSetupForm form,
            BindingResult binding,
            Authentication authentication
    ){
        if (binding.hasErrors()) return "private/onboarding";

        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Registrated user not found: " + authentication.getName()));

        UserProfile profile = me.getProfile();

        if(profile == null){
            profile = new UserProfile();
            profile.setUser(me);
            me.setProfile(profile);
        }

        form.setBio(profile.getBio());
        form.setCountry(profile.getCountry());
        form.setCity(profile.getCity());
        form.setCurrentStudy(profile.getCurrentStudy());
        form.setOccupationEnum(profile.getOccupationEnum());
        if(profile.getAvatarUrl() == null) form.setAvatarUrl("/images/private/profile/common-profile.png");
        else form.setAvatarUrl(profile.getAvatarUrl());
        if(profile.getCoverUrl() != null) form.setCoverUrl(profile.getCoverUrl());
        form.setBirthDate(profile.getBirthDate());

        userRepository.save(me);
        return "redirect:/profile";
    }

}
