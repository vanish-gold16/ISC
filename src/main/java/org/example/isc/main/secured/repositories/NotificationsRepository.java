package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationsRepository extends JpaRepository<Notification, Long> {
}
