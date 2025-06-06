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
    private final UserScoreService userScoreService;
    private final ModeratorService moderatorService;

    @Autowired
    public VoteService(FirestoreVoteRepository voteRepository,
                       FirestoreBugRepository bugRepository,
                       FirestoreCommentRepository commentRepository,
                       UserScoreService userScoreService,
                       ModeratorService moderatorService) {
        this.voteRepository = voteRepository;
        this.bugRepository = bugRepository;
        this.commentRepository = commentRepository;
        this.userScoreService = userScoreService;
        this.moderatorService = moderatorService;
    }

    public void voteOnBug(String bugId, String userId, boolean isUpvote) {
        // Verificăm dacă utilizatorul este banat
        moderatorService.checkUserAccess(userId);

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        // Verificăm dacă utilizatorul este autorul (nu poate vota propriul bug)
        if (bug.getAuthorId().equals(userId)) {
            throw new RuntimeException("Cannot vote on your own bug");
        }

        Optional<Vote> existingVote = voteRepository.findByUserIdAndBugId(userId, bugId);
        boolean isNewVote = existingVote.isEmpty();
        Boolean previousVoteValue = existingVote.map(Vote::isUpvote).orElse(null);

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

        // BONUS 1: Actualizăm scorul utilizatorilor
        userScoreService.updateUserScoreForVote(
                bug.getAuthorId(),
                userId,
                isUpvote,
                true, // este bug
                isNewVote,
                previousVoteValue
        );
    }

    public void voteOnComment(String commentId, String userId, boolean isUpvote) {
        // Verificăm dacă utilizatorul este banat
        moderatorService.checkUserAccess(userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Verificăm dacă utilizatorul este autorul (nu poate vota propriul comentariu)
        if (comment.getAuthorId().equals(userId)) {
            throw new RuntimeException("Cannot vote on your own comment");
        }

        Optional<Vote> existingVote = voteRepository.findByUserIdAndCommentId(userId, commentId);
        boolean isNewVote = existingVote.isEmpty();
        Boolean previousVoteValue = existingVote.map(Vote::isUpvote).orElse(null);

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

        // BONUS 1: Actualizăm scorul utilizatorilor
        userScoreService.updateUserScoreForVote(
                comment.getAuthorId(),
                userId,
                isUpvote,
                false, // este comentariu
                isNewVote,
                previousVoteValue
        );
    }

    /**
     * Șterge un vot (pentru cazurile când utilizatorul își retrage votul)
     */
    public void removeVoteOnBug(String bugId, String userId) {
        moderatorService.checkUserAccess(userId);

        Optional<Vote> vote = voteRepository.findByUserIdAndBugId(userId, bugId);
        if (vote.isPresent()) {
            Bug bug = bugRepository.findById(bugId)
                    .orElseThrow(() -> new RuntimeException("Bug not found"));

            // Actualizăm numărul de voturi
            int voteChange = vote.get().isUpvote() ? -1 : 1;
            bug.setVoteCount(bug.getVoteCount() + voteChange);
            bugRepository.save(bug);

            // Ștergem votul
            voteRepository.deleteById(vote.get().getId());

            // Actualizăm scorurile (inversăm efectul votului)
            userScoreService.updateUserScoreForVote(
                    bug.getAuthorId(),
                    userId,
                    !vote.get().isUpvote(), // inversăm votul pentru a-l anula
                    true,
                    false,
                    vote.get().isUpvote()
            );
        }
    }

    public void removeVoteOnComment(String commentId, String userId) {
        moderatorService.checkUserAccess(userId);

        Optional<Vote> vote = voteRepository.findByUserIdAndCommentId(userId, commentId);
        if (vote.isPresent()) {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));

            // Actualizăm numărul de voturi
            int voteChange = vote.get().isUpvote() ? -1 : 1;
            comment.setVoteCount(comment.getVoteCount() + voteChange);
            commentRepository.save(comment);

            // Ștergem votul
            voteRepository.deleteById(vote.get().getId());

            // Actualizăm scorurile
            userScoreService.updateUserScoreForVote(
                    comment.getAuthorId(),
                    userId,
                    !vote.get().isUpvote(),
                    false,
                    false,
                    vote.get().isUpvote()
            );
        }
    }
}