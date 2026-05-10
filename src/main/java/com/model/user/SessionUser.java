package com.model.user;

import com.model.enums.AuthRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SessionUser {
    @NotNull
    private UUID id;
    @Size(max = 255)
    private String username;
    @NotNull
    private AuthRole role;

    public SessionUser(UUID id, String username, AuthRole role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
}
