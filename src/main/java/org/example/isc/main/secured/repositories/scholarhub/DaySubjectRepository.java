package org.example.isc.main.secured.repositories.scholarhub;

import org.example.isc.main.secured.models.scholarship.DaySubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DaySubjectRepository extends JpaRepository<DaySubject, Long> {
    Optional<DaySubject> findByIdAndDayScheduleUserUsername(Long id, String username);
}
