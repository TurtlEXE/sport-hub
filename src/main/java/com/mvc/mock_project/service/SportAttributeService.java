package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.SportAttributeRequest;
import com.mvc.mock_project.dto.response.SportAttributeDTO;

import java.util.List;

public interface SportAttributeService {
    List<SportAttributeDTO> getAttributesBySportId(Integer sportId);
    SportAttributeDTO createAttribute(Integer sportId, SportAttributeRequest request);
    SportAttributeDTO updateAttribute(Integer sportId, Integer attributeId, SportAttributeRequest request);
    void deleteAttribute(Integer sportId, Integer attributeId);
}
