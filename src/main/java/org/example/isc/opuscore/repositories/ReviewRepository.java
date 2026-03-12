package org.example.isc.opuscore.repositories;

import org.example.isc.opuscore.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
