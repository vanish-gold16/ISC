package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
}
