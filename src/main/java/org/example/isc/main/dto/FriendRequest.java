package org.example.isc.main.dto;

import jakarta.validation.constraints.NotNull;
import org.example.isc.main.secured.models.User;

public class FriendRequest {

    @NotNull
    private User sender;

    @NotNull
    private User reciever;

}
