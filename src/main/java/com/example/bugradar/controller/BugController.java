package com.example.bugradar.controller;

import com.example.bugradar.dto.BugDto;
import com.example.bugradar.entity.Bug;
import com.example.bugradar.service.BugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bugs")
public class BugController {

    private final BugService bugService;

    @Autowired
    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @PostMapping
    public ResponseEntity<Bug> createBug(@RequestBody BugDto bugDto, Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        Bug bug = bugService.createBug(bugDto, uid);
        return new ResponseEntity<>(bug, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bug> getBugById(@PathVariable String id) {
        Bug bug = bugService.getBugById(id);
        return ResponseEntity.ok(bug);
    }

    @GetMapping
    public ResponseEntity<List<Bug>> getAllBugs() {
        List<Bug> bugs = bugService.getAllBugs();
        return ResponseEntity.ok(bugs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bug> updateBug(@PathVariable String id, @RequestBody BugDto bugDto, Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        Bug updatedBug = bugService.updateBug(id, bugDto, uid);
        return ResponseEntity.ok(updatedBug);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBug(@PathVariable String id, Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        bugService.deleteBug(id, uid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/filter/tag/{tagId}")
    public ResponseEntity<List<Bug>> filterBugsByTag(@PathVariable String tagId) {
        List<Bug> bugs = bugService.filterBugsByTag(tagId);
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/filter/text/{text}")
    public ResponseEntity<List<Bug>> filterBugsByText(@PathVariable String text) {
        List<Bug> bugs = bugService.filterBugsByText(text);
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/filter/user/{userId}")
    public ResponseEntity<List<Bug>> filterBugsByUser(@PathVariable String userId) {
        List<Bug> bugs = bugService.filterBugsByUser(userId);
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/my-bugs")
    public ResponseEntity<List<Bug>> getMyBugs(Authentication authentication) {
        String uid = (String) authentication.getPrincipal();
        List<Bug> bugs = bugService.getMyBugs(uid);
        return ResponseEntity.ok(bugs);
    }
}