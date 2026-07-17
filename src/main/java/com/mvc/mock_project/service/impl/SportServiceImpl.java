package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.response.SportAttributeDTO;
import com.mvc.mock_project.dto.response.SportDTO;
import com.mvc.mock_project.repository.SportAttributeRepository;
import com.mvc.mock_project.repository.SportRepository;
import com.mvc.mock_project.service.SportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SportServiceImpl implements SportService {

    private final SportRepository sportRepository;
    private final SportAttributeRepository sportAttributeRepository;

    @Override
    public List<SportDTO> getAllActiveSports() {
        return sportRepository.findByIsActiveTrue().stream()
                .map(sport -> SportDTO.builder()
                        .sportId(sport.getId())
                        .sportCode(sport.getSportCode())
                        .sportName(sport.getSportName())
                        .defaultMinDurationMinutes(sport.getDefaultMinDurationMinutes())
                        .defaultSlotStepMinutes(sport.getDefaultSlotStepMinutes())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SportAttributeDTO> getSportAttributes(Integer sportId) {
        return sportAttributeRepository.findBySport_Id(sportId).stream()
                .map(attr -> SportAttributeDTO.builder()
                        .attributeId(attr.getId())
                        .attributeCode(attr.getAttributeCode())
                        .attributeName(attr.getAttributeName())
                        .dataType(attr.getDataType())
                        .optionsJson(attr.getOptionsJson())
                        .isRequired(attr.getIsRequired())
                        .build())
                .collect(Collectors.toList());
    }
}
