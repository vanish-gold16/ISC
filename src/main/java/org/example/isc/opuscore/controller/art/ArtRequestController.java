package org.example.isc.opuscore.controller.art;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.NewArtRequestDTO;
import org.example.isc.opuscore.models.NewArtRequest;
import org.example.isc.opuscore.repositories.NewArtRequestRepository;
import org.example.isc.opuscore.service.ArtworkService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/opuscore/art-requests")
public class ArtRequestController {

    private final UserRepository userRepository;
    private final ArtworkService artworkService;
    private final NewArtRequestRepository newArtRequestRepository;

    public ArtRequestController(UserRepository userRepository, ArtworkService artworkService, NewArtRequestRepository newArtRequestRepository) {
        this.userRepository = userRepository;
        this.artworkService = artworkService;
        this.newArtRequestRepository = newArtRequestRepository;
    }

    @GetMapping
    public String getArtRequest(
            Authentication authentication,
            Model model,
            @RequestParam(value = "error", required = false) String error
    ){
        return "/opuscore/new-art";
    }

    @GetMapping("/{id}")
    public String getPendingArtRequest(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ){
        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Art request not found: " + id));

        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        if(!(request.getRequester() == me)){
            return "redirect:/opuscore/new-art";
        }
        model.addAttribute("title" + request.getName());
        model.addAttribute("request", request);

        return "/opuscore/art-page-pending/" + id;
    }

    @PostMapping
    public String postArtRequest(
            @Valid @RequestBody NewArtRequestDTO dto,
            BindingResult bindingResult,
            Model model,
            Authentication authentication
    ){
        if(bindingResult.hasErrors()){
            model.addAttribute("form", dto);
            return getArtRequest(authentication, model, null);
        }

        Long requestId = 0L;

        if(dto == null){
            return "/opuscore/new-art";
        }

        try{
            requestId = artworkService.newArtRequest(authentication, dto);
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", dto);
            return getArtRequest(authentication, model, null);
        }

        log.info("Art request created");

        return "redirect:/opuscore/art-page-pending/" + requestId;
    }

}
