package org.example.isc.main.secured.scholarhub.controller.api;

import org.example.isc.main.dto.scholarship.GradeDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/scholar-hub/grades")
public class GradeApiController {

    @GetMapping
    public ResponseEntity<List<GradeDTO>> getAllGrades(

    ){



    }

}
