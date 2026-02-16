package org.example.isc.main.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum RoleEnum {
    USER,
    ADMIN;

    public SimpleGrantedAuthority toAuthority(){
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }
}
