package org.example.isc.main.secured.messenger;

import org.example.isc.main.common.service.UserService;
import org.example.isc.main.dto.messenger.MessageDTO;
import org.example.isc.main.dto.messenger.MessagePayload;
import org.example.isc.main.enums.conversation.MessageType;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.models.messenger.Message;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/messenger")
public class ChatWebSocketController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessengerService messengerService;
    private final SimpMessagingTemplate brokerMessagingTemplate;

    public ChatWebSocketController(UserService userService, UserRepository userRepository, ConversationRepository conversationRepository, MessengerService messengerService, SimpMessagingTemplate brokerMessagingTemplate) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messengerService = messengerService;
        this.brokerMessagingTemplate = brokerMessagingTemplate;
    }

    @MessageMapping("/chat/{id}")
    public void handleMessage(
            @DestinationVariable Long id,
            @Payload MessagePayload payload,
            Authentication authentication
    ){
        User user = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        Conversation conversation = conversationRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Conversation not found: " + id)
        );

        messengerService.assertMember(user, conversation);

        Message saved = messengerService.saveMessage(conversation, user, payload.getBody(), MessageType.TEXT);

        MessageDTO dto = MessageDTO.from(saved);

        brokerMessagingTemplate.convertAndSend("/topic/conversation." + conversation.getId(), dto);
    }

}
