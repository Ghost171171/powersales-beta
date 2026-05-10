package com.controller;

import com.model.enums.AuthRole;
import com.model.user.LoginRequest;
import com.model.user.LoginResponse;
import com.model.user.SessionUser;
import com.model.user.User;
import com.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final static Logger log = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /users/login -> validiere User über Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            LoginResponse resp = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

            if (resp == null) {
                log.warn("Authentication failed for user {} at {}", loginRequest.getUsername(),  request.getRequestURI());
                return ResponseEntity.status(401).build();
            }

            log.info("Authenticated user {} successfully",  loginRequest.getUsername());

            HttpSession oldSession = request.getSession(false);

            if (oldSession != null) {
                log.info("Invalidate old session for user {}", loginRequest.getUsername());
                oldSession.invalidate();
            }

            HttpSession newSession = request.getSession(true);

            AuthRole role;
            try {
                role = AuthRole.valueOf(resp.getRole().toUpperCase());
            } catch (Exception e) {
                log.error("Invalid role {} for user {}", resp.getRole(), loginRequest.getUsername());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR ,"Invalid role");
            }

            SessionUser user = new SessionUser(resp.getId(), resp.getUsername(), role);
            log.info("Successful Login for user {}", loginRequest.getUsername());

            newSession.setAttribute("user", user);
            newSession.setMaxInactiveInterval(60*60);

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Login failed for user {}", loginRequest.getUsername(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR ,"Login Failed");
        }
    }

   @PostMapping("/logout")
   public ResponseEntity<Void> logout(HttpSession session) {
        if (session != null) {
            log.info("Logout user {}", session.getAttribute("user"));
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
   }

   //GET /users/{username} -> bekomme UserId durch Username
   @GetMapping("/find")
   public ResponseEntity<String> getCurrentUserId(@RequestParam String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            log.warn("Unauthorized access attempt to /users/me without valid session");
            throw new ResponseStatusException((HttpStatus.UNAUTHORIZED), "No active session found!");
        }
        log.info("Successfully assigned rect to user {}", user.getId());
        return ResponseEntity.ok(user.getId().toString());
    }
}
