package org.example.isc.opuscore.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.NewArtRequestDTO;
import org.example.isc.opuscore.enums.ReviewStatusEnum;
import org.example.isc.opuscore.models.NewArtRequest;
import org.example.isc.opuscore.service.ArtService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Controller
@RequestMapping("/opuscore/art-requests")
public class ArtRequestController {

    private final UserRepository userRepository;
    private final ArtService artService;

    public ArtRequestController(UserRepository userRepository, ArtService artService) {
        this.userRepository = userRepository;
        this.artService = artService;
    }

    @GetMapping
    public String getArtRequest(
            Authentication authentication,
            Model model,
            @RequestParam(value = "error", required = false) String error
    ){
        return "/opuscore/new-art";
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
            requestId = artService.newArtRequest(authentication, dto);
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", dto);
            return getArtRequest(authentication, model, null);
        }

        log.info("Art request created");

        return "redirect:/opuscore/art-page/" + requestId;
    }

}
