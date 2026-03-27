package org.example.isc.opuscore.repositories;

import org.example.isc.opuscore.enums.ArtTypeEnum;
import org.example.isc.opuscore.enums.ReviewStatusEnum;
import org.example.isc.opuscore.models.NewArtRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NewArtRequestRepository extends JpaRepository<NewArtRequest, Long> {
    @Query("""
            select n from NewArtRequest n 
                        where lower(n.status) like lower(concat('%', :normalizedQuery, '%'))
                        order by n.createdAt asc           
                """)
    List<NewArtRequest> searchByResolvedName(String normalizedQuery);

    List<NewArtRequest> findByStatusOrderByCreatedAtAsc(ReviewStatusEnum status);
}
