package com.example.bugradar.repository;

import com.example.bugradar.entity.Vote;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreVoteRepository {

    private final Firestore firestore;
    private final CollectionReference votesCollection;

    @Autowired
    public FirestoreVoteRepository(Firestore firestore) {
        this.firestore = firestore;
        this.votesCollection = firestore.collection("votes");
    }

    public Vote save(Vote vote) {
        try {
            // Dacă votul nu are ID, generăm unul
            if (vote.getId() == null || vote.getId().isEmpty()) {
                DocumentReference docRef = votesCollection.document();
                vote.setId(docRef.getId());
                docRef.set(vote).get();
            } else {
                votesCollection.document(vote.getId()).set(vote).get();
            }
            return vote;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving vote", e);
        }
    }

    public Optional<Vote> findById(String id) {
        try {
            DocumentSnapshot document = votesCollection.document(id).get().get();
            if (document.exists()) {
                return Optional.of(document.toObject(Vote.class));
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding vote", e);
        }
    }

    public List<Vote> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = votesCollection.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Vote> votes = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                votes.add(document.toObject(Vote.class));
            }
            return votes;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding all votes", e);
        }
    }

    public void deleteById(String id) {
        try {
            votesCollection.document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting vote", e);
        }
    }

    public Optional<Vote> findByUserIdAndBugId(String userId, String bugId) {
        try {
            ApiFuture<QuerySnapshot> future = votesCollection
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("bugId", bugId)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                return Optional.of(documents.get(0).toObject(Vote.class));
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding vote by user and bug", e);
        }
    }

    public Optional<Vote> findByUserIdAndCommentId(String userId, String commentId) {
        try {
            ApiFuture<QuerySnapshot> future = votesCollection
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("commentId", commentId)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                return Optional.of(documents.get(0).toObject(Vote.class));
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding vote by user and comment", e);
        }
    }
}