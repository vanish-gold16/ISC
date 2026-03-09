package org.example.isc.main.secured.models.messenger;

import jakarta.persistence.*;
import org.example.isc.main.enums.conversation.ConversationRole;
import org.example.isc.main.secured.models.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_members")
public class ConversationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_role", nullable = false)
    private ConversationRole conversationRole;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    private LocalDateTime joinedDate;
    private LocalDateTime mutedUntil;

    public ConversationMember(Conversation conversation, User user, ConversationRole conversationRole, LocalDateTime joinedDate, LocalDateTime mutedUntil) {
        this.conversation = conversation;
        this.user = user;
        this.conversationRole = conversationRole;
        this.joinedDate = joinedDate;
        this.mutedUntil = mutedUntil;
    }

    public ConversationMember() {
    }

    public LocalDateTime getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(LocalDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ConversationRole getConversationRole() {
        return conversationRole;
    }

    public void setConversationRole(ConversationRole conversationRole) {
        this.conversationRole = conversationRole;
    }

    public LocalDateTime getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDateTime joinedDate) {
        this.joinedDate = joinedDate;
    }

    public LocalDateTime getMutedUntil() {
        return mutedUntil;
    }

    public void setMutedUntil(LocalDateTime mutedUntil) {
        this.mutedUntil = mutedUntil;
    }
}
