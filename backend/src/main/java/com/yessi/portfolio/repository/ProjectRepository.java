package com.yessi.portfolio.repository;

import com.yessi.portfolio.model.ProjectItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectItem, Long> {
}
