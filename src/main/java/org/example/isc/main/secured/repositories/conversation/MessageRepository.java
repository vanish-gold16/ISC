package org.example.isc.main.secured.repositories.conversation;

import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.models.messenger.ConversationMember;
import org.example.isc.main.secured.models.messenger.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationAndDeletedAtIsNullOrderByCreatedAtDesc(Conversation c, Pageable pageable);

    Long countByConversationAndSenderIdNotAndDeletedAtIsNullAndCreatedAtAfter(Conversation conversation, Long sender_id, LocalDateTime createdAt);

    Long countByConversationNotAndDeletedAtIsNullAndCreatedAtAfter(Conversation conversation, LocalDateTime deletedAt, LocalDateTime createdAtAfter);

    @Query("""
      select count(m)
      from Message m
      where m.conversation = :conversation
        and m.deletedAt is null
        and m.sender <> :me
  """)
    Long countUnreadMessagesNoReadAt(
            @Param("conversation") Conversation conversation,
            @Param("me") User user);

    @Query("""
      select count(m)
      from Message m
      where m.conversation = :conversation
        and m.deletedAt is null
        and m.sender <> :me
        and m.createdAt > :lastReadAt
  """)
    Long countUnreadMessagesAfter(
            @Param("conversation") Conversation conversation,
            @Param("me") User user,
            @Param("lastReadAt") LocalDateTime lastReadAt);

}
