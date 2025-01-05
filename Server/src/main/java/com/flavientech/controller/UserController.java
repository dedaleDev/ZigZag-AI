package com.flavientech.controller;

import com.flavientech.entity.User;
import com.flavientech.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public List<String> getAllUsernames() {
        return userService.getAllUsernames();
    }

    public Optional<User> getUserByUsername(String username) {
        return userService.getUserByUsername(username);
    }

    public User createUser(String username) {
        return userService.createUser(username);
    }

    public void deleteUserByUsername(String username) {
        userService.deleteUserByUsername(username);
    }
}