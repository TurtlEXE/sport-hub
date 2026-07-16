package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Integer> {
    List<Court> findByFacilitySportIdAndIsActiveTrue(Integer facilitySportId);
}
