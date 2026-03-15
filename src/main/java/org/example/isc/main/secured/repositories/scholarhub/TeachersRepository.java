package org.example.isc.main.secured.repositories.scholarhub;

import org.example.isc.main.secured.models.scholarship.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeachersRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByFullNameIgnoreCase(String fullName);
}
