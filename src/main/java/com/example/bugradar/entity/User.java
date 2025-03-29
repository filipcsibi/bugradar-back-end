package com.example.bugradar.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String uid;
    private String username;
    private String email;
    private double score = 0;
    private boolean isBanned = false;
    private boolean isModerator = false;
}