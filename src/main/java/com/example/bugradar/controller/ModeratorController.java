package com.example.bugradar.controller;

import com.example.bugradar.dto.BugDto;
import com.example.bugradar.dto.CommentDto;
import com.example.bugradar.entity.Bug;
import com.example.bugradar.entity.Comment;
import com.example.bugradar.service.ModeratorService;
import com.example.bugradar.service.UserScoreService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/moderator")
public class ModeratorController {

    private final ModeratorService moderatorService;
    private final UserScoreService userScoreService;

    @Autowired
    public ModeratorController(ModeratorService moderatorService, UserScoreService userScoreService) {
        this.moderatorService = moderatorService;
        this.userScoreService = userScoreService;
    }

    private String getCurrentUserId(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        return decodedToken.getUid();
    }

    /**
     * Banează un utilizator
     */
    @PostMapping("/ban/{userId}")
    public ResponseEntity<String> banUser(
            @PathVariable String userId,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String idToken) {
        try {
            String moderatorId = getCurrentUserId(idToken.replace("Bearer ", ""));
            String reason = request.getOrDefault("reason", "Inappropriate behavior");

            moderatorService.banUser(userId, moderatorId, reason);
            return ResponseEntity.ok("User banned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Debanează un utilizator
     */
    @PostMapping("/unban/{userId}")
    public ResponseEntity<String> unbanUser(
            @PathVariable String userId,
            @RequestHeader("Authorization") String idToken) {
        try {
            String moderatorId = getCurrentUserId(idToken.replace("Bearer ", ""));

            moderatorService.unbanUser(userId, moderatorId);
            return ResponseEntity.ok("User unbanned successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Șterge orice bug (ca moderator)
     */
    @DeleteMapping("/bugs/{bugId}")
    public ResponseEntity<String> deleteBug(
            @PathVariable String bugId,
            @RequestHeader("Authorization") String idToken) {
        try {
            String moderatorId = getCurrentUserId(idToken.replace("Bearer ", ""));

            moderatorService.moderatorDeleteBug(bugId, moderatorId);
            return ResponseEntity.ok("Bug deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Editează orice bug (ca moderator)
     */
    @PutMapping("/bugs/{bugId}")
    public ResponseEntity<Bug> editBug(
            @PathVariable String bugId,
            @RequestBody BugDto bugDto,
            @RequestHeader("Authorization") String idToken) {
        try {
            String moderatorId = getCurrentUserId(idToken.replace("Bearer ", ""));

            Bug updatedBug = moderatorService.moderatorEditBug(bugId, bugDto, moderatorId);
            return ResponseEntity.ok(updatedBug);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Șterge orice comentariu (ca moderator)
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable String commentId,
            @RequestHeader("Authorization") String idToken) {
        try {
            String moderatorId = getCurrentUserId(idToken.replace("Bearer ", ""));

            moderatorService.moderatorDeleteComment(commentId, moderatorId);
            return ResponseEntity.ok("Comment deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Editează orice comentariu (ca moderator)
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Comment> editComment(
            @PathVariable String commentId,
            @RequestBody CommentDto commentDto,
            @RequestHeader("Authorization") String idToken) {
        try {
            String moderatorId = getCurrentUserId(idToken.replace("Bearer ", ""));

            Comment updatedComment = moderatorService.moderatorEditComment(commentId, commentDto, moderatorId);
            return ResponseEntity.ok(updatedComment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Promovează un utilizator ca moderator
     */
    @PostMapping("/promote/{userId}")
    public ResponseEntity<String> promoteToModerator(
            @PathVariable String userId,
            @RequestHeader("Authorization") String idToken) {
        try {
            String moderatorId = getCurrentUserId(idToken.replace("Bearer ", ""));

            moderatorService.promoteToModerator(userId, moderatorId);
            return ResponseEntity.ok("User promoted to moderator successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrogradează un moderator
     */
    @PostMapping("/demote/{userId}")
    public ResponseEntity<String> demoteFromModerator(
            @PathVariable String userId,
            @RequestHeader("Authorization") String idToken) {
        try {
            String moderatorId = getCurrentUserId(idToken.replace("Bearer ", ""));

            moderatorService.demoteFromModerator(userId, moderatorId);
            return ResponseEntity.ok("User demoted from moderator successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Recalculează scorul unui utilizator (util pentru debugging)
     */
    @PostMapping("/recalculate-score/{userId}")
    public ResponseEntity<String> recalculateUserScore(
            @PathVariable String userId,
            @RequestHeader("Authorization") String idToken) {
        try {
            String moderatorId = getCurrentUserId(idToken.replace("Bearer ", ""));

            if (!moderatorService.isModerator(moderatorId)) {
                return ResponseEntity.status(403).body("Only moderators can recalculate scores");
            }

            userScoreService.recalculateUserScore(userId);
            return ResponseEntity.ok("User score recalculated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Recalculează scorurile tuturor utilizatorilor
     */
    @PostMapping("/recalculate-all-scores")
    public ResponseEntity<String> recalculateAllUserScores(
            @RequestHeader("Authorization") String idToken) {
        try {
            String moderatorId = getCurrentUserId(idToken.replace("Bearer ", ""));

            if (!moderatorService.isModerator(moderatorId)) {
                return ResponseEntity.status(403).body("Only moderators can recalculate all scores");
            }

            userScoreService.recalculateAllUserScores();
            return ResponseEntity.ok("All user scores recalculated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Verifică dacă utilizatorul curent este moderator
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkModeratorStatus(
            @RequestHeader("Authorization") String idToken) {
        try {
            String userId = getCurrentUserId(idToken.replace("Bearer ", ""));
            boolean isModerator = moderatorService.isModerator(userId);

            return ResponseEntity.ok(Map.of("isModerator", isModerator));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}