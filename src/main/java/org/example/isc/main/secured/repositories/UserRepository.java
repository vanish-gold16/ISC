package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
