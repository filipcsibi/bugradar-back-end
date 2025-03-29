package com.example.bugradar.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vote {
    private String id;
    private String userId;
    private String bugId;     // Pentru voturile pe bug-uri
    private String commentId;  // Pentru voturile pe comentarii
    private boolean isUpvote;
}