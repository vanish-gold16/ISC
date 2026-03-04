package org.example.isc.main.dto.messenger;

import org.example.isc.main.secured.models.messenger.Message;

import java.time.LocalDateTime;

public class MessageDTO {

    private Long id;
    private String body;
    private String senderName;
    private String senderAvatarUrl;
    private LocalDateTime createdAt;
    private boolean fromMe;

    public static MessageDTO from(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.id = message.getId();
        dto.body = message.getText();
        dto.senderName = message.getSender().getFirstName();
        dto.createdAt = message.getCreatedAt();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
    }

    public void setSenderAvatarUrl(String senderAvatarUrl) {
        this.senderAvatarUrl = senderAvatarUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isFromMe() {
        return fromMe;
    }

    public void setFromMe(boolean fromMe) {
        this.fromMe = fromMe;
    }
}
