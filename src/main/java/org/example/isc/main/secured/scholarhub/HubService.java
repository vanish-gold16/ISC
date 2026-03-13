package org.example.isc.main.secured.scholarhub;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class HubService {

    public ResponseEntity<Void> loadHub(Authentication authentication) {
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> createSchedule(Authentication authentication) {


        return ResponseEntity.ok().build();
    }
}
