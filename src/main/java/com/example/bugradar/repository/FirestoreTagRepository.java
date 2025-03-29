package com.example.bugradar.repository;

import com.example.bugradar.entity.Tag;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreTagRepository {

    private final Firestore firestore;
    private final CollectionReference tagsCollection;

    @Autowired
    public FirestoreTagRepository(Firestore firestore) {
        this.firestore = firestore;
        this.tagsCollection = firestore.collection("tags");
    }

    public Tag save(Tag tag) {
        try {
            // Dacă tag-ul nu are ID, generăm unul
            if (tag.getId() == null || tag.getId().isEmpty()) {
                DocumentReference docRef = tagsCollection.document();
                tag.setId(docRef.getId());
                docRef.set(tag).get();
            } else {
                tagsCollection.document(tag.getId()).set(tag).get();
            }
            return tag;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving tag", e);
        }
    }

    public Optional<Tag> findById(String id) {
        try {
            DocumentSnapshot document = tagsCollection.document(id).get().get();
            if (document.exists()) {
                return Optional.of(document.toObject(Tag.class));
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding tag", e);
        }
    }

    public List<Tag> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = tagsCollection.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Tag> tags = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                tags.add(document.toObject(Tag.class));
            }
            return tags;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding all tags", e);
        }
    }

    public void deleteById(String id) {
        try {
            tagsCollection.document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting tag", e);
        }
    }

    public Optional<Tag> findByName(String name) {
        try {
            ApiFuture<QuerySnapshot> future = tagsCollection.whereEqualTo("name", name).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            if (!documents.isEmpty()) {
                return Optional.of(documents.get(0).toObject(Tag.class));
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding tag by name", e);
        }
    }
}