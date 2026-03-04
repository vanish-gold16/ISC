package org.example.isc.main.secured.repositories.conversation;

import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("""
        SELECT c FROM Conversation c 
        JOIN ConversationMember m 
        ON m.conversation = c 
        WHERE m.user = :user 
        ORDER BY c.createdAt DESC
""")
    List<Conversation> findByMember(@Param("user") User user);
}
