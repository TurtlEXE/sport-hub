package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.FacilitySport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilitySportRepository extends JpaRepository<FacilitySport, Integer> {
    List<FacilitySport> findByIsActiveTrue();
}
