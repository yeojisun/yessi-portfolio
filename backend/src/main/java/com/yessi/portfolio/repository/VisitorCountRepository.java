package com.yessi.portfolio.repository;

import com.yessi.portfolio.model.VisitorCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface VisitorCountRepository extends JpaRepository<VisitorCount, LocalDate> {

    @Query("SELECT COALESCE(SUM(v.count), 0) FROM VisitorCount v")
    long sumAllCounts();
}
