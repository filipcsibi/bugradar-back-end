package com.example.bugradar.repository;

import com.example.bugradar.entity.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreUserRepository {

    private final Firestore firestore;
    private final CollectionReference usersCollection;

    @Autowired
    public FirestoreUserRepository(Firestore firestore) {
        this.firestore = firestore;
        this.usersCollection = firestore.collection("users");
    }

    public User save(User user) {
        try {
            usersCollection.document(user.getUid()).set(user).get();
            return user;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    public Optional<User> findById(String uid) {
        try {
            DocumentSnapshot document = usersCollection.document(uid).get().get();
            if (document.exists()) {
                User user = document.toObject(User.class);
                return Optional.ofNullable(user);
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding user", e);
        }
    }

    public List<User> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = usersCollection.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                users.add(document.toObject(User.class));
            }
            return users;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding all users", e);
        }
    }

    public void deleteById(String uid) {
        try {
            usersCollection.document(uid).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            ApiFuture<QuerySnapshot> future = usersCollection.whereEqualTo("email", email).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                return Optional.of(documents.get(0).toObject(User.class));
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
    }
}