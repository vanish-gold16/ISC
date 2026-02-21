package org.example.isc.main.secured.profile.service;

import org.example.isc.main.common.dto.EditRequest;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.UserProfile;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    public void edit(UserRepository userRepository, Authentication authentication, EditRequest request){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));
        String username = request.getUsername();
        String email = request.getEmail();

        if(userRepository.existsByUsername(username) && !authentication.getName().equals(username))
            throw new IllegalArgumentException("This username already exists!");
        if(userRepository.existsByEmail(email) && !me.getEmail().equals(email))
            throw new IllegalArgumentException("This email has already been used!");

        UserProfile newProfile = new UserProfile(
                me,
                request.getBio(),
                request.getCountry(),
                request.getCity(),
                request.getCurrentStudy(),
                request.getOccupationEnum(),
                request.getBirthDate()
        );

        me.setFirstName(request.getFirstName());
        me.setLastName(request.getLastName());
        me.setUsername(request.getUsername());
        me.setEmail(request.getEmail());
        me.setProfile(newProfile);
    }

}
