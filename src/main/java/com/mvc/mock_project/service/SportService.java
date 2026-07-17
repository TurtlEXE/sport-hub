package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.response.SportAttributeDTO;
import com.mvc.mock_project.dto.response.SportDTO;
import java.util.List;

public interface SportService {
    List<SportDTO> getAllActiveSports();
    List<SportAttributeDTO> getSportAttributes(Integer sportId);
}
