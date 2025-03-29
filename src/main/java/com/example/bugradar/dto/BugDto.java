package com.example.bugradar.dto;

import lombok.Data;

import java.util.Set;

@Data
public class BugDto {
    private String title;
    private String description;
    private String imageUrl;
    private Set<String> tagNames;
}