package com.example.bugradar.service;

import com.example.bugradar.entity.User;
import com.example.bugradar.entity.Vote;
import com.example.bugradar.repository.FirestoreBugRepository;
import com.example.bugradar.repository.FirestoreCommentRepository;
import com.example.bugradar.repository.FirestoreUserRepository;
import com.example.bugradar.repository.FirestoreVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class UserScoreService {

    private final FirestoreUserRepository userRepository;
    private final FirestoreVoteRepository voteRepository;
    private final FirestoreBugRepository bugRepository;
    private final FirestoreCommentRepository commentRepository;

    @Autowired
    public UserScoreService(FirestoreUserRepository userRepository,
                            FirestoreVoteRepository voteRepository,
                            FirestoreBugRepository bugRepository,
                            FirestoreCommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.bugRepository = bugRepository;
        this.commentRepository = commentRepository;
    }

    /**
     * Actualizează scorul unui utilizator bazat pe un vot nou/modificat
     * Apelată din VoteService când se votează
     */
    public void updateUserScoreForVote(String contentAuthorId, String voterId, boolean isUpvote,
                                       boolean isBug, boolean isNewVote, Boolean previousVoteValue) {

        User contentAuthor = userRepository.findById(contentAuthorId)
                .orElseThrow(() -> new RuntimeException("Content author not found"));

        User voter = userRepository.findById(voterId)
                .orElseThrow(() -> new RuntimeException("Voter not found"));

        double authorScoreChange = 0;
        double voterScoreChange = 0;

        if (isNewVote) {
            // Vot nou
            if (isUpvote) {
                // Autorul primește puncte pentru upvote
                authorScoreChange = isBug ? 2.5 : 5.0;
            } else {
                // Autorul pierde puncte pentru downvote
                authorScoreChange = isBug ? -1.5 : -2.5;
                // Votantul pierde puncte pentru că dă downvote
                voterScoreChange = -1.5;
            }
        } else {
            // Schimbare de vot
            if (previousVoteValue != null) {
                // Anulăm efectul votului anterior
                if (previousVoteValue) {
                    // Era upvote, îl anulăm
                    authorScoreChange -= isBug ? 2.5 : 5.0;
                } else {
                    // Era downvote, îl anulăm
                    authorScoreChange += isBug ? 1.5 : 2.5;
                    voterScoreChange += 1.5; // Recuperează penalizarea pentru downvote
                }
            }

            // Aplicăm efectul noului vot
            if (isUpvote) {
                authorScoreChange += isBug ? 2.5 : 5.0;
            } else {
                authorScoreChange -= isBug ? 1.5 : 2.5;
                voterScoreChange -= 1.5;
            }
        }

        // Actualizăm scorurile
        contentAuthor.setScore(contentAuthor.getScore() + authorScoreChange);
        userRepository.save(contentAuthor);

        if (voterScoreChange != 0) {
            voter.setScore(voter.getScore() + voterScoreChange);
            userRepository.save(voter);
        }
    }

    /**
     * Recalculează complet scorul unui utilizator pe baza tuturor voturilor
     * Util pentru migrare sau verificări de consistență
     */
    public void recalculateUserScore(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Folosim AtomicReference pentru a putea modifica în lambda
        AtomicReference<Double> totalScore = new AtomicReference<>(0.0);

        // Calculăm punctele din bug-urile utilizatorului
        bugRepository.findByAuthorId(userId).forEach(bug -> {
            totalScore.updateAndGet(score -> score + (bug.getVoteCount() * 2.5));
        });

        // Calculăm punctele din comentariile utilizatorului
        commentRepository.findByAuthorId(userId).forEach(comment -> {
            totalScore.updateAndGet(score -> score + (comment.getVoteCount() * 5.0));
        });

        // Calculăm penalizările pentru downvote-urile date de utilizator
        List<Vote> userDownvotes = voteRepository.findAll().stream()
                .filter(vote -> vote.getUserId().equals(userId) && !vote.isUpvote())
                .toList();

        // Aplicăm penalizarea pentru downvote-uri
        double downvotePenalty = userDownvotes.size() * 1.5;
        totalScore.updateAndGet(score -> score - downvotePenalty);

        user.setScore(totalScore.get());
        userRepository.save(user);
    }

    /**
     * Recalculează scorurile tuturor utilizatorilor
     */
    public void recalculateAllUserScores() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            recalculateUserScore(user.getUid());
        }
    }
}