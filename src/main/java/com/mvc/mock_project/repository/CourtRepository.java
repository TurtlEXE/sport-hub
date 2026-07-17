package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Court;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourtRepository extends JpaRepository<Court, Integer> {
    List<Court> findByFacilitySport_IdAndIsActiveTrue(Integer facilitySportId);
    Optional<Court> findByIdAndFacilitySport_Facility_Owner_Id(Integer courtId, Integer ownerAccountId);

    @org.springframework.data.jpa.repository.Query("SELECT fs.sport.sportName, COUNT(c.id) FROM Court c JOIN c.facilitySport fs JOIN fs.facility f WHERE c.isActive = true AND f.isActive = true AND fs.isActive = true GROUP BY fs.sport.sportName")
    List<Object[]> countCourtsBySport();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(c.id) FROM Court c JOIN c.facilitySport fs JOIN fs.facility f WHERE c.isActive = true AND f.isActive = true AND fs.isActive = true")
    long countTotalActiveCourts();
}
