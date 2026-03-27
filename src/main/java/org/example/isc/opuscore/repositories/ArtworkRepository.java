package org.example.isc.opuscore.repositories;

import org.example.isc.opuscore.dto.ArtDropdownDTO;
import org.example.isc.opuscore.models.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    @Query("""
        select a from Artwork a 
        where lower(a.name) like lower(concat('%', :normalizedQuery, '%'))
           or lower(a.author) like lower(concat('%', :normalizedQuery, '%'))           
        order by a.name asc  
                """)
    List<Artwork> findByResolvedName(String normalizedQuery);
}
