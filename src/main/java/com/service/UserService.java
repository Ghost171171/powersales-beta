package com.service;

import com.model.enums.AuthRole;
import com.model.user.LoginResponse;
import com.model.user.User;
import com.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final static Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addUser(User user) {
        String password = user.getPassword();
        //Generate Hashed Password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        //Set Hashed Password
        user.setPassword(hashedPassword);
        //Add User with Hashed Password
        userRepository.addUser(user);

    }

    //if password has been updated
    public void updateUserPassword(User user) {
        String password = user.getPassword();
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        user.setPassword(hashedPassword);
        userRepository.updateUser(user);
    }

    //if no password has been updated
    public void updateUser(User user) {
        userRepository.updateUser(user);
    }

    public void deleteUser(UUID id) {
        userRepository.deleteUser(id);
    }

    public User getUserById(UUID id) {
        return userRepository.getUser(id).orElseThrow();
    }

    public User getUserByUsername(String username) {
        return userRepository.getUserByName(username).orElseThrow();
    }

    public ArrayList<User> getAllUsers() {
        return new ArrayList<>(userRepository.getAllUsers());
    }

    //authenticate if user password tupel exists
    public LoginResponse authenticate(String username, String password) {

        Optional<User> userOpt = userRepository.getUserByName(username);

        if (userOpt.isEmpty()) {
            log.info("Could not find user with name {}", username);
            return null;
        }

        User user = userOpt.get();

        if (!BCrypt.checkpw(password, user.getPassword())) {
            log.info("Wrong password typed by user of name {}!",  username);
            return null;
        }

        log.info("Successfully authenticated user {}", username);
        return new LoginResponse(user.getId(), user.getUsername(), user.getRole().toString());
    }
}

//TEST USER: Ben;Banana123 und TEST ADMIN: Vicco;Shiro2023!xD
class Test {
    public static void main(String[] args) {
        UserService userService = new UserService(UserRepository.getInstance());
        userService.addUser(new User("Vicco", "Shiro2023!xD", AuthRole.ADMIN));
    }

}
