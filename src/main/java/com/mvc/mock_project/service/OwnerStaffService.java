package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.StaffFormDTO;
import com.mvc.mock_project.dto.response.StaffResponseDTO;

import java.util.List;

public interface OwnerStaffService {

    List<StaffResponseDTO> getStaffByOwner(Integer ownerId);

    List<StaffResponseDTO> getStaffByFacility(Integer facilityId, Integer ownerId);

    StaffResponseDTO getStaffById(Integer staffId, Integer ownerId);

    void createStaff(StaffFormDTO form, Integer ownerId);

    void updateStaff(Integer staffId, StaffFormDTO form, Integer ownerId);

    void deleteStaff(Integer staffId, Integer ownerId);
}
