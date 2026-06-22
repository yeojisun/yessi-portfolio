package com.yessi.portfolio.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    private int stargazersCount;

    private int forksCount;

    private String language;

    private String htmlUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_topics", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "topic")
    private List<String> topics;

    private LocalDateTime updatedAt;
}
