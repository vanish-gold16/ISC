package org.example.isc.main.secured.messenger;

import org.example.isc.main.dto.ConversationDTO;
import org.example.isc.main.enums.conversation.ConversationType;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.repositories.conversation.ConversationMemberRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessengerService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    public MessengerService(ConversationRepository conversationRepository, ConversationMemberRepository conversationMemberRepository, ConversationMemberRepository conversationMemberRepository1) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository1;
    }

    public ConversationDTO getConversations(User me){
        List<Conversation> conversations = conversationRepository.findByMember(me);
        return conversations.stream().map(c -> new ConversationDTO(
                c.getId(),
                c.getType(),
                c.getTitle(),
                c.getAvatarUrl()
        ));
    }

    public Conversation getOrCreateDirect(User me, User target){
        List<Conversation> allConversations = conversationRepository.findByMember(me);
        Conversation currentConversation;

        for (int i = 0; i < allConversations.size(); i++) {
            currentConversation = allConversations.get(i);
            if (conversationMemberRepository.existsByConversationAndUser(currentConversation, target)){
                return allConversations.get(i);
            }
        }

        currentConversation = new Conversation(
                ConversationType.DIRECT,
                null,
                null,
                me,
                LocalDateTime.now()
        );
        return currentConversation;
    }

}
