package com.yessi.portfolio.controller;

import com.yessi.portfolio.model.ContactMessage;
import com.yessi.portfolio.model.ProjectItem;
import com.yessi.portfolio.repository.ContactMessageRepository;
import com.yessi.portfolio.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortfolioController {

    private final ProjectRepository projectRepository;
    private final ContactMessageRepository contactMessageRepository;

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectItem>> getAllProjects() {
        return ResponseEntity.ok(projectRepository.findAll());
    }

    @GetMapping("/messages")
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        return ResponseEntity.ok(contactMessageRepository.findAll());
    }

    @PostMapping("/messages")
    public ResponseEntity<ContactMessage> createMessage(@RequestBody ContactMessage message) {
        ContactMessage saved = contactMessageRepository.save(message);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}
