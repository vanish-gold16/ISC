package org.example.isc.opuscore.repositories;

import org.example.isc.opuscore.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserIdOrderByValueDescIdDesc(Long userId);

    List<Review> findAllByArtRequestId(Long artRequestId);
}
