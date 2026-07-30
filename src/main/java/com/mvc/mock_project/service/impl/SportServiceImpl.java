package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.SportRequest;
import com.mvc.mock_project.dto.response.SportDTO;
import com.mvc.mock_project.entities.Sport;
import com.mvc.mock_project.exception.DuplicateCodeException;
import com.mvc.mock_project.mapper.SportMapper;
import com.mvc.mock_project.repository.SportRepository;
import com.mvc.mock_project.service.SportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SportServiceImpl implements SportService {

    private final SportRepository sportRepository;
    private final SportMapper sportMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SportDTO> getAllActiveSports() {
        return sportRepository.findByIsActiveTrue().stream()
                .map(sportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SportDTO> getAllSports(Boolean isActive, Pageable pageable) {
        if (isActive != null) {
            return sportRepository.findByIsActive(isActive, pageable).map(sportMapper::toDto);
        }
        return sportRepository.findAll(pageable).map(sportMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public SportDTO getSportById(Integer id) {
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sport not found"));
        return sportMapper.toDto(sport);
    }

    @Override
    @Transactional
    public SportDTO createSport(SportRequest request) {
        if (sportRepository.existsBySportCode(request.getSportCode())) {
            throw new DuplicateCodeException("Sport code already exists: " + request.getSportCode());
        }
        Sport sport = sportMapper.toEntity(request);
        Sport saved = sportRepository.save(sport);
        log.info("Created new sport: {}", saved.getSportCode());
        return sportMapper.toDto(saved);
    }

    @Override
    @Transactional
    public SportDTO updateSport(Integer id, SportRequest request) {
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sport not found"));

        if (!sport.getSportCode().equals(request.getSportCode())) {
            if (sportRepository.existsBySportCode(request.getSportCode())) {
                throw new DuplicateCodeException("Sport code already exists: " + request.getSportCode());
            }
            sport.setSportCode(request.getSportCode());
        }

        sportMapper.updateEntityFromRequest(sport, request);
        Sport saved = sportRepository.save(sport);
        log.info("Updated sport: {}", saved.getSportCode());
        return sportMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void toggleActiveStatus(Integer id) {
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sport not found"));
        sport.setIsActive(!sport.getIsActive());
        sportRepository.save(sport);
        log.info("Toggled active status for sport {} to {}", sport.getSportCode(), sport.getIsActive());
    }
}
