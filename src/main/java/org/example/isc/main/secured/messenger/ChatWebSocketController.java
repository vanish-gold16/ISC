package org.example.isc.main.secured.messenger;

import org.example.isc.main.common.service.UserService;
import org.example.isc.main.dto.messenger.MessageDTO;
import org.example.isc.main.dto.messenger.MessagePayload;
import org.example.isc.main.enums.conversation.ConversationType;
import org.example.isc.main.enums.conversation.MessageType;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.models.messenger.Message;
import org.example.isc.main.secured.profile.service.ActivityService;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationMemberRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/messenger")
public class ChatWebSocketController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessengerService messengerService;
    private final SimpMessagingTemplate brokerMessagingTemplate;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ActivityService activityService;

    public ChatWebSocketController(UserService userService, UserRepository userRepository, ConversationRepository conversationRepository, MessengerService messengerService, SimpMessagingTemplate brokerMessagingTemplate, ConversationMemberRepository conversationMemberRepository, ActivityService activityService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messengerService = messengerService;
        this.brokerMessagingTemplate = brokerMessagingTemplate;
        this.conversationMemberRepository = conversationMemberRepository;
        this.activityService = activityService;
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

        List<User> targets = new ArrayList<>();
        User target;
        if(!conversation.getType().equals(ConversationType.DIRECT)) {
            targets = conversationMemberRepository.findOtherUsersByConversation(conversation, user);
            for (User u : targets) {
                brokerMessagingTemplate.convertAndSendToUser(
                        u.getUsername(),
                        "/queue/conversations",
                        buildConversationPayload(conversation, user, u, saved)
                );
            }
        }
        else {
            target = conversationMemberRepository.findOtherUserByConversationDirect(conversation, user);
            brokerMessagingTemplate.convertAndSendToUser(
                    target.getUsername(),
                    "/queue/conversations",
                    buildConversationPayload(conversation, user, target, saved)
            );
        }

        MessageDTO dto = MessageDTO.from(saved);

        brokerMessagingTemplate.convertAndSend("/topic/conversation." + conversation.getId(), dto);

        user.getProfile().setLastActivityAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private Map<String, Object> buildConversationPayload(Conversation conversation, User sender, User recipient, Message saved) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", conversation.getId());
        payload.put("type", conversation.getType().name());

        String name = conversation.getTitle();
        String avatar = conversation.getAvatarUrl();
        boolean online = false;
        String lastSeenAtIso = null;
        Long otherUserId = null;

        if (conversation.getType() == ConversationType.DIRECT) {
            name = sender.getFirstName() + " " + sender.getLastName();
            avatar = sender.getProfile() != null ? sender.getProfile().getAvatarUrl() : null;
            online = activityService.online(sender.getId());
            otherUserId = sender.getId();
            LocalDateTime lastSeenAt = sender.getProfile() != null ? sender.getProfile().getLastActivityAt() : null;
            lastSeenAtIso = lastSeenAt == null ? null
                    : lastSeenAt.atZone(ZoneId.systemDefault()).toInstant().toString();
        }

        if (name == null || name.isBlank()) {
            name = conversation.getType() == ConversationType.DIRECT ? "Conversation" : "Group chat";
        }

        payload.put("name", name);
        payload.put("title", conversation.getTitle());
        payload.put("avatar", avatar);
        payload.put("online", online);
        payload.put("lastSeenAtIso", lastSeenAtIso);
        payload.put("otherUserId", otherUserId);
        payload.put("friend", false);

        payload.put("lastMessage", saved.getText());
        payload.put("lastMessageAt", saved.getCreatedAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli());
        payload.put("lastMessageSenderId", sender.getId());
        payload.put("lastMessageSenderName", sender.getFirstName());
        payload.put("unread", 1);

        return payload;
    }

}
