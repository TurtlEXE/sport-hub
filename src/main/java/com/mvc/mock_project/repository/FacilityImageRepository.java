package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.FacilityImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityImageRepository extends JpaRepository<FacilityImage, Integer> {
    List<FacilityImage> findByFacilityId(Integer facilityId);
    FacilityImage findFirstByFacilityIdAndIsThumbnailTrue(Integer facilityId);
    boolean existsByFacilityId(Integer facilityId);
}
