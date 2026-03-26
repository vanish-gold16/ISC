package org.example.isc.opuscore.repositories;

import org.example.isc.opuscore.models.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
}
