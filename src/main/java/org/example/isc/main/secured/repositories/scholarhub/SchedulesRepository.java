package org.example.isc.main.secured.repositories.scholarhub;

import org.example.isc.main.secured.models.scholarship.Schedule;
import org.example.isc.main.secured.models.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulesRepository extends JpaRepository<Schedule, Long> {
    Schedule findByUser(User user);
}
