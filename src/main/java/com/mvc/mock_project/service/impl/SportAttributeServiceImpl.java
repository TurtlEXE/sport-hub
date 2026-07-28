package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.SportAttributeRequest;
import com.mvc.mock_project.dto.response.SportAttributeDTO;
import com.mvc.mock_project.entities.Sport;
import com.mvc.mock_project.entities.SportAttribute;
import com.mvc.mock_project.exception.DuplicateCodeException;
import com.mvc.mock_project.mapper.SportAttributeMapper;
import com.mvc.mock_project.repository.SportAttributeRepository;
import com.mvc.mock_project.repository.SportRepository;
import com.mvc.mock_project.service.SportAttributeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SportAttributeServiceImpl implements SportAttributeService {

    private final SportAttributeRepository sportAttributeRepository;
    private final SportRepository sportRepository;
    private final SportAttributeMapper sportAttributeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SportAttributeDTO> getAttributesBySportId(Integer sportId) {
        return sportAttributeRepository.findBySport_Id(sportId).stream()
                .map(sportAttributeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SportAttributeDTO createAttribute(Integer sportId, SportAttributeRequest request) {
        Sport sport = sportRepository.findById(sportId)
                .orElseThrow(() -> new RuntimeException("Sport not found"));

        if (sportAttributeRepository.existsByAttributeCodeAndSport_Id(request.getAttributeCode(), sportId)) {
            throw new DuplicateCodeException("Attribute code already exists for this sport: " + request.getAttributeCode());
        }

        SportAttribute attribute = sportAttributeMapper.toEntity(request, sport);
        SportAttribute saved = sportAttributeRepository.save(attribute);
        log.info("Created new attribute {} for sport {}", saved.getAttributeCode(), sportId);
        return sportAttributeMapper.toDto(saved);
    }

    @Override
    @Transactional
    public SportAttributeDTO updateAttribute(Integer sportId, Integer attributeId, SportAttributeRequest request) {
        SportAttribute attribute = sportAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new RuntimeException("Sport Attribute not found"));

        if (!attribute.getSport().getId().equals(sportId)) {
            throw new RuntimeException("Attribute does not belong to the specified sport");
        }

        if (!attribute.getAttributeCode().equals(request.getAttributeCode())) {
            if (sportAttributeRepository.existsByAttributeCodeAndSport_Id(request.getAttributeCode(), sportId)) {
                throw new DuplicateCodeException("Attribute code already exists for this sport: " + request.getAttributeCode());
            }
            attribute.setAttributeCode(request.getAttributeCode());
        }

        sportAttributeMapper.updateEntityFromRequest(attribute, request);
        SportAttribute saved = sportAttributeRepository.save(attribute);
        log.info("Updated attribute {} for sport {}", saved.getAttributeCode(), sportId);
        return sportAttributeMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteAttribute(Integer sportId, Integer attributeId) {
        SportAttribute attribute = sportAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new RuntimeException("Sport Attribute not found"));

        if (!attribute.getSport().getId().equals(sportId)) {
            throw new RuntimeException("Attribute does not belong to the specified sport");
        }

        // Soft delete
        attribute.setIsActive(false);
        sportAttributeRepository.save(attribute);
        log.info("Soft deleted attribute {} for sport {}", attribute.getAttributeCode(), sportId);
    }
}
