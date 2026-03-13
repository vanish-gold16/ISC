package org.example.isc.main.dto.messenger;

import jakarta.validation.constraints.NotNull;

public class CreateDirectRequest {

    @NotNull
    private Long me;

    @NotNull
    private Long target;

    public Long getMe() {
        return me;
    }

    public void setMe(Long me) {
        this.me = me;
    }

    public Long getTarget() {
        return target;
    }

    public void setTarget(Long target) {
        this.target = target;
    }
}
