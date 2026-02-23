package org.example.isc.main.secured.profile.controller;

import jakarta.validation.Valid;
import org.example.isc.main.dto.ProfileSetupForm;
import org.example.isc.main.enums.CountryEnum;
import org.example.isc.main.enums.OccupationEnum;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.UserProfile;
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

    private static final String PROFILE_IMAGES_BASE = "/images/private/profile/";
    private static final String DEFAULT_AVATAR = "/images/private/profile/common-profile.png";

    private final UserRepository userRepository;

    public OnboardingController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
            if(profile.getAvatarUrl() == null) form.setAvatarUrl(DEFAULT_AVATAR);
            else form.setAvatarUrl(profile.getAvatarUrl());
            if(profile.getCoverUrl() != null) form.setCoverUrl(profile.getCoverUrl());
            form.setBirthDate(profile.getBirthDate());
        }

        model.addAttribute("form", form);
        addReferenceData(model);
        return "private/onboarding";
    }

    @PostMapping
    public String submit(
            @ModelAttribute("form") @Valid ProfileSetupForm form,
            BindingResult binding,
            Model model,
            Authentication authentication
    ){
        if (binding.hasErrors()) {
            binding.reject("form.invalid", "Please check the highlighted fields and try again.");
            addReferenceData(model);
            return "private/onboarding";
        }

        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Registrated user not found: " + authentication.getName()));

        UserProfile profile = me.getProfile();

        if(profile == null){
            profile = new UserProfile();
            profile.setUser(me);
            me.setProfile(profile);
        }

        profile.setBio(form.getBio());
        profile.setCountry(form.getCountry());
        profile.setCity(form.getCity());
        profile.setCurrentStudy(form.getCurrentStudy());
        profile.setOccupationEnum(form.getOccupationEnum());
        String currentAvatar = normalizeImagePath(profile.getAvatarUrl(), DEFAULT_AVATAR);
        String currentCover = normalizeImagePath(profile.getCoverUrl(), null);
        profile.setAvatarUrl(normalizeImagePath(form.getAvatarUrl(), currentAvatar));
        profile.setCoverUrl(normalizeImagePath(form.getCoverUrl(), currentCover));
        profile.setBirthDate(form.getBirthDate());

        userRepository.save(me);
        return "redirect:/profile";
    }

    private void addReferenceData(Model model) {
        model.addAttribute("countries", CountryEnum.values());
        model.addAttribute("occupations", OccupationEnum.values());
    }

    private String normalizeImagePath(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        String path = value.trim().replace('\\', '/');
        if (!path.startsWith(PROFILE_IMAGES_BASE) || path.contains("..")) {
            return fallback;
        }
        return path;
    }
}
