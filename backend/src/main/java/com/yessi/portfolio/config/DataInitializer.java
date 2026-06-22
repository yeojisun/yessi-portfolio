package com.yessi.portfolio.config;

import com.yessi.portfolio.model.ProjectItem;
import com.yessi.portfolio.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProjectRepository projectRepository;

    @Override
    public void run(String... args) throws Exception {
        if (projectRepository.count() == 0) {
            ProjectItem p1 = ProjectItem.builder()
                    .name("toy-store-backend")
                    .description("Spring Boot, Spring Security, JPA, and PostgreSQL-based API service for managing and processing orders in a toy store application.")
                    .stargazersCount(8)
                    .forksCount(2)
                    .language("Java")
                    .htmlUrl("https://github.com/yeojisun/toy-store-backend")
                    .topics(Arrays.asList("spring-boot", "jpa", "postgresql", "java", "security"))
                    .updatedAt(LocalDateTime.now().minusDays(7))
                    .build();

            ProjectItem p2 = ProjectItem.builder()
                    .name("realtime-chat-app")
                    .description("A responsive real-time messaging application with Vue 3, CSS Grid/Flexbox, WebSocket, and Redis caching layers.")
                    .stargazersCount(14)
                    .forksCount(4)
                    .language("Vue")
                    .htmlUrl("https://github.com/yeojisun/realtime-chat-app")
                    .topics(Arrays.asList("vue", "javascript", "websocket", "redis", "glassmorphism"))
                    .updatedAt(LocalDateTime.now().minusDays(2))
                    .build();

            ProjectItem p3 = ProjectItem.builder()
                    .name("yessi-portfolio")
                    .description("My personal developer portfolio containing active GitHub repositories, dynamic filters, and clean responsive CSS.")
                    .stargazersCount(5)
                    .forksCount(1)
                    .language("Vue")
                    .htmlUrl("https://github.com/yeojisun/yessi-portfolio")
                    .topics(Arrays.asList("vue", "java", "vite", "netlify-forms", "css-aurora"))
                    .updatedAt(LocalDateTime.now())
                    .build();

            projectRepository.saveAll(Arrays.asList(p1, p2, p3));
            System.out.println("Data Initialized with mock portfolio projects!");
        }
    }
}
