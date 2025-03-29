package com.example.bugradar.repository;


import com.example.bugradar.entity.Comment;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class FirestoreCommentRepository {

    private final Firestore firestore;
    private final CollectionReference commentsCollection;

    @Autowired
    public FirestoreCommentRepository(Firestore firestore) {
        this.firestore = firestore;
        this.commentsCollection = firestore.collection("comments");
    }

    public Comment save(Comment comment) {
        try {
            // Salvăm creationDate ca string
            if (comment.getCreationDate() != null) {
                comment.setCreationDateString(comment.getCreationDate().toString());
            }

            // Setăm explicit bugId din obiectul bug
            if (comment.getBug() != null && comment.getBug().getId() != null) {
                comment.setBugId(comment.getBug().getId());
            }

            // Dacă comentariul nu are ID, generăm unul
            if (comment.getId() == null || comment.getId().isEmpty()) {
                DocumentReference docRef = commentsCollection.document();
                comment.setId(docRef.getId());
                docRef.set(comment).get();
            } else {
                commentsCollection.document(comment.getId()).set(comment).get();
            }
            return comment;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving comment", e);
        }
    }

    public Optional<Comment> findById(String id) {
        try {
            DocumentSnapshot document = commentsCollection.document(id).get().get();
            if (document.exists()) {
                Comment comment = document.toObject(Comment.class);
                // Convertim creationDateString înapoi la LocalDateTime
                if (comment != null && comment.getCreationDateString() != null) {
                    comment.setCreationDate(LocalDateTime.parse(comment.getCreationDateString()));
                }
                return Optional.ofNullable(comment);
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding comment", e);
        }
    }

    public List<Comment> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = commentsCollection.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Comment> comments = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Comment comment = document.toObject(Comment.class);
                // Convertim creationDateString înapoi la LocalDateTime
                if (comment.getCreationDateString() != null) {
                    comment.setCreationDate(LocalDateTime.parse(comment.getCreationDateString()));
                }
                comments.add(comment);
            }
            return comments;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding all comments", e);
        }
    }

    public void deleteById(String id) {
        try {
            commentsCollection.document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting comment", e);
        }
    }

    public List<Comment> findByBugIdOrderByVoteCountDesc(String bugId) {
        try {
            ApiFuture<QuerySnapshot> future = commentsCollection.whereEqualTo("bugId", bugId).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Comment> comments = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Comment comment = document.toObject(Comment.class);
                // Convertim creationDateString înapoi la LocalDateTime
                if (comment.getCreationDateString() != null) {
                    comment.setCreationDate(LocalDateTime.parse(comment.getCreationDateString()));
                }
                comments.add(comment);
            }
            return comments.stream()
                    .sorted(Comparator.comparing(Comment::getVoteCount).reversed())
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding comments by bug id", e);
        }
    }

    public List<Comment> findByAuthorId(String authorId) {
        try {
            ApiFuture<QuerySnapshot> future = commentsCollection.whereEqualTo("authorId", authorId).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Comment> comments = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Comment comment = document.toObject(Comment.class);
                // Convertim creationDateString înapoi la LocalDateTime
                if (comment.getCreationDateString() != null) {
                    comment.setCreationDate(LocalDateTime.parse(comment.getCreationDateString()));
                }
                comments.add(comment);
            }
            return comments;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding comments by author", e);
        }
    }
}