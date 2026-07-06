package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SportRepository extends JpaRepository<Sport, Integer> {
    List<Sport> findByIsActiveTrue();
}
