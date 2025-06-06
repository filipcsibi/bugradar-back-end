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
import java.util.Map;

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
        User user = userService.getUserWithScore(id); // Include scorul în răspuns
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * BONUS 1: Leaderboard - utilizatori sortați după scor
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<User>> getLeaderboard() {
        List<User> users = userService.getUsersByScore();
        return ResponseEntity.ok(users);
    }

    /**
     * BONUS 1: Statistici utilizator (include scorul)
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<UserService.UserStats> getUserStats(@PathVariable String id) {
        UserService.UserStats stats = userService.getUserStats(id);
        return ResponseEntity.ok(stats);
    }

    /**
     * Profilul utilizatorului curent
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        User user = userService.getUserWithScore(uid);
        return ResponseEntity.ok(user);
    }

    /**
     * Statisticile utilizatorului curent
     */
    @GetMapping("/me/stats")
    public ResponseEntity<UserService.UserStats> getCurrentUserStats(Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        UserService.UserStats stats = userService.getUserStats(uid);
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody UserDto userDto, Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        User updatedUser = userService.updateUser(id, userDto, uid); // Actualizat să folosească noua metodă
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id, Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        userService.deleteUser(id, uid); // Actualizat să folosească noua metodă
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Verifică accesul utilizatorului (pentru debugging)
     */
    @GetMapping("/check-access")
    public ResponseEntity<Map<String, Object>> checkAccess(Authentication authentication) {
        try {
            String uid = (String) authentication.getPrincipal();
            userService.validateUserAccess(uid);

            User user = userService.getUserById(uid);
            return ResponseEntity.ok(Map.of(
                    "access", "granted",
                    "isBanned", user.isBanned(),
                    "isModerator", user.isModerator(),
                    "score", user.getScore()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("access", "denied", "reason", e.getMessage()));
        }
    }
}