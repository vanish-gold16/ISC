package org.example.isc.main.dto.messenger;

import jakarta.validation.constraints.NotNull;
import org.example.isc.main.secured.models.User;

import java.util.List;

public class CreateGroupRequest {

    @NotNull
    private User me;

    private List<User> target;

    private String title;

    public CreateGroupRequest() {
    }

    public CreateGroupRequest(User me) {
        this.me = me;
    }

    public CreateGroupRequest(User me, List<User> target) {
        this.me = me;
        this.target = target;
    }


    public String getTitle() {
        if (title == null || title.isBlank()) {
            return me != null ? me.getUsername() + "'s group" : null;
        }
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getMe() {
        return me;
    }

    public void setMe(User me) {
        this.me = me;
    }

    public List<User> getTarget() {
        return target;
    }

    public void setTarget(List<User> target) {
        this.target = target;
    }
}
