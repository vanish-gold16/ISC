package org.example.isc.main.secured.messenger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class MessengerApiController {

    @GetMapping
    public List<ConversationDTO> getMyConversations(){

    }

    @PostMapping("/direct")
    public void

}
