package com.example.bugradar.service;

import com.example.bugradar.dto.BugDto;
import com.example.bugradar.dto.CommentDto;
import com.example.bugradar.entity.Bug;
import com.example.bugradar.entity.Comment;
import com.example.bugradar.entity.User;
import com.example.bugradar.repository.FirestoreBugRepository;
import com.example.bugradar.repository.FirestoreCommentRepository;
import com.example.bugradar.repository.FirestoreUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class ModeratorService {

    private final FirestoreUserRepository userRepository;
    private final FirestoreBugRepository bugRepository;
    private final FirestoreCommentRepository commentRepository;
    private final TagService tagService;

    // Lazy loading pentru a evita dependențele circulare
    @Lazy
    @Autowired
    private EmailService emailService;

    @Lazy
    @Autowired
    private SmsService smsService;

    @Autowired
    public ModeratorService(FirestoreUserRepository userRepository,
                            FirestoreBugRepository bugRepository,
                            FirestoreCommentRepository commentRepository,
                            TagService tagService) {
        this.userRepository = userRepository;
        this.bugRepository = bugRepository;
        this.commentRepository = commentRepository;
        this.tagService = tagService;
    }

    // Restul metodelor rămân la fel...
    public boolean isModerator(String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElse(null);
            return user != null && user.isModerator();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isBanned(String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElse(null);
            return user != null && user.isBanned();
        } catch (Exception e) {
            return false;
        }
    }

    public void banUser(String userId, String moderatorId, String reason) {
        if (!isModerator(moderatorId)) {
            throw new RuntimeException("Only moderators can ban users");
        }

        User userToBan = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userToBan.isModerator()) {
            throw new RuntimeException("Cannot ban another moderator");
        }

        userToBan.setBanned(true);
        userRepository.save(userToBan);

        // Trimitem notificări (cu lazy loading)
        try {
            if (emailService != null) {
                emailService.sendBanNotification(userToBan.getEmail(), reason);
            }
            if (smsService != null) {
                // smsService.sendBanNotification(userToBan.getPhoneNumber(), reason);
            }
        } catch (Exception e) {
            System.err.println("Failed to send ban notification: " + e.getMessage());
        }
    }

    public void unbanUser(String userId, String moderatorId) {
        if (!isModerator(moderatorId)) {
            throw new RuntimeException("Only moderators can unban users");
        }

        User userToUnban = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userToUnban.setBanned(false);
        userRepository.save(userToUnban);

        try {
            if (emailService != null) {
                emailService.sendUnbanNotification(userToUnban.getEmail());
            }
        } catch (Exception e) {
            System.err.println("Failed to send unban notification: " + e.getMessage());
        }
    }

    // Restul metodelor rămân la fel ca în implementarea originală...
    public void moderatorDeleteBug(String bugId, String moderatorId) {
        if (!isModerator(moderatorId)) {
            throw new RuntimeException("Only moderators can delete any bug");
        }
        bugRepository.deleteById(bugId);
    }

    public Bug moderatorEditBug(String bugId, BugDto bugDto, String moderatorId) {
        if (!isModerator(moderatorId)) {
            throw new RuntimeException("Only moderators can edit any bug");
        }

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        bug.setTitle(bugDto.getTitle());
        bug.setDescription(bugDto.getDescription());
        bug.setImageUrl(bugDto.getImageUrl());

        if (bugDto.getTagNames() != null && !bugDto.getTagNames().isEmpty()) {
            var tags = bugDto.getTagNames().stream()
                    .map(tagService::findOrCreateTag)
                    .toList();
            bug.setTags(tags);
        }

        return bugRepository.save(bug);
    }

    public void moderatorDeleteComment(String commentId, String moderatorId) {
        if (!isModerator(moderatorId)) {
            throw new RuntimeException("Only moderators can delete any comment");
        }
        commentRepository.deleteById(commentId);
    }

    public Comment moderatorEditComment(String commentId, CommentDto commentDto, String moderatorId) {
        if (!isModerator(moderatorId)) {
            throw new RuntimeException("Only moderators can edit any comment");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setText(commentDto.getText());
        comment.setImageUrl(commentDto.getImageUrl());

        return commentRepository.save(comment);
    }

    public void promoteToModerator(String userId, String moderatorId) {
        if (!isModerator(moderatorId)) {
            throw new RuntimeException("Only moderators can promote other users");
        }

        User userToPromote = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userToPromote.setModerator(true);
        userRepository.save(userToPromote);
    }

    public void demoteFromModerator(String userId, String moderatorId) {
        if (!isModerator(moderatorId)) {
            throw new RuntimeException("Only moderators can demote other moderators");
        }

        User userToDemote = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userToDemote.setModerator(false);
        userRepository.save(userToDemote);
    }

    public void checkUserAccess(String userId) {
        if (isBanned(userId)) {
            throw new RuntimeException("User is banned from the application");
        }
    }
}