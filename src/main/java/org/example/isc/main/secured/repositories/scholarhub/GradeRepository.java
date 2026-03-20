package org.example.isc.main.secured.repositories.scholarhub;

import org.example.isc.main.secured.models.scholarship.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findAllBySubjectIdAndSubjectUserUsername(Long subjectId, String username);

    List<Grade> findAllBySubjectUserUsername(String username);

    Optional<Grade> findByIdAndSubjectUserUsername(Long id, String username);
}
