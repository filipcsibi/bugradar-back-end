package com.example.bugradar.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private String id;
    private Bug bug;
    private String bugId; // Referință directă la ID-ul bug-ului
    private String authorId;
    private String text;
    private String imageUrl;
    private String creationDate;
    private int voteCount = 0;

    // În Firestore, LocalDateTime nu este serializat direct, așa că putem adăuga aceste metode helper
    public void setCreationDateString(String dateString) {
        this.creationDate = dateString;
    }

    public String getCreationDateString() {
        return creationDate != null ? creationDate.toString() : null;
    }
}