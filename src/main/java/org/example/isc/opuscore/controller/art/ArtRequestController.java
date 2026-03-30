package org.example.isc.opuscore.controller.art;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.isc.main.enums.RoleEnum;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.NewArtRequestDTO;
import org.example.isc.opuscore.models.NewArtRequest;
import org.example.isc.opuscore.repositories.NewArtRequestRepository;
import org.example.isc.opuscore.service.ReviewService;
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
    private final ReviewService reviewService;

    public ArtRequestController(
            UserRepository userRepository,
            ArtworkService artworkService,
            NewArtRequestRepository newArtRequestRepository,
            ReviewService reviewService
    ) {
        this.userRepository = userRepository;
        this.artworkService = artworkService;
        this.newArtRequestRepository = newArtRequestRepository;
        this.reviewService = reviewService;
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

        boolean isOwner = request.getRequester() != null
                && request.getRequester().getId() != null
                && request.getRequester().getId().equals(me.getId());
        boolean isAdmin = me.getRole() == RoleEnum.ADMIN;

        if (!isOwner && !isAdmin) {
            return "redirect:/opuscore";
        }

        if (request.getStatus() == org.example.isc.opuscore.enums.ReviewStatusEnum.REJECTED) {
            return "redirect:/opuscore";
        }

        if (request.getApprovedArtworkId() != null) {
            return "redirect:/opuscore/art-page/" + request.getApprovedArtworkId();
        }

        model.addAttribute("title", request.getName() != null ? request.getName() : "Pending artwork");
        model.addAttribute("user", me);
        model.addAttribute("request", request);
        reviewService.findExistingReview(me.getId(), request.getApprovedArtworkId(), request.getId())
                .ifPresent(review -> model.addAttribute("existingReview", review));

        return "/opuscore/art-page";
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

        return "redirect:/opuscore/art-requests/" + requestId;
    }

}
