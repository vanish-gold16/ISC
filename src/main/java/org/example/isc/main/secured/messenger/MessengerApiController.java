package org.example.isc.main.secured.messenger;

import org.example.isc.main.dto.ConversationDTO;
import org.example.isc.main.dto.CreateDirectRequest;
import org.example.isc.main.dto.CreateGroupRequest;
import org.example.isc.main.enums.conversation.ConversationType;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationRepository;
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

    public MessengerApiController(ConversationRepository conversationRepository, UserRepository userRepository, MessengerService messengerService, MessengerService messengerService1) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messengerService = messengerService1;
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

}
