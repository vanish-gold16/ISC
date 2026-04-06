package org.example.isc.opuscore.repositories;

import org.example.isc.opuscore.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserIdOrderByValueDescIdDesc(Long userId);

    List<Review> findAllByArtRequestId(Long artRequestId);

    Optional<Review> findTopByUserIdAndArtworkIdOrderByIdDesc(Long userId, Long artworkId);

    Optional<Review> findTopByUserIdAndArtRequestIdOrderByIdDesc(Long userId, Long artRequestId);

    void deleteAllByArtRequestId(Long artRequestId);

    List<Review> findAllByArtworkId(Long artworkId);
}
