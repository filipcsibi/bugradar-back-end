package com.example.bugradar.repository;

import com.example.bugradar.entity.Bug;
import com.example.bugradar.entity.Tag;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreBugRepository {

    private final Firestore firestore;
    private final CollectionReference bugsCollection;

    @Autowired
    public FirestoreBugRepository(Firestore firestore) {
        this.firestore = firestore;
        this.bugsCollection = firestore.collection("bugs");
    }

    public Bug save(Bug bug) {
        try {
            // Dacă bug-ul nu are ID, generăm unul
            if (bug.getId() == null || bug.getId().isEmpty()) {
                DocumentReference docRef = bugsCollection.document();
                bug.setId(docRef.getId());
                docRef.set(bug).get();
            } else {
                bugsCollection.document(bug.getId()).set(bug).get();
            }
            return bug;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving bug", e);
        }
    }

    public Optional<Bug> findById(String id) {
        try {
            DocumentSnapshot document = bugsCollection.document(id).get().get();
            if (document.exists()) {
                Bug bug = document.toObject(Bug.class);
                return Optional.ofNullable(bug);
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding bug", e);
        }
    }

    public List<Bug> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = bugsCollection.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Bug> bugs = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Bug bug = document.toObject(Bug.class);
                bugs.add(bug);
            }
            return bugs;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding all bugs", e);
        }
    }

    public List<Bug> findAllByOrderByCreationDateDesc() {
        List<Bug> bugs = findAll();
        // Folosim getCreationDate (generat de Lombok), nu getCreationDateTime
        Collections.sort(bugs, (bug1, bug2) -> {
            String date1 = bug1.getCreationDate();
            String date2 = bug2.getCreationDate();

            if (date1 == null) date1 = "";
            if (date2 == null) date2 = "";

            return date2.compareTo(date1);
        });
        return bugs;
    }

    public void deleteById(String id) {
        try {
            bugsCollection.document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting bug", e);
        }
    }

    public List<Bug> findByAuthorId(String authorId) {
        try {
            ApiFuture<QuerySnapshot> future = bugsCollection.whereEqualTo("authorId", authorId).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Bug> bugs = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Bug bug = document.toObject(Bug.class);
                bugs.add(bug);
            }
            return bugs;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding bugs by author", e);
        }
    }

    public List<Bug> findByTitleContainingIgnoreCase(String text) {
        try {
            // Firestore nu are operații de căutare text direct, așa că facem o căutare simplificată
            ApiFuture<QuerySnapshot> future = bugsCollection.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Bug> bugs = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Bug bug = document.toObject(Bug.class);
                if (bug.getTitle().toLowerCase().contains(text.toLowerCase())) {
                    bugs.add(bug);
                }
            }
            return bugs;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error finding bugs by title", e);
        }
    }

    public List<Bug> findByTagsContaining(Tag tag) {
        try {
            // În Firestore, căutarea într-o colecție de obiecte e mai complexă
            List<Bug> allBugs = findAll();
            List<Bug> result = new ArrayList<>();
            for (Bug bug : allBugs) {
                if (bug.getTags() != null && bug.getTags().stream().anyMatch(t -> t.getId().equals(tag.getId()))) {
                    result.add(bug);
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error finding bugs by tag", e);
        }
    }
}