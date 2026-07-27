package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.SportRequest;
import com.mvc.mock_project.dto.response.SportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SportService {
    // Public method
    List<SportDTO> getAllActiveSports();

    // Admin methods
    Page<SportDTO> getAllSports(Boolean isActive, Pageable pageable);
    SportDTO getSportById(Integer id);
    SportDTO createSport(SportRequest request);
    SportDTO updateSport(Integer id, SportRequest request);
    void toggleActiveStatus(Integer id);
}
