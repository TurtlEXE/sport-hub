package com.mvc.mock_project.mapper;

import com.mvc.mock_project.dto.request.SportRequest;
import com.mvc.mock_project.dto.response.SportDTO;
import com.mvc.mock_project.entities.Sport;
import org.springframework.stereotype.Component;

@Component
public class SportMapper {

    public Sport toEntity(SportRequest request) {
        if (request == null) return null;
        
        return Sport.builder()
                .sportCode(request.getSportCode())
                .sportName(request.getSportName())
                .iconPath(request.getIconPath())
                .defaultMinDurationMinutes(request.getDefaultMinDurationMinutes())
                .defaultSlotStepMinutes(request.getDefaultSlotStepMinutes())
                .isActive(true)
                .build();
    }

    public void updateEntityFromRequest(Sport sport, SportRequest request) {
        if (request == null || sport == null) return;
        
        sport.setSportName(request.getSportName());
        sport.setIconPath(request.getIconPath());
        sport.setDefaultMinDurationMinutes(request.getDefaultMinDurationMinutes());
        sport.setDefaultSlotStepMinutes(request.getDefaultSlotStepMinutes());
    }

    public SportDTO toDto(Sport sport) {
        if (sport == null) return null;
        
        return SportDTO.builder()
                .sportId(sport.getId())
                .sportCode(sport.getSportCode())
                .sportName(sport.getSportName())
                .iconPath(sport.getIconPath())
                .defaultMinDurationMinutes(sport.getDefaultMinDurationMinutes())
                .defaultSlotStepMinutes(sport.getDefaultSlotStepMinutes())
                .isActive(sport.getIsActive())
                .build();
    }
}
