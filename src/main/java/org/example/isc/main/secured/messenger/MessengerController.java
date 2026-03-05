package org.example.isc.main.secured.messenger;

import org.example.isc.main.enums.conversation.ConversationType;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.models.messenger.Message;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationMemberRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationRepository;
import org.example.isc.main.secured.repositories.conversation.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping({"/messenger", "/messages"})
public class MessengerController {

    private static final String DEFAULT_AVATAR = "/images/private/profile/common-profile.png";

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final MessageRepository messageRepository;
    private final MessengerService messengerService;

    public MessengerController(UserRepository userRepository, ConversationRepository conversationRepository, ConversationMemberRepository conversationMemberRepository, MessageRepository messageRepository, MessengerService messengerService) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.messageRepository = messageRepository;
        this.messengerService = messengerService;
    }

    //  1. Отправка сообщений через HTTP или WebSocket
    //     Сейчас кнопка “Send” в шаблоне не отправляет.
    //     Варианты:
    //      - сделать POST /messages/c/{id}/send (REST)
    //      - или подключить STOMP/WebSocket в JS и слать в /app/chat/{id}
    //  2. Unread-логика
    //     Сейчас unread = 0 везде. Нужно хранить read/delivered:
    //      - таблица message_status или поле last_read_at в conversation_members.
    //  3. Последнее сообщение и время
    //     Сейчас мы делаем запрос на каждую беседу (N+1). Лучше сделать репозиторий/SQL, который отдаёт список чатов с последним сообщением в одном запросе.
    //  4. Безопасность/доступ
    //     В UI проверка есть, но стоит ещё:
    //      - запрет на доступ к /messages/c/{id}, если не участник.
    //  5. Онлайн/статус
    //     Сейчас всегда false. Нужны presence-данные или хотя бы timestamp последней активности.

    @GetMapping()
    public String messengerPage(
            Authentication authentication,
            Model model
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        populateModel(model, me, null);
        return "/private/messenger";
    }

    @GetMapping("/{id}")
    public String userPage(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
        Conversation conversation = conversationRepository.findById(
                messengerService.getOrCreateDirect(me, target).getId()
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        return "redirect:/messages/c/" + conversation.getId();
    }

    @GetMapping("/c/{id}")
    public String conversationPage(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found: " + id));
        if (!conversationMemberRepository.existsByConversationAndUser(conversation, me)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found: " + id);
        }
        populateModel(model, me, conversation);
        return "/private/messenger";
    }

    private void populateModel(Model model, User me, Conversation activeConversation) {
        model.addAttribute("title", "Messenger");
        List<Conversation> conversations = conversationRepository.findByMember(me);

        List<Map<String, Object>> conversationViews = new ArrayList<>();
        for (Conversation conversation : conversations) {
            conversationViews.add(buildConversationView(conversation, me));
        }

        Map<String, Object> activeConversationView = activeConversation == null ? null : buildActiveConversationView(activeConversation, me);
        List<Map<String, Object>> messages = activeConversation == null ? Collections.emptyList() : buildMessageViews(activeConversation, me);

        model.addAttribute("conversations", conversationViews);
        model.addAttribute("activeConversation", activeConversationView);
        model.addAttribute("messages", messages);
        model.addAttribute("activeConversationId", activeConversation == null ? null : activeConversation.getId());
        model.addAttribute("currentUserId", me.getId());
    }

    private Map<String, Object> buildConversationView(Conversation conversation, User me) {
        User other = null;
        String name = conversation.getTitle();
        String avatar = conversation.getAvatarUrl();
        String subtitle = "";

        if (conversation.getType() == ConversationType.DIRECT) {
            other = conversationMemberRepository.findOtherUserByConversationDirect(conversation, me);
            if (other != null) {
                name = other.getFirstName() + " " + other.getLastName();
                subtitle = "@" + other.getUsername();
                if (other.getProfile() != null && other.getProfile().getAvatarUrl() != null) {
                    avatar = other.getProfile().getAvatarUrl();
                }
            }
        }

        if (name == null || name.isBlank()) {
            name = conversation.getType() == ConversationType.GROUP ? "Group chat" : "Conversation";
        }
        if (avatar == null || avatar.isBlank()) {
            avatar = DEFAULT_AVATAR;
        }

        Optional<Message> lastMessage = messageRepository
                .findByConversationAndDeletedAtIsNullOrderByCreatedAtDesc(conversation, PageRequest.of(0, 1))
                .stream()
                .findFirst();
        String lastText = lastMessage.map(Message::getText).orElse("No messages yet");
        if (lastText == null || lastText.isBlank()) {
            lastText = "No messages yet";
        }
        String time = lastMessage.map(Message::getCreatedAt).map(this::formatTime).orElse("");

        if (conversation.getType() != ConversationType.DIRECT && lastMessage.isPresent()) {
            String senderName = lastMessage.get().getSender().getFirstName();
            lastText = senderName + ": " + lastText;
        }

        return Map.of(
                "id", conversation.getId(),
                "name", name,
                "avatar", avatar,
                "lastMessage", lastText,
                "time", time,
                "unread", 0,
                "online", false,
                "friend", false,
                "subtitle", subtitle
        );
    }

    private Map<String, Object> buildActiveConversationView(Conversation conversation, User me) {
        User other = null;
        String name = conversation.getTitle();
        String avatar = conversation.getAvatarUrl();
        String subtitle = conversation.getType() == ConversationType.GROUP ? "Group chat" : "Conversation";

        Optional<Message> lastMessage = messageRepository.findByConversationAndDeletedAtIsNullOrderByCreatedAtDesc(
                conversation, PageRequest.of(0, 1)
        ).stream().findFirst();
        Long lastMessageAt = lastMessage.map(Message::getCreatedAt)
                .map(dt -> dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).orElse(0L);

        if (conversation.getType() == ConversationType.DIRECT) {
            other = conversationMemberRepository.findOtherUserByConversationDirect(conversation, me);
            if (other != null) {
                name = other.getFirstName() + " " + other.getLastName();
                subtitle = "@" + other.getUsername();
                if (other.getProfile() != null && other.getProfile().getAvatarUrl() != null) {
                    avatar = other.getProfile().getAvatarUrl();
                }
            }
        } else if (conversation.getType() == ConversationType.GROUP) {
            int members = conversationMemberRepository.findByConversation(conversation).size();
            subtitle = members + " members";
        }

        if (name == null || name.isBlank()) {
            name = conversation.getType() == ConversationType.GROUP ? "Group chat" : "Conversation";
        }
        if (avatar == null || avatar.isBlank()) {
            avatar = DEFAULT_AVATAR;
        }

        return Map.of(
                "id", conversation.getId(),
                "name", name,
                "avatar", avatar,
                "lastMessage", lastMessage,
                "lastMessageAt", lastMessageAt,
                "subtitle", subtitle,
                "online", false,
                "friend", false,
                "type", conversation.getType().name()
        );
    }

    private List<Map<String, Object>> buildMessageViews(Conversation conversation, User me) {
        List<Message> raw = new ArrayList<>(messageRepository
                .findByConversationAndDeletedAtIsNullOrderByCreatedAtDesc(conversation, PageRequest.of(0, 50))
                .getContent());
        Collections.reverse(raw);
        List<Map<String, Object>> items = new ArrayList<>();
        for (Message message : raw) {
            boolean fromMe = message.getSender() != null && message.getSender().getId().equals(me.getId());
            String senderName = message.getSender() != null ? message.getSender().getFirstName() : "Unknown";
            String text = message.getText();
            if (text == null) {
                text = "";
            }
            items.add(Map.of(
                    "fromMe", fromMe,
                    "text", text,
                    "time", formatTime(message.getCreatedAt()),
                    "senderName", senderName,
                    "sentAt", message.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toString()
            ));
        }
        return items;
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

}
