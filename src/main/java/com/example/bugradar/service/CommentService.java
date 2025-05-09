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

    @Autowired
    public CommentService(FirestoreCommentRepository commentRepository, FirestoreBugRepository bugRepository) {
        this.commentRepository = commentRepository;
        this.bugRepository = bugRepository;
    }

    public Comment createComment(CommentDto commentDto, String bugId, String authorId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

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
        Comment comment = getCommentById(id);

        // Verificăm dacă utilizatorul curent este autorul
        if (!comment.getAuthorId().equals(currentUserId)) {
            throw new RuntimeException("Not authorized to update this comment");
        }

        comment.setText(commentDto.getText());
        comment.setImageUrl(commentDto.getImageUrl());

        return commentRepository.save(comment);
    }

    public void deleteComment(String id, String currentUserId) {
        Comment comment = getCommentById(id);

        // Verificăm dacă utilizatorul curent este autorul
        if (!comment.getAuthorId().equals(currentUserId)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }

        commentRepository.deleteById(id);
    }
}