package com.example.bugradar.service;

import com.example.bugradar.dto.CommentDto;
import com.example.bugradar.entity.Bug;
import com.example.bugradar.entity.BugStatus;
import com.example.bugradar.entity.Comment;
import com.example.bugradar.repository.FirestoreBugRepository;
import com.example.bugradar.repository.FirestoreCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final FirestoreCommentRepository commentRepository;
    private final FirestoreBugRepository bugRepository;
    private final ModeratorService moderatorService;

    @Autowired
    public CommentService(FirestoreCommentRepository commentRepository,
                          FirestoreBugRepository bugRepository,
                          ModeratorService moderatorService) {
        this.commentRepository = commentRepository;
        this.bugRepository = bugRepository;
        this.moderatorService = moderatorService;
    }

    public Comment createComment(CommentDto commentDto, String bugId, String authorId) {
        // Verificăm dacă utilizatorul este banat
        moderatorService.checkUserAccess(authorId);

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        // Verificăm dacă bug-ul este în status SOLVED
        if (bug.getStatus() == BugStatus.SOLVED) {
            throw new RuntimeException("Cannot add comments to a solved bug");
        }

        // Actualizăm status-ul bug-ului la IN_PROGRESS dacă este primul comentariu
        if (bug.getStatus() == BugStatus.RECEIVED) {
            bug.setStatus(BugStatus.IN_PROGRESS);
            bugRepository.save(bug);
        }

        Comment comment = new Comment();
        comment.setBug(bug);
        comment.setBugId(bugId);
        comment.setAuthorId(authorId);
        comment.setText(commentDto.getText());
        comment.setImageUrl(commentDto.getImageUrl());
        comment.setCreationDate(LocalDateTime.now().toString());

        return commentRepository.save(comment);
    }

    public Comment getCommentById(String id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    public List<Comment> getCommentsByBugId(String bugId) {
        return commentRepository.findByBugIdOrderByVoteCountDesc(bugId);
    }

    public Comment updateComment(String id, CommentDto commentDto, String currentUserId) {
        // Verificăm dacă utilizatorul este banat
        moderatorService.checkUserAccess(currentUserId);

        Comment comment = getCommentById(id);

        // Verificăm dacă utilizatorul curent este autorul SAU moderator
        if (!comment.getAuthorId().equals(currentUserId) && !moderatorService.isModerator(currentUserId)) {
            throw new RuntimeException("Not authorized to update this comment");
        }

        comment.setText(commentDto.getText());
        comment.setImageUrl(commentDto.getImageUrl());

        return commentRepository.save(comment);
    }

    public void deleteComment(String id, String currentUserId) {
        // Verificăm dacă utilizatorul este banat
        moderatorService.checkUserAccess(currentUserId);

        Comment comment = getCommentById(id);

        // Verificăm dacă utilizatorul curent este autorul SAU moderator
        if (!comment.getAuthorId().equals(currentUserId) && !moderatorService.isModerator(currentUserId)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }

        commentRepository.deleteById(id);
    }

    /**
     * Marchează un bug ca fiind rezolvat (doar autorul bug-ului poate face asta)
     * BONUS: Feature din cerință - când autorul acceptă comentariile
     */
    public void markBugAsSolved(String bugId, String userId) {
        // Verificăm dacă utilizatorul este banat
        moderatorService.checkUserAccess(userId);

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        // Doar autorul poate marca bug-ul ca rezolvat
        if (!bug.getAuthorId().equals(userId)) {
            throw new RuntimeException("Only the bug author can mark it as solved");
        }

        bug.setStatus(BugStatus.SOLVED);
        bugRepository.save(bug);
    }
}