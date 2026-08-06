package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.CustomerFavoriteFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerFavoriteFacilityRepository extends JpaRepository<CustomerFavoriteFacility, Integer> {
    
    Optional<CustomerFavoriteFacility> findByAccountIdAndFacilityId(Integer accountId, Integer facilityId);
    
    boolean existsByAccountIdAndFacilityId(Integer accountId, Integer facilityId);
    
    void deleteByAccountIdAndFacilityId(Integer accountId, Integer facilityId);
    
    @Query("SELECT f.facility.id FROM CustomerFavoriteFacility f WHERE f.account.id = :accountId")
    List<Integer> findFacilityIdsByAccountId(@Param("accountId") Integer accountId);
}
