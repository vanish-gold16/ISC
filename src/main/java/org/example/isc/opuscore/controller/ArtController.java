package org.example.isc.opuscore.controller;

import org.example.isc.opuscore.models.Artwork;
import org.example.isc.opuscore.repositories.ArtworkRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/opuscore/art-page")
public class ArtController {

    private final ArtworkRepository artworkRepository;

    public ArtController(ArtworkRepository artworkRepository) {
        this.artworkRepository = artworkRepository;
    }

    @GetMapping("/{id}")
    public String getArt(
            @PathVariable Long id,
            Model model
    ){
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artwork not found: " + id));

        model.addAttribute("title", "Artwork");
        model.addAttribute("artwork", artwork);

        return "/opuscore/art-page";
    }

}
