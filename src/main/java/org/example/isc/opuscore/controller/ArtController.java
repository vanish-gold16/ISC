package org.example.isc.opuscore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/opuscore/art-page")
public class ArtController {

    @GetMapping("/{ic}")
    public String getArt(){
        return "/opuscore/art-page";
    }

}
