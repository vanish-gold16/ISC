package org.example.isc.main.secured.repositories.scholarhub;

import org.example.isc.main.secured.models.scholarship.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, Long> {
}
