package com.flavientech.service;
import com.flavientech.entity.User;
import com.flavientech.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<String> getAllUsernames() {
        return userRepository.findAll().stream().map(User::getUsername).toList();
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void deleteUserByUsername(String username) {
        userRepository.deleteByUsername(username);
    }

    public User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        return userRepository.save(user);
    }
}