package com.model.user;

import com.model.enums.AuthRole;
import com.model.rect.BoundsRect;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class User {
    @NotNull
    UUID id;
    @Size(max = 255) @NotNull
    String username; //dürfen keine Whitespaces im Nutzernamen erlauben, lieber _
    @Size(max = 255) @NotNull
    String password;
    AuthRole role;
    //save all assigned rects
    List<BoundsRect> assignedRects;

    public User() {}

    public User(String username, String password, AuthRole role) {
        id = UUID.randomUUID();
        this.username = username;
        this.password = password;
        this.role = role;
        assignedRects = new ArrayList<>();
    }

    public User(UUID id, String username, String password, AuthRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        assignedRects = new ArrayList<>();
    }
}
