package com.model.user;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LoginResponse {
    private UUID id;
    private String username;
    private String role;

    public LoginResponse(UUID id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
}
