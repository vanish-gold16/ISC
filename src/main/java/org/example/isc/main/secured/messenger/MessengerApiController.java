package org.example.isc.main.secured.messenger;

import org.example.isc.main.dto.ConversationDTO;
import org.example.isc.main.dto.CreateDirectRequest;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.messenger.Conversation;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.conversation.ConversationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ConversationDTO> createConversation(
        @RequestBody CreateDirectRequest request,
        Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        ConversationDTO conversationDTO = messengerService.getOrCreateDirect(me, request.getTarget());

        return ResponseEntity.ok(conversationDTO);
    }

}
