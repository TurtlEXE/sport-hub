package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.StaffFormDTO;
import com.mvc.mock_project.dto.response.StaffResponseDTO;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.Facility;
import com.mvc.mock_project.entities.Staff;
import com.mvc.mock_project.entities.enums.Role;
import com.mvc.mock_project.exception.EmailAlreadyExistsException;
import com.mvc.mock_project.exception.FacilityAccessDeniedException;
import com.mvc.mock_project.exception.FacilityNotFoundException;
import com.mvc.mock_project.exception.StaffAccessDeniedException;
import com.mvc.mock_project.exception.StaffNotFoundException;
import com.mvc.mock_project.repository.AccountRepository;
import com.mvc.mock_project.repository.FacilityRepository;
import com.mvc.mock_project.repository.StaffRepository;
import com.mvc.mock_project.service.OwnerStaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnerStaffServiceImpl implements OwnerStaffService {

    private final StaffRepository staffRepository;
    private final AccountRepository accountRepository;
    private final FacilityRepository facilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<StaffResponseDTO> getStaffByOwner(Integer ownerId) {
        return staffRepository.findByOwner_IdAndIsActiveTrue(ownerId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StaffResponseDTO> getStaffByFacility(Integer facilityId, Integer ownerId) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException("facility.not.found"));
        validateFacilityOwnership(facility, ownerId);

        return staffRepository.findByFacility_IdAndIsActiveTrue(facilityId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StaffResponseDTO getStaffById(Integer staffId, Integer ownerId) {
        Staff staff = findActiveStaffById(staffId);
        validateStaffOwnership(staff, ownerId);
        return toResponseDTO(staff);
    }

    @Override
    @Transactional
    public void createStaff(StaffFormDTO form, Integer ownerId) {
        // Validate email uniqueness
        if (accountRepository.existsByEmail(form.getEmail())) {
            throw new EmailAlreadyExistsException("msg.error.email_exists");
        }

        // Validate phone uniqueness if provided
        if (form.getPhone() != null && !form.getPhone().trim().isEmpty()
                && accountRepository.existsByPhone(form.getPhone())) {
            throw new EmailAlreadyExistsException("msg.error.phone_exists");
        }

        // Validate facility ownership if provided
        Facility facility = null;
        if (form.getFacilityId() != null) {
            facility = facilityRepository.findById(form.getFacilityId())
                    .orElseThrow(() -> new FacilityNotFoundException("facility.not.found"));
            validateFacilityOwnership(facility, ownerId);
        }

        Account owner = accountRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        // Create Account with STAFF role
        String rawPassword = (form.getPassword() != null && !form.getPassword().trim().isEmpty())
                ? form.getPassword()
                : "123456";

        Account account = Account.builder()
                .fullName(form.getFullName())
                .email(form.getEmail())
                .phone(form.getPhone())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(Role.STAFF)
                .isActive(true)
                .build();
        accountRepository.save(account);

        // Create Staff assignment
        Staff staff = Staff.builder()
                .account(account)
                .facility(facility)
                .owner(owner)
                .isActive(true)
                .build();
        staffRepository.save(staff);

        log.info("Owner [{}] created staff account [{}] for facility [{}]",
                ownerId, account.getId(), facility != null ? facility.getId() : "none");
    }

    @Override
    @Transactional
    public void updateStaff(Integer staffId, StaffFormDTO form, Integer ownerId) {
        Staff staff = findActiveStaffById(staffId);
        validateStaffOwnership(staff, ownerId);

        Account account = staff.getAccount();

        // Update account info
        account.setFullName(form.getFullName());

        // Check email change
        if (!account.getEmail().equals(form.getEmail())) {
            if (accountRepository.existsByEmail(form.getEmail())) {
                throw new EmailAlreadyExistsException("msg.error.email_exists");
            }
            account.setEmail(form.getEmail());
        }

        // Check phone change
        if (form.getPhone() != null && !form.getPhone().trim().isEmpty()) {
            if (accountRepository.existsByPhoneAndIdNot(form.getPhone(), account.getId())) {
                throw new EmailAlreadyExistsException("msg.error.phone_exists");
            }
            account.setPhone(form.getPhone());
        }

        // Update password if provided
        if (form.getPassword() != null && !form.getPassword().trim().isEmpty()) {
            account.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }

        accountRepository.save(account);

        // Update facility assignment if changed
        Integer currentFacilityId = staff.getFacility() != null ? staff.getFacility().getId() : null;
        if ((form.getFacilityId() != null && !form.getFacilityId().equals(currentFacilityId))
             || (form.getFacilityId() == null && currentFacilityId != null)) {
            
            Facility newFacility = null;
            if (form.getFacilityId() != null) {
                newFacility = facilityRepository.findById(form.getFacilityId())
                        .orElseThrow(() -> new FacilityNotFoundException("facility.not.found"));
                validateFacilityOwnership(newFacility, ownerId);
            }
            staff.setFacility(newFacility);
            staffRepository.save(staff);
        }

        log.info("Owner [{}] updated staff [{}]", ownerId, staffId);
    }

    @Override
    @Transactional
    public void deleteStaff(Integer staffId, Integer ownerId) {
        Staff staff = findActiveStaffById(staffId);
        validateStaffOwnership(staff, ownerId);

        // Soft-delete
        staff.setIsActive(false);
        staff.getAccount().setIsActive(false);
        staffRepository.save(staff);
        accountRepository.save(staff.getAccount());

        log.info("Owner [{}] soft-deleted staff [{}]", ownerId, staffId);
    }

    // ---- Private helpers ----

    private Staff findActiveStaffById(Integer staffId) {
        return staffRepository.findByIdAndIsActiveTrue(staffId)
                .orElseThrow(() -> new StaffNotFoundException("staff.not.found"));
    }

    private void validateFacilityOwnership(Facility facility, Integer ownerId) {
        if (!facility.getOwner().getId().equals(ownerId)) {
            throw new FacilityAccessDeniedException("facility.access.denied");
        }
    }

    private void validateStaffOwnership(Staff staff, Integer ownerId) {
        if (!staff.getOwner().getId().equals(ownerId)) {
            throw new StaffAccessDeniedException("staff.access.denied");
        }
    }

    private StaffResponseDTO toResponseDTO(Staff staff) {
        Account account = staff.getAccount();
        Facility facility = staff.getFacility();
        return StaffResponseDTO.builder()
                .staffId(staff.getId())
                .accountId(account.getId())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phone(account.getPhone())
                .avatarPath(account.getAvatarPath())
                .isActive(staff.getIsActive())
                .facilityId(facility != null ? facility.getId() : null)
                .facilityName(facility != null ? facility.getName() : null)
                .createdAt(account.getCreatedAt())
                .build();
    }
}
