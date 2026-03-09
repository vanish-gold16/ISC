package org.example.isc.main.secured.repositories.conversation;

import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.models.messenger.ConversationMember;
import org.example.isc.main.secured.models.messenger.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationAndDeletedAtIsNullOrderByCreatedAtDesc(Conversation c, Pageable pageable);

    Long countByConversationAndSenderIdNotAndDeletedAtIsNullAndCreatedAtAfter(Conversation conversation, Long sender_id, LocalDateTime createdAt);

    Long countByConversationNotAndDeletedAtIsNullAndCreatedAtAfter(Conversation conversation, LocalDateTime deletedAt, LocalDateTime createdAtAfter);
}
