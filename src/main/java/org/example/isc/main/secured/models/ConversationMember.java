package org.example.isc.main.secured.models;

import jakarta.persistence.*;
import org.example.isc.main.enums.conversation.ConversationRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_members")
public class ConversationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation  conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_role", nullable = false)
    private ConversationRole conversationRole;

    private LocalDateTime joinedDate;
    private LocalDateTime mutedUntil;

}
