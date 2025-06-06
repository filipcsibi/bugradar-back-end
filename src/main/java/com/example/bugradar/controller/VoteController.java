package com.example.bugradar.controller;

import com.example.bugradar.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    @Autowired
    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    /**
     * Votează pe un bug
     */
    @PostMapping("/bug/{bugId}")
    public ResponseEntity<String> voteOnBug(
            @PathVariable String bugId,
            @RequestParam boolean isUpvote,
            Authentication authentication) {
        try {
            String uid = (String) authentication.getPrincipal();
            voteService.voteOnBug(bugId, uid, isUpvote);
            return ResponseEntity.ok("Vote registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Votează pe un comentariu
     */
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<String> voteOnComment(
            @PathVariable String commentId,
            @RequestParam boolean isUpvote,
            Authentication authentication) {
        try {
            String uid = (String) authentication.getPrincipal();
            voteService.voteOnComment(commentId, uid, isUpvote);
            return ResponseEntity.ok("Vote registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrage votul pe un bug
     */
    @DeleteMapping("/bug/{bugId}")
    public ResponseEntity<String> removeVoteOnBug(
            @PathVariable String bugId,
            Authentication authentication) {
        try {
            String uid = (String) authentication.getPrincipal();
            voteService.removeVoteOnBug(bugId, uid);
            return ResponseEntity.ok("Vote removed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrage votul pe un comentariu
     */
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<String> removeVoteOnComment(
            @PathVariable String commentId,
            Authentication authentication) {
        try {
            String uid = (String) authentication.getPrincipal();
            voteService.removeVoteOnComment(commentId, uid);
            return ResponseEntity.ok("Vote removed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}