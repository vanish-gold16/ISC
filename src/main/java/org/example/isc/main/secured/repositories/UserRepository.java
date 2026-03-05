package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            select u from User u where
            lower(u.username) like lower(concat('%', :q, '%')) or 
            lower(u.firstName) like lower(concat('%', :q, '%')) or 
            lower(u.lastName) like lower(concat('%', :q, '%'))
                        """)
    List<User> searchByQuery(@Param("q") String q);

    @Query("update User u set " +
            "u.profile.lastActivityAt = :time where lower(u.username) = lower(:username) ")
    @Modifying
    void updateLastActivityByUsername(String username, LocalDateTime time);
}
