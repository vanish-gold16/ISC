package org.example.isc.main.secured.messenger;

import org.example.isc.main.dto.messenger.ConversationDTO;
import org.example.isc.main.dto.messenger.CreateDirectRequest;
import org.example.isc.main.dto.messenger.CreateGroupRequest;
import org.example.isc.main.dto.messenger.MessageDTO;
import org.example.isc.main.enums.conversation.ConversationType;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.models.messenger.ConversationMember;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationMemberRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationRepository;
import org.example.isc.main.secured.repositories.conversation.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class MessengerApiController {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessengerService messengerService;
    private final MessageRepository messageRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    public MessengerApiController(ConversationRepository conversationRepository, UserRepository userRepository, MessengerService messengerService, MessageRepository messageRepository, ConversationMemberRepository conversationMemberRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messengerService = messengerService;
        this.messageRepository = messageRepository;
        this.conversationMemberRepository = conversationMemberRepository;
    }

    @GetMapping
    public ResponseEntity<List<ConversationDTO>> getMyConversations(
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                        .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        List<ConversationDTO> result = messengerService.getConversations(me);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/direct")
    public ResponseEntity<ConversationDTO> getOrCreateDirect(
        @RequestBody CreateDirectRequest request,
        Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        User target = userRepository.findById(request.getTarget())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ConversationDTO conversationDTO = messengerService.getOrCreateDirect(me, target);

        return ResponseEntity.ok(conversationDTO);
    }

    @PostMapping("/group")
    public ResponseEntity<ConversationDTO> getOrCreateGroup(
            @RequestBody CreateGroupRequest request,
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        String title = request.getTitle();
        if (title == null || title.isBlank()) {
            title = me.getUsername() + "'s group";
        }

        Conversation conversation = new Conversation(
                ConversationType.GROUP,
                title,
                null,
                me,
                LocalDateTime.now()
        );
        ConversationDTO conversationDTO = messengerService.getOrCreateGroup(me, conversation);

        return ResponseEntity.ok(conversationDTO);
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<Page<MessageDTO>> history(
        @PathVariable Long id,
        Authentication authentication,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        Conversation currentConversation = conversationRepository.findById(id)
                        .orElseThrow(() -> new IllegalStateException("Conversation not found: " + id));
        if(!conversationMemberRepository.existsByConversationAndUser(currentConversation, me))
            return ResponseEntity.status(403).build();

        Pageable pageable = PageRequest.of(page, size);

        Page<MessageDTO> messages = messageRepository.findByConversationAndDeletedAtIsNullOrderByCreatedAtDesc(currentConversation, pageable)
                .map(MessageDTO::from);

        return ResponseEntity.ok(messages);
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<Void> rename(
            @PathVariable Long id,
            Authentication authentication,
            String newName
    ){
        Conversation currentConversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Conversation not found: " + id));
        messengerService.rename(currentConversation, newName, authentication);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/members/{userId}/add")
    public ResponseEntity<ConversationMember> addMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found " + userId));
        Conversation currentConversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Conversation not found: " + id));
        if(!conversationMemberRepository.existsByConversationAndUser(currentConversation, me))
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(messengerService.addUser(currentConversation, target, authentication));
    }

    @DeleteMapping("{id}/members/{userId}/delete")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            Authentication authentication,
            @PathVariable Long userId
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found " + userId));
        Conversation currentConversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Conversation not found: " + id));
        if(!conversationMemberRepository.existsByConversationAndUser(currentConversation, me))
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(messengerService.deleteUser(currentConversation, target, authentication));
    }

    @PostMapping("{id}/read")
    public ResponseEntity<Void> readConversation(
            @PathVariable Long id,
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Conversation not found: " + id));

        if(!conversationMemberRepository.existsByConversationAndUser(conversation, me)){
            return ResponseEntity.notFound().build();
        }
        messengerService.markConversationRead(conversation, me);

        return ResponseEntity.ok().build();
    }

}
