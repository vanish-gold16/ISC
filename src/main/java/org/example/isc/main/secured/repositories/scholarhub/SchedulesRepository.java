package org.example.isc.main.secured.repositories.scholarhub;

import org.example.isc.main.secured.models.scholarship.Schedule;
import org.example.isc.main.secured.models.users.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SchedulesRepository extends JpaRepository<Schedule, Long> {
    @EntityGraph(attributePaths = {
            "days",
            "days.lessons",
            "days.lessons.subject"
    })
    Schedule findDetailedByUser(User user);


    Schedule findByUser(User user);
}
