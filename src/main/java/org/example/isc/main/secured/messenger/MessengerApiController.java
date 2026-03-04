package org.example.isc.main.secured.messenger;

import org.example.isc.main.dto.ConversationDTO;
import org.example.isc.main.dto.CreateDirectRequest;
import org.example.isc.main.dto.CreateGroupRequest;
import org.example.isc.main.enums.conversation.ConversationType;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.models.messenger.ConversationMember;
import org.example.isc.main.secured.models.messenger.Message;
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

    public MessengerApiController(ConversationRepository conversationRepository, UserRepository userRepository, MessengerService messengerService, MessengerService messengerService1, MessageRepository messageRepository, ConversationMemberRepository conversationMemberRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messengerService = messengerService1;
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
        User target = userRepository.findById(request.getTarget().getId())
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

    @GetMapping("/{id}/messages?page=0")
    public Page<Message> history(
        @PathVariable Long id,
        Authentication authentication,
        int limit
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        Conversation currentConversation = conversationRepository.findById(id)
                        .orElseThrow(() -> new IllegalStateException("Conversation not found: " + id));
        if(conversationMemberRepository.existsByConversationAndUser(currentConversation, me))
            return Page.empty();

        Pageable pageable = PageRequest.of(0, limit);

        Page<Message> messages = messageRepository.findByConversationAndDeletedAtIsNullOrderByCreatedAtDesc(currentConversation, pageable);

        return messages;
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

    @PostMapping("/{id}/members/add")
    public ResponseEntity<ConversationMember> addMember(
            @PathVariable Long id,
            Authentication authentication,
            User user
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        Conversation currentConversation = conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Conversation not found: " + id));
        if(!conversationMemberRepository.existsByConversationAndUser(currentConversation, me))
            return ResponseEntity.notFound().build();


        return ResponseEntity.ok();
    }

}
