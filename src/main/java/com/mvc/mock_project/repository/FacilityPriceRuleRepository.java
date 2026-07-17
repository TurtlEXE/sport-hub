package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.FacilityPriceRule;
import com.mvc.mock_project.entities.enums.DayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityPriceRuleRepository extends JpaRepository<FacilityPriceRule, Integer> {
    List<FacilityPriceRule> findByFacilitySportIdAndIsActiveTrue(Integer facilitySportId);
    void deleteByFacilitySport_Id(Integer facilitySportId);
    List<FacilityPriceRule> findByFacilitySportIdAndDayTypeAndIsActiveTrue(Integer facilitySportId, DayType dayType);
    long countByFacilitySportIdAndIsActiveTrue(Integer facilitySportId);
}
