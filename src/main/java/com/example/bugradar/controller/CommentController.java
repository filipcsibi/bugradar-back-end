package com.example.bugradar.controller;


import com.example.bugradar.dto.CommentDto;
import com.example.bugradar.entity.Comment;
import com.example.bugradar.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bugs/{bugId}/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(
            @PathVariable String bugId,
            @RequestBody CommentDto commentDto,
            Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        Comment comment = commentService.createComment(commentDto, bugId, uid);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable String id) {
        Comment comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getCommentsByBugId(@PathVariable String bugId) {
        List<Comment> comments = commentService.getCommentsByBugId(bugId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable String id,
            @RequestBody CommentDto commentDto,
            Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        Comment updatedComment = commentService.updateComment(id, commentDto, uid);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String id,
            Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        commentService.deleteComment(id, uid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}