package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.FacilitySport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilitySportRepository extends JpaRepository<FacilitySport, Integer> {
    List<FacilitySport> findByFacilityIdAndIsActiveTrue(Integer facilityId);
    Optional<FacilitySport> findByIdAndFacility_Owner_Id(Integer facilitySportId, Integer ownerAccountId);
    boolean existsByFacility_IdAndSport_Id(Integer facilityId, Integer sportId);
    List<FacilitySport> findByIsActiveTrue();
}
