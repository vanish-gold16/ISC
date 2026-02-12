package org.example.isc.main.repositories;

import org.example.isc.main.models.Post;
import org.example.isc.main.models.User;
import org.example.isc.main.models.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {



}
