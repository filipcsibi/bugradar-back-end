package com.example.bugradar.service;

import com.example.bugradar.entity.Bug;
import com.example.bugradar.entity.Comment;
import com.example.bugradar.entity.Vote;
import com.example.bugradar.repository.FirestoreBugRepository;
import com.example.bugradar.repository.FirestoreCommentRepository;
import com.example.bugradar.repository.FirestoreVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VoteService {

    private final FirestoreVoteRepository voteRepository;
    private final FirestoreBugRepository bugRepository;
    private final FirestoreCommentRepository commentRepository;

    @Autowired
    public VoteService(FirestoreVoteRepository voteRepository, FirestoreBugRepository bugRepository, FirestoreCommentRepository commentRepository) {
        this.voteRepository = voteRepository;
        this.bugRepository = bugRepository;
        this.commentRepository = commentRepository;
    }

    public void voteOnBug(String bugId, String userId, boolean isUpvote) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        // Verificăm dacă utilizatorul este autorul (nu poate vota propriul bug)
        if (bug.getAuthorId().equals(userId)) {
            throw new RuntimeException("Cannot vote on your own bug");
        }

        Optional<Vote> existingVote = voteRepository.findByUserIdAndBugId(userId, bugId);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            // Dacă votul este același, nu facem nimic
            if (vote.isUpvote() == isUpvote) {
                return;
            }

            // Actualizăm votul existent
            vote.setUpvote(isUpvote);
            voteRepository.save(vote);

            // Actualizăm numărul de voturi pentru bug
            int voteChange = isUpvote ? 2 : -2; // +2 pentru schimbare de la down la up, -2 pentru schimbare de la up la down
            bug.setVoteCount(bug.getVoteCount() + voteChange);
        } else {
            // Creăm un vot nou
            Vote vote = new Vote();
            vote.setUserId(userId);
            vote.setBugId(bugId);
            vote.setUpvote(isUpvote);
            voteRepository.save(vote);

            // Actualizăm numărul de voturi pentru bug
            int voteChange = isUpvote ? 1 : -1;
            bug.setVoteCount(bug.getVoteCount() + voteChange);
        }

        bugRepository.save(bug);
    }

    public void voteOnComment(String commentId, String userId, boolean isUpvote) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Verificăm dacă utilizatorul este autorul (nu poate vota propriul comentariu)
        if (comment.getAuthorId().equals(userId)) {
            throw new RuntimeException("Cannot vote on your own comment");
        }

        Optional<Vote> existingVote = voteRepository.findByUserIdAndCommentId(userId, commentId);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            // Dacă votul este același, nu facem nimic
            if (vote.isUpvote() == isUpvote) {
                return;
            }

            // Actualizăm votul existent
            vote.setUpvote(isUpvote);
            voteRepository.save(vote);

            // Actualizăm numărul de voturi pentru comentariu
            int voteChange = isUpvote ? 2 : -2;
            comment.setVoteCount(comment.getVoteCount() + voteChange);
        } else {
            // Creăm un vot nou
            Vote vote = new Vote();
            vote.setUserId(userId);
            vote.setCommentId(commentId);
            vote.setUpvote(isUpvote);
            voteRepository.save(vote);

            // Actualizăm numărul de voturi pentru comentariu
            int voteChange = isUpvote ? 1 : -1;
            comment.setVoteCount(comment.getVoteCount() + voteChange);
        }

        commentRepository.save(comment);
    }
}