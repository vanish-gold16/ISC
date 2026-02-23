package org.example.isc.main.common.service;

import org.example.isc.main.dto.RegistrationRequest;
import org.example.isc.main.enums.RoleEnum;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;


    @Autowired
    public UserService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public void register(
            RegistrationRequest request
    ){
        String email = request.getEmail().trim().toLowerCase();

        if(repository.existsByEmail(email))
            throw new IllegalStateException("This email is already registered!");

        User user = new User();

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setUsername(request.getUsername().trim());
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(request.getPassword()));
        user.setRole(RoleEnum.USER);
        user.setDate(LocalDate.now());

        repository.save(user);
    }
}
