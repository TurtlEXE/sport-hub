package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.FacilitySport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilitySportRepository extends JpaRepository<FacilitySport, Integer> {
    @Query("SELECT fs FROM FacilitySport fs WHERE fs.facility.id = :facilityId AND (fs.isActive IS NULL OR fs.isActive = true)")
    List<FacilitySport> findByFacilityIdAndIsActiveTrue(@Param("facilityId") Integer facilityId);

    Optional<FacilitySport> findByIdAndFacility_Owner_Id(Integer facilitySportId, Integer ownerAccountId);
    boolean existsByFacility_IdAndSport_Id(Integer facilityId, Integer sportId);
    List<FacilitySport> findByIsActiveTrue();

    @Query("SELECT fs FROM FacilitySport fs WHERE fs.facility.owner.id = :ownerId AND (fs.isActive IS NULL OR fs.isActive = true)")
    List<FacilitySport> findByFacility_Owner_IdAndIsActiveTrue(@Param("ownerId") Integer ownerId);

    List<FacilitySport> findByFacility_Id(Integer facilityId);
}
