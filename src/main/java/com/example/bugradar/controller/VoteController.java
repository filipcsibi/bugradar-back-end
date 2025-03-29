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

    @PostMapping("/bug/{bugId}")
    public ResponseEntity<Void> voteOnBug(
            @PathVariable String bugId,
            @RequestParam boolean isUpvote,
            Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        voteService.voteOnBug(bugId, uid, isUpvote);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/comment/{commentId}")
    public ResponseEntity<Void> voteOnComment(
            @PathVariable String commentId,
            @RequestParam boolean isUpvote,
            Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        voteService.voteOnComment(commentId, uid, isUpvote);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}