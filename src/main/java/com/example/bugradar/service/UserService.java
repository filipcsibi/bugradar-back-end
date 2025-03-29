package com.example.bugradar.service;


import com.example.bugradar.dto.UserDto;
import com.example.bugradar.entity.User;
import com.example.bugradar.repository.FirestoreUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final FirestoreUserRepository userRepository;

    @Autowired
    public UserService(FirestoreUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String uid, UserDto userDto) {
        User user = new User();
        user.setUid(uid);
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        return userRepository.save(user);
    }

    public User getUserById(String uid) {
        return userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(String uid, UserDto userDto) {
        User user = getUserById(uid);
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        return userRepository.save(user);
    }

    public void deleteUser(String uid) {
        userRepository.deleteById(uid);
    }
}