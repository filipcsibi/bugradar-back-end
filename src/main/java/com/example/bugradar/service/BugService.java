package com.example.bugradar.service;

import com.example.bugradar.dto.BugDto;
import com.example.bugradar.entity.Bug;
import com.example.bugradar.entity.BugStatus;
import com.example.bugradar.entity.Tag;
import com.example.bugradar.repository.FirestoreBugRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BugService {

    private final FirestoreBugRepository bugRepository;
    private final TagService tagService;

    @Autowired
    public BugService(FirestoreBugRepository bugRepository, TagService tagService) {
        this.bugRepository = bugRepository;
        this.tagService = tagService;
    }

    public Bug createBug(BugDto bugDto, String authorId) {
        Bug bug = new Bug();
        bug.setAuthorId(authorId);
        bug.setTitle(bugDto.getTitle());
        bug.setDescription(bugDto.getDescription());
        bug.setImageUrl(bugDto.getImageUrl());
        bug.setCreationDateTime(LocalDateTime.now());
        bug.setStatus(BugStatus.RECEIVED);

        // Procesare tag-uri cu List în loc de Set
        List<Tag> tags = new ArrayList<>();
        if (bugDto.getTagNames() != null && !bugDto.getTagNames().isEmpty()) {
            tags = bugDto.getTagNames().stream()
                    .map(tagService::findOrCreateTag)
                    .collect(Collectors.toList());  // Folosim toList în loc de toSet
        }
        bug.setTags(tags);

        return bugRepository.save(bug);
    }

    public Bug getBugById(String id) {
        return bugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bug not found"));
    }

    public List<Bug> getAllBugs() {
        return bugRepository.findAllByOrderByCreationDateDesc();
    }

    public Bug updateBug(String id, BugDto bugDto, String currentUserId) {
        Bug bug = getBugById(id);

        // Verificăm dacă utilizatorul curent este autorul
        if (!bug.getAuthorId().equals(currentUserId)) {
            throw new RuntimeException("Not authorized to update this bug");
        }

        bug.setTitle(bugDto.getTitle());
        bug.setDescription(bugDto.getDescription());
        bug.setImageUrl(bugDto.getImageUrl());

        // Procesare tag-uri cu List în loc de Set
        if (bugDto.getTagNames() != null && !bugDto.getTagNames().isEmpty()) {
            List<Tag> tags = bugDto.getTagNames().stream()
                    .map(tagService::findOrCreateTag)
                    .collect(Collectors.toList());  // Folosim toList în loc de toSet
            bug.setTags(tags);
        }

        return bugRepository.save(bug);
    }
    public void deleteBug(String id, String currentUserId) {
        Bug bug = getBugById(id);

        // Verificăm dacă utilizatorul curent este autorul
        if (!bug.getAuthorId().equals(currentUserId)) {
            throw new RuntimeException("Not authorized to delete this bug");
        }

        bugRepository.deleteById(id);
    }

    public List<Bug> filterBugsByTag(String tagId) {
        Tag tag = tagService.getTagById(tagId);
        return bugRepository.findByTagsContaining(tag);
    }

    public List<Bug> filterBugsByText(String text) {
        return bugRepository.findByTitleContainingIgnoreCase(text);
    }

    public List<Bug> filterBugsByUser(String userId) {
        return bugRepository.findByAuthorId(userId);
    }

    public List<Bug> getMyBugs(String currentUserId) {
        return bugRepository.findByAuthorId(currentUserId);
    }
}