package com.yessi.portfolio.controller;

import com.yessi.portfolio.model.ContactMessage;
import com.yessi.portfolio.model.ProjectItem;
import com.yessi.portfolio.model.VisitorCount;
import com.yessi.portfolio.repository.ContactMessageRepository;
import com.yessi.portfolio.repository.ProjectRepository;
import com.yessi.portfolio.repository.VisitorCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortfolioController {

    private final ProjectRepository projectRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final VisitorCountRepository visitorCountRepository;

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

    @PostMapping("/visits")
    public ResponseEntity<Map<String, Long>> recordVisit() {
        LocalDate today = LocalDate.now();
        VisitorCount visitorCount = visitorCountRepository.findById(today)
                .orElse(VisitorCount.builder().visitDate(today).count(0L).build());
        visitorCount.setCount(visitorCount.getCount() + 1);
        visitorCountRepository.save(visitorCount);

        Map<String, Long> stats = new HashMap<>();
        stats.put("today", visitorCount.getCount());
        stats.put("total", visitorCountRepository.sumAllCounts());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/visits")
    public ResponseEntity<Map<String, Long>> getVisits() {
        LocalDate today = LocalDate.now();
        long todayCount = visitorCountRepository.findById(today)
                .map(VisitorCount::getCount)
                .orElse(0L);

        Map<String, Long> stats = new HashMap<>();
        stats.put("today", todayCount);
        stats.put("total", visitorCountRepository.sumAllCounts());
        return ResponseEntity.ok(stats);
    }
}
