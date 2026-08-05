package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.CustomerFavoriteFacility;
import com.mvc.mock_project.entities.Facility;
import com.mvc.mock_project.exception.FacilityNotFoundException;
import com.mvc.mock_project.repository.AccountRepository;
import com.mvc.mock_project.repository.CustomerFavoriteFacilityRepository;
import com.mvc.mock_project.repository.FacilityRepository;
import com.mvc.mock_project.service.FavoriteFacilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteFacilityServiceImpl implements FavoriteFacilityService {

    private final CustomerFavoriteFacilityRepository favoriteRepository;
    private final FacilityRepository facilityRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public boolean toggleFavorite(Integer accountId, Integer facilityId) {
        log.info("Toggling favorite for accountId: {}, facilityId: {}", accountId, facilityId);
        
        Optional<CustomerFavoriteFacility> existing = favoriteRepository.findByAccountIdAndFacilityId(accountId, facilityId);
        
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            log.info("Removed facilityId: {} from favorites for accountId: {}", facilityId, accountId);
            return false;
        } else {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            
            Facility facility = facilityRepository.findById(facilityId)
                    .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));
            
            CustomerFavoriteFacility newFavorite = CustomerFavoriteFacility.builder()
                    .account(account)
                    .facility(facility)
                    .build();
            
            favoriteRepository.save(newFavorite);
            log.info("Added facilityId: {} to favorites for accountId: {}", facilityId, accountId);
            return true;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getFavoriteFacilityIds(Integer accountId) {
        return favoriteRepository.findFacilityIdsByAccountId(accountId);
    }
}
