package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {



}
