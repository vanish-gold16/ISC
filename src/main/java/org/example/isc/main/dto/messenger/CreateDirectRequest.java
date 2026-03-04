package org.example.isc.main.dto.messenger;

import jakarta.validation.constraints.NotNull;
import org.example.isc.main.secured.models.User;

public class CreateDirectRequest {

    @NotNull
    private User me;

    @NotNull
    private User target;

    public User getMe() {
        return me;
    }

    public void setMe(User me) {
        this.me = me;
    }

    public User getTarget() {
        return target;
    }

    public void setTarget(User target) {
        this.target = target;
    }
}
