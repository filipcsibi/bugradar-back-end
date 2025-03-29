package com.example.bugradar.service;

import com.example.bugradar.entity.Tag;
import com.example.bugradar.repository.FirestoreTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TagService {

    private final FirestoreTagRepository tagRepository;

    @Autowired
    public TagService(FirestoreTagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Tag findOrCreateTag(String tagName) {
        Optional<Tag> existingTag = tagRepository.findByName(tagName);
        if (existingTag.isPresent()) {
            return existingTag.get();
        } else {
            Tag newTag = new Tag();
            newTag.setName(tagName);
            return tagRepository.save(newTag);
        }
    }

    public Tag getTagById(String id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
    }
}