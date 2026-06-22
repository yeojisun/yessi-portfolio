package com.yessi.portfolio.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "visitor_counts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitorCount {

    @Id
    private LocalDate visitDate;

    @Column(nullable = false)
    private long count;
}
