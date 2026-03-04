package org.example.isc.main.dto.messenger;

import org.example.isc.main.enums.conversation.ConversationType;

public class ConversationDTO{
    private Long id;
    private ConversationType conversationType;
    private String title;
    private String avatarURL;

    public ConversationDTO(Long id, ConversationType conversationType, String title, String avatarURL) {
        this.id = id;
        this.conversationType = conversationType;
        this.title = title;
        this.avatarURL = avatarURL;
    }

    public ConversationDTO() {
    }

    public Long getId() {
        return id;
    }

    public ConversationType getConversationType() {
        return conversationType;
    }

    public String getTitle() {
        return title;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

}
