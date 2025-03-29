package com.example.bugradar.controller;

import com.example.bugradar.dto.UserDto;
import com.example.bugradar.entity.User;
import com.example.bugradar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDto userDto, Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        User user = userService.createUser(uid, userDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody UserDto userDto, Authentication authentication) {
        String uid = (String) authentication.getPrincipal();

        // Verificăm dacă utilizatorul încearcă să își actualizeze propriul profil
        if (!id.equals(uid)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        User updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id, Authentication authentication) {
        String uid = (String) authentication.getPrincipal();

        // Verificăm dacă utilizatorul încearcă să își șteargă propriul profil
        if (!id.equals(uid)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}