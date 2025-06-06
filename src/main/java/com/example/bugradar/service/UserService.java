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
    private final ModeratorService moderatorService;

    @Autowired
    public UserService(FirestoreUserRepository userRepository, ModeratorService moderatorService) {
        this.userRepository = userRepository;
        this.moderatorService = moderatorService;
    }

    public User createUser(String uid, UserDto userDto) {
        User user = new User();
        user.setUid(uid);
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setScore(0); // Scor inițial
        user.setBanned(false);
        user.setModerator(false);
        return userRepository.save(user);
    }

    public User getUserById(String uid) {
        return userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(String uid, UserDto userDto, String currentUserId) {
        // Verificăm dacă utilizatorul este banat
        moderatorService.checkUserAccess(currentUserId);

        // Verificăm dacă utilizatorul încearcă să își actualizeze propriul profil
        if (!uid.equals(currentUserId) && !moderatorService.isModerator(currentUserId)) {
            throw new RuntimeException("Not authorized to update this user");
        }

        User user = getUserById(uid);
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        // Nu permitem modificarea scorului, isBanned sau isModerator prin această metodă
        return userRepository.save(user);
    }

    public void deleteUser(String uid, String currentUserId) {
        // Verificăm dacă utilizatorul este banat
        moderatorService.checkUserAccess(currentUserId);

        // Doar moderatorii pot șterge utilizatori sau utilizatorul își poate șterge propriul cont
        if (!uid.equals(currentUserId) && !moderatorService.isModerator(currentUserId)) {
            throw new RuntimeException("Not authorized to delete this user");
        }

        userRepository.deleteById(uid);
    }

    /**
     * Obține utilizatorul cu scorul afișat (pentru BONUS 1)
     */
    public User getUserWithScore(String uid) {
        User user = getUserById(uid);
        // Scorul este deja stocat în entitate, deci îl returnăm direct
        return user;
    }

    /**
     * Obține toți utilizatorii sortați după scor (leaderboard)
     */
    public List<User> getUsersByScore() {
        List<User> users = userRepository.findAll();
        // Sortăm descrescător după scor
        users.sort((u1, u2) -> Double.compare(u2.getScore(), u1.getScore()));
        return users;
    }

    /**
     * Verifică dacă un utilizator poate accesa aplicația
     */
    public void validateUserAccess(String uid) {
        User user = getUserById(uid);
        if (user.isBanned()) {
            throw new RuntimeException("User is banned from the application. Please contact support.");
        }
    }

    /**
     * Obține statistici despre utilizator (pentru dashboard)
     */
    public UserStats getUserStats(String uid) {
        User user = getUserById(uid);

        // Poți extinde cu mai multe statistici
        return new UserStats(
                user.getScore(),
                user.isModerator(),
                user.isBanned()
        );
    }

    // Clasă inner pentru statisticile utilizatorului
    public static class UserStats {
        private final double score;
        private final boolean isModerator;
        private final boolean isBanned;

        public UserStats(double score, boolean isModerator, boolean isBanned) {
            this.score = score;
            this.isModerator = isModerator;
            this.isBanned = isBanned;
        }

        public double getScore() { return score; }
        public boolean isModerator() { return isModerator; }
        public boolean isBanned() { return isBanned; }
    }
}