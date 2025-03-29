package com.example.bugradar.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bug {
    private String id;
    private String authorId;
    private String title;
    private String description;
    private String creationDate;
    private String imageUrl;
    private BugStatus status = BugStatus.RECEIVED;
    private List<Tag> tags = new ArrayList<>();
    private int voteCount = 0;

    // În Firestore, LocalDateTime nu este serializat direct, așa că putem adăuga aceste metode helper
    public void setCreationDateTime(LocalDateTime dateTime) {
        this.creationDate = dateTime.toString();
    }
}