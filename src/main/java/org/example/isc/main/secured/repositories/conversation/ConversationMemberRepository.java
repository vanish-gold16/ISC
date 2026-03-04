package org.example.isc.main.secured.repositories.conversation;

import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.models.messenger.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {
    Optional<ConversationMember> findByConversationAndUser(Conversation c, User u);
    boolean existsByConversationAndUser(Conversation c, User u);

    ConversationMember[] countByConversation(Conversation conversation);
}
