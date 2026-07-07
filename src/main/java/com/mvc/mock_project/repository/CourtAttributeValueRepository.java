package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.CourtAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtAttributeValueRepository extends JpaRepository<CourtAttributeValue, Integer> {
    List<CourtAttributeValue> findByCourt_Id(Integer courtId);
    void deleteByCourt_Id(Integer courtId);
}
