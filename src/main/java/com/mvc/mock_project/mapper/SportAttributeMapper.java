package com.mvc.mock_project.mapper;

import com.mvc.mock_project.dto.request.SportAttributeRequest;
import com.mvc.mock_project.dto.response.SportAttributeDTO;
import com.mvc.mock_project.entities.Sport;
import com.mvc.mock_project.entities.SportAttribute;
import org.springframework.stereotype.Component;

@Component
public class SportAttributeMapper {

    public SportAttribute toEntity(SportAttributeRequest request, Sport sport) {
        if (request == null) return null;
        
        return SportAttribute.builder()
                .sport(sport)
                .attributeCode(request.getAttributeCode())
                .attributeName(request.getAttributeName())
                .dataType(request.getDataType())
                .optionsJson(request.getOptionsJson())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : false)
                .isActive(true)
                .build();
    }

    public void updateEntityFromRequest(SportAttribute attribute, SportAttributeRequest request) {
        if (request == null || attribute == null) return;
        
        attribute.setAttributeName(request.getAttributeName());
        attribute.setDataType(request.getDataType());
        attribute.setOptionsJson(request.getOptionsJson());
        if (request.getIsRequired() != null) {
            attribute.setIsRequired(request.getIsRequired());
        }
    }

    public SportAttributeDTO toDto(SportAttribute attribute) {
        if (attribute == null) return null;
        
        return SportAttributeDTO.builder()
                .attributeId(attribute.getId())
                .attributeCode(attribute.getAttributeCode())
                .attributeName(attribute.getAttributeName())
                .dataType(attribute.getDataType())
                .optionsJson(attribute.getOptionsJson())
                .isRequired(attribute.getIsRequired())
                .isActive(attribute.getIsActive())
                .build();
    }
}
