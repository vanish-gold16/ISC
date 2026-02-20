package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {



}
