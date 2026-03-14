package org.example.isc.main.secured.scholarhub;

import org.example.isc.main.secured.models.users.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class HubService {

    public ResponseEntity<Void> loadHub(Authentication authentication) {
        return ResponseEntity.ok().build();
    }

    private void setup(Authentication authentication, NewScheduleForm form){

    }
}
