package org.example.isc.main.secured.repositories.scholarhub;

import org.example.isc.main.dto.scholarship.SubjectOptionDTO;
import org.example.isc.main.secured.models.scholarship.Subject;
import org.example.isc.main.secured.models.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectsRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByUserAndNameContainingIgnoreCaseOrderByNameAsc(User user, String name);
    Optional<Subject> findByUserAndNameIgnoreCase(User user, String name);

    List<SubjectOptionDTO> findByUser(User user);


    boolean existsByUserAndFullName(User user, String fullName);

    SubjectOptionDTO findByUserAndFullNameIgnoreCase(User user, String fullName);
}
