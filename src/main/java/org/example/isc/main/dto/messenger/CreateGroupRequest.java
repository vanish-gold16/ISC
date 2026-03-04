package org.example.isc.main.dto.messenger;

import jakarta.validation.constraints.NotNull;
import org.example.isc.main.secured.models.User;

import java.util.List;

public class CreateGroupRequest {

    @NotNull
    private Long me;

    private List<Long> target;

    private String title;

    public CreateGroupRequest() {
    }

    public CreateGroupRequest(Long me) {
        this.me = me;
    }

    public CreateGroupRequest(Long me, List<Long> target) {
        this.me = me;
        this.target = target;
    }

    public Long getMe() {
        return me;
    }

    public void setMe(Long me) {
        this.me = me;
    }

    public List<Long> getTarget() {
        return target;
    }

    public void setTarget(List<Long> target) {
        this.target = target;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
