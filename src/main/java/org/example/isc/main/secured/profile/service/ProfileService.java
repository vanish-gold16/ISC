package org.example.isc.main.secured.profile.service;

import org.example.isc.main.dto.EditRequest;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.UserProfile;
import org.example.isc.main.secured.repositories.UserProfileRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserProfileRepository userProfileRepository;

    public ProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public void edit(UserRepository userRepository, Authentication authentication, EditRequest request){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));
        String username = request.getUsername();
        String email = request.getEmail();

        if(userRepository.existsByUsernameIgnoreCase(username) && !me.getUsername().trim().equals(username))
            throw new IllegalArgumentException("This username already exists!");
//        if(userRepository.existsByUsername(username) && !authentication.getName().equals(username))
//            throw new IllegalArgumentException("This username already exists!");
        if(userRepository.existsByEmailIgnoreCase(email) && !me.getEmail().trim().equals(email))
            throw new IllegalArgumentException("This email has already been used!");
//        if(userRepository.existsByEmail(email) && !me.getEmail().equals(email))
//            throw new IllegalArgumentException("This email has already been used!");

//        UserProfile newProfile = userProfileRepository.findByUserId(me.getId()).orElseThrow(
//                () -> new IllegalStateException("Profile not found!")
//        );
        UserProfile newProfile = userProfileRepository.findByUserId(me.getId()).orElse(new UserProfile());
        newProfile.setUser(me);
        newProfile.setBio(request.getBio());
        newProfile.setCountry(request.getCountry());
        newProfile.setCity(request.getCity());
        newProfile.setCurrentStudy(request.getCurrentStudy());
        newProfile.setOccupationEnum(request.getOccupationEnum());
        newProfile.setBirthDate(request.getBirthDate());

        me.setFirstName(request.getFirstName());
        me.setLastName(request.getLastName());
        me.setUsername(request.getUsername());
        me.setEmail(request.getEmail());
        me.setProfile(newProfile);
    }

}
