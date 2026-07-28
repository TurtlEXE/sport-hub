package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.facility.*;
import com.mvc.mock_project.dto.response.facility.*;
import com.mvc.mock_project.entities.*;
import com.mvc.mock_project.entities.enums.ApprovalStatus;
import com.mvc.mock_project.exception.*;
import com.mvc.mock_project.mapper.FacilityMapper;
import com.mvc.mock_project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerFacilityService {

    private final FacilityRepository facilityRepository;
    private final FacilitySportRepository facilitySportRepository;
    private final CourtRepository courtRepository;
    private final FacilityPriceRuleRepository priceRuleRepository;
    private final SportAttributeRepository sportAttributeRepository;
    private final CourtAttributeValueRepository courtAttributeValueRepository;
    private final AccountRepository accountRepository;
    private final SportRepository sportRepository;
    private final FacilityMapper facilityMapper;
    private final FacilityImageRepository facilityImageRepository;
    private final CloudinaryService cloudinaryService;
    private final StaffRepository staffRepository;
    private final BookingSlotRepository bookingSlotRepository;

    private void validateTimeAlignment(LocalTime openTime, LocalTime closeTime) {
        if (openTime.getMinute() % 30 != 0 || closeTime.getMinute() % 30 != 0) {
            throw new InvalidTimeFormatException("Giờ mở/đóng cửa phải chia hết cho 30 phút");
        }
        if (!closeTime.isAfter(openTime)) {
            throw new InvalidTimeFormatException("Giờ đóng cửa phải sau giờ mở cửa");
        }
    }

    private boolean isOnSlotBoundary(LocalTime time, LocalTime openTime, int slotStepMinutes) {
        int minutesFromOpen = (time.toSecondOfDay() - openTime.toSecondOfDay()) / 60;
        return minutesFromOpen >= 0 && minutesFromOpen % slotStepMinutes == 0;
    }

    private void validateOwnerProfile(Account account) {
        if (account.getOwnerProfile() == null
                || account.getOwnerProfile().getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new OwnerProfileNotApprovedException("Tài khoản chủ sân chưa được duyệt");
        }
    }

    @Transactional
    public void createFacility(Integer accountId, CreateFacilityRequest request) {
        Account owner = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        validateOwnerProfile(owner);
        validateTimeAlignment(request.getOpenTime(), request.getCloseTime());

        Facility facility = new Facility();
        facility.setOwner(owner);
        facility.setName(request.getName());
        facility.setAddress(request.getAddress());
        facility.setProvince(request.getProvince());
        facility.setDistrict(request.getDistrict());
        facility.setWard(request.getWard());
        facility.setLatitude(request.getLatitude());
        facility.setLongitude(request.getLongitude());
        facility.setDescription(request.getDescription());
        facility.setOpenTime(request.getOpenTime());
        facility.setCloseTime(request.getCloseTime());
        facility.setApprovalStatus(ApprovalStatus.PENDING);
        facility.setIsActive(true);

        facilityRepository.save(facility);
    }

    @Transactional
    public void updateFacility(Integer accountId, Integer facilityId, UpdateFacilityRequest request) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, accountId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found or access denied"));

        LocalTime newOpenTime = request.getOpenTime() != null ? request.getOpenTime() : facility.getOpenTime();
        LocalTime newCloseTime = request.getCloseTime() != null ? request.getCloseTime() : facility.getCloseTime();

        if (request.getOpenTime() != null || request.getCloseTime() != null) {
            validateTimeAlignment(newOpenTime, newCloseTime);

            // Check shrink guard
            if (!newOpenTime.equals(facility.getOpenTime()) || !newCloseTime.equals(facility.getCloseTime())) {
                if (facility.getFacilitySports() != null) {
                    for (FacilitySport fs : facility.getFacilitySports()) {
                        List<FacilityPriceRule> rules = priceRuleRepository
                                .findByFacilitySportIdAndIsActiveTrue(fs.getId());
                        for (FacilityPriceRule rule : rules) {
                            if (rule.getStartTime().isBefore(newOpenTime) || rule.getEndTime().isAfter(newCloseTime)) {
                                throw new InvalidTimeFormatException(
                                        "Không thể thu hẹp giờ hoạt động vì có bảng giá nằm ngoài khung giờ mới");
                            }
                        }
                    }
                }
            }
        }

        if (request.getName() != null)
            facility.setName(request.getName());
        if (request.getAddress() != null)
            facility.setAddress(request.getAddress());
        if (request.getProvince() != null)
            facility.setProvince(request.getProvince());
        if (request.getDistrict() != null)
            facility.setDistrict(request.getDistrict());
        if (request.getWard() != null)
            facility.setWard(request.getWard());
        if (request.getLatitude() != null)
            facility.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)
            facility.setLongitude(request.getLongitude());
        if (request.getDescription() != null)
            facility.setDescription(request.getDescription());
        if (request.getOpenTime() != null)
            facility.setOpenTime(newOpenTime);
        if (request.getCloseTime() != null)
            facility.setCloseTime(newCloseTime);

        facilityRepository.save(facility);
    }

    @Transactional
    public void deleteFacility(Integer accountId, Integer facilityId) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, accountId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));
        // TODO: check if has active bookings
        facility.setIsActive(false);
        facilityRepository.save(facility);
    }

    @Transactional
    public void resubmitFacility(Integer accountId, Integer facilityId) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, accountId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));
        if (facility.getApprovalStatus() != ApprovalStatus.REJECTED) {
            throw new InvalidFacilityStatusException("Chỉ có thể nộp lại cơ sở bị từ chối");
        }
        facility.setApprovalStatus(ApprovalStatus.PENDING);
        facility.setRejectionReason(null);
        facilityRepository.save(facility);
    }

    public List<OwnerFacilityListDTO> getMyFacilities(Integer accountId) {
        return facilityRepository.findByOwner_IdOrderByCreatedAtDesc(accountId).stream()
                .map(facilityMapper::toOwnerFacilityListDTO)
                .collect(Collectors.toList());
    }

    public OwnerFacilityDetailDTO getMyFacilityDetail(Integer accountId, Integer facilityId) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, accountId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));
        return facilityMapper.toOwnerFacilityDetailDTO(facility);
    }

    @Transactional
    public void assignStaffToFacility(Integer accountId, Integer facilityId, AssignFacilityStaffRequest request) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, accountId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));

        // Get all staff of this owner
        List<Staff> ownerStaffs = staffRepository.findByOwner_IdAndIsActiveTrue(accountId);

        for (Staff staff : ownerStaffs) {
            boolean shouldBeAssigned = request.getStaffIds() != null && request.getStaffIds().contains(staff.getId());
            boolean isCurrentlyAssigned = staff.getFacility() != null && staff.getFacility().getId().equals(facilityId);

            if (shouldBeAssigned && !isCurrentlyAssigned) {
                staff.setFacility(facility);
                staffRepository.save(staff);
            } else if (!shouldBeAssigned && isCurrentlyAssigned) {
                staff.setFacility(null);
                staffRepository.save(staff);
            }
        }
    }

    @Transactional
    public void addSportToFacility(Integer accountId, Integer facilityId, AddFacilitySportRequest request) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, accountId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));

        if (facilitySportRepository.existsByFacility_IdAndSport_Id(facilityId, request.getSportId())) {
            throw new DuplicateFacilitySportException("Môn thể thao này đã được thêm");
        }

        if (request.getSlotStepMinutes() % 30 != 0 || request.getSlotStepMinutes() < 30) {
            throw new InvalidSlotConfigException("Bước nhảy slot phải là bội số của 30 phút");
        }
        if (request.getMinDurationMinutes() < request.getSlotStepMinutes()
                || request.getMinDurationMinutes() % request.getSlotStepMinutes() != 0) {
            throw new InvalidSlotConfigException(
                    "Thời lượng tối thiểu phải >= bước nhảy slot và là bội số của bước nhảy");
        }

        Sport sport = sportRepository.findById(request.getSportId())
                .orElseThrow(() -> new RuntimeException("Sport not found"));

        FacilitySport fs = new FacilitySport();
        fs.setFacility(facility);
        fs.setSport(sport);
        fs.setMinDurationMinutes(request.getMinDurationMinutes());
        fs.setSlotStepMinutes(request.getSlotStepMinutes());
        fs.setIsActive(true);

        facilitySportRepository.save(fs);
    }

    @Transactional
    public void updateFacilitySport(Integer accountId, Integer facilitySportId, AddFacilitySportRequest request) {
        FacilitySport fs = facilitySportRepository.findByIdAndFacility_Owner_Id(facilitySportId, accountId)
                .orElseThrow(() -> new RuntimeException("Facility Sport not found"));

        if (!fs.getSlotStepMinutes().equals(request.getSlotStepMinutes())) {
            long activeRules = priceRuleRepository.countByFacilitySportIdAndIsActiveTrue(facilitySportId);
            if (activeRules > 0) {
                throw new InvalidSlotConfigException(
                        "Cannot change slot step when pricing rules exist. Please delete them first.");
            }
        }

        if (request.getSlotStepMinutes() % 30 != 0 || request.getSlotStepMinutes() < 30) {
            throw new InvalidSlotConfigException("Slot step must be a multiple of 30 minutes.");
        }
        if (request.getMinDurationMinutes() < request.getSlotStepMinutes()
                || request.getMinDurationMinutes() % request.getSlotStepMinutes() != 0) {
            throw new InvalidSlotConfigException("Min duration must be >= slot step and a multiple of slot step.");
        }

        fs.setMinDurationMinutes(request.getMinDurationMinutes());
        fs.setSlotStepMinutes(request.getSlotStepMinutes());
        facilitySportRepository.save(fs);
    }

    @Transactional
    public void removeSportFromFacility(Integer accountId, Integer facilitySportId) {
        FacilitySport fs = facilitySportRepository.findByIdAndFacility_Owner_Id(facilitySportId, accountId)
                .orElseThrow(() -> new RuntimeException("Facility Sport not found"));
        // TODO: check active bookings
        fs.setIsActive(false);
        facilitySportRepository.save(fs);
    }

    @Transactional
    public void createCourt(Integer accountId, CreateCourtRequest request) {
        FacilitySport fs = facilitySportRepository.findByIdAndFacility_Owner_Id(request.getFacilitySportId(), accountId)
                .orElseThrow(() -> new RuntimeException("Facility Sport not found"));

        Court court = new Court();
        court.setFacilitySport(fs);
        court.setCourtName(request.getCourtName());
        court.setDescription(request.getDescription());
        court.setIsActive(true);
        courtRepository.save(court);

        if (request.getAttributes() != null) {
            for (CourtAttributeRequest attrReq : request.getAttributes()) {
                SportAttribute sa = sportAttributeRepository.findById(attrReq.getAttributeId())
                        .orElseThrow(() -> new RuntimeException("Attribute not found"));
                CourtAttributeValue cav = new CourtAttributeValue();
                cav.setCourt(court);
                cav.setAttribute(sa);
                cav.setValue(attrReq.getValue());
                courtAttributeValueRepository.save(cav);
            }
        }
    }

    @Transactional
    public void updateCourt(Integer accountId, Integer courtId, UpdateCourtRequest request) {
        Court court = courtRepository.findByIdAndFacilitySport_Facility_Owner_Id(courtId, accountId)
                .orElseThrow(() -> new RuntimeException("Court not found"));

        if (request.getCourtName() != null)
            court.setCourtName(request.getCourtName());
        if (request.getDescription() != null)
            court.setDescription(request.getDescription());
        courtRepository.save(court);

        if (request.getAttributes() != null) {
            courtAttributeValueRepository.deleteByCourt_Id(courtId);
            for (CourtAttributeRequest attrReq : request.getAttributes()) {
                SportAttribute sa = sportAttributeRepository.findById(attrReq.getAttributeId())
                        .orElseThrow(() -> new RuntimeException("Attribute not found"));
                CourtAttributeValue cav = new CourtAttributeValue();
                cav.setCourt(court);
                cav.setAttribute(sa);
                cav.setValue(attrReq.getValue());
                courtAttributeValueRepository.save(cav);
            }
        }
    }

    @Transactional
    public void deleteCourt(Integer accountId, Integer courtId, boolean forceDeactivate) {
        Court court = courtRepository.findByIdAndFacilitySport_Facility_Owner_Id(courtId, accountId)
                .orElseThrow(() -> new RuntimeException("Court not found"));

        boolean hasConstraints = bookingSlotRepository.existsByCourt_Id(courtId);

        if (hasConstraints) {
            if (forceDeactivate) {
                court.setIsActive(false);
                courtRepository.save(court);
            } else {
                throw new org.springframework.dao.DataIntegrityViolationException("COURT_HAS_BOOKINGS");
            }
        } else {
            courtRepository.delete(court);
        }
    }

    @Transactional
    public void toggleCourtStatus(Integer accountId, Integer courtId) {
        Court court = courtRepository.findByIdAndFacilitySport_Facility_Owner_Id(courtId, accountId)
                .orElseThrow(() -> new RuntimeException("Court not found"));
        // If toggling off, might need to check active bookings
        boolean currentStatus = court.getIsActive() != null ? court.getIsActive() : true;
        court.setIsActive(!currentStatus);
        courtRepository.save(court);
    }

    private boolean isOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    @Transactional
    public void createPriceRule(Integer accountId, CreatePriceRuleRequest request) {
        FacilitySport fs = facilitySportRepository.findByIdAndFacility_Owner_Id(request.getFacilitySportId(), accountId)
                .orElseThrow(() -> new RuntimeException("Facility Sport not found"));

        LocalTime openTime = fs.getFacility().getOpenTime();
        LocalTime closeTime = fs.getFacility().getCloseTime();
        int step = fs.getSlotStepMinutes();

        if (!isOnSlotBoundary(request.getStartTime(), openTime, step)) {
            throw new InvalidPriceRuleException("Giờ bắt đầu phải khớp với mốc slot");
        }
        if (!isOnSlotBoundary(request.getEndTime(), openTime, step)) {
            throw new InvalidPriceRuleException("Giờ kết thúc phải khớp với mốc slot");
        }
        if (request.getStartTime().isBefore(openTime) || request.getEndTime().isAfter(closeTime)) {
            throw new InvalidPriceRuleException("Khung giờ phải nằm trong giờ hoạt động");
        }

        List<FacilityPriceRule> existingRules = priceRuleRepository
                .findByFacilitySportIdAndDayTypeAndIsActiveTrue(fs.getId(), request.getDayType());
        for (FacilityPriceRule existing : existingRules) {
            if (isOverlap(existing.getStartTime(), existing.getEndTime(), request.getStartTime(),
                    request.getEndTime())) {
                throw new PriceRuleOverlapException("Khung giờ trùng với bảng giá đã tồn tại");
            }
        }

        FacilityPriceRule rule = new FacilityPriceRule();
        rule.setFacilitySport(fs);
        rule.setDayType(request.getDayType());
        rule.setStartTime(request.getStartTime());
        rule.setEndTime(request.getEndTime());
        rule.setPricePerSlot(request.getPricePerSlot());
        rule.setEffectiveFrom(request.getEffectiveFrom());
        rule.setEffectiveTo(request.getEffectiveTo());
        rule.setIsActive(true);
        priceRuleRepository.save(rule);
    }

    @Transactional
    public void updatePriceRule(Integer accountId, Integer ruleId, UpdatePriceRuleRequest request) {
        FacilityPriceRule rule = priceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));

        FacilitySport fs = facilitySportRepository
                .findByIdAndFacility_Owner_Id(rule.getFacilitySport().getId(), accountId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        LocalTime newStart = request.getStartTime() != null ? request.getStartTime() : rule.getStartTime();
        LocalTime newEnd = request.getEndTime() != null ? request.getEndTime() : rule.getEndTime();

        LocalTime openTime = fs.getFacility().getOpenTime();
        LocalTime closeTime = fs.getFacility().getCloseTime();
        int step = fs.getSlotStepMinutes();

        if (!isOnSlotBoundary(newStart, openTime, step) || !isOnSlotBoundary(newEnd, openTime, step)) {
            throw new InvalidPriceRuleException("Giờ phải khớp với mốc slot");
        }
        if (newStart.isBefore(openTime) || newEnd.isAfter(closeTime)) {
            throw new InvalidPriceRuleException("Khung giờ phải nằm trong giờ hoạt động");
        }

        if (request.getDayType() != null || request.getStartTime() != null || request.getEndTime() != null) {
            com.mvc.mock_project.entities.enums.DayType dayType = request.getDayType() != null ? request.getDayType()
                    : rule.getDayType();
            List<FacilityPriceRule> existingRules = priceRuleRepository
                    .findByFacilitySportIdAndDayTypeAndIsActiveTrue(fs.getId(), dayType);
            for (FacilityPriceRule existing : existingRules) {
                if (!existing.getId().equals(ruleId)
                        && isOverlap(existing.getStartTime(), existing.getEndTime(), newStart, newEnd)) {
                    throw new PriceRuleOverlapException("Khung giờ trùng với bảng giá đã tồn tại");
                }
            }
        }

        if (request.getDayType() != null)
            rule.setDayType(request.getDayType());
        if (request.getStartTime() != null)
            rule.setStartTime(request.getStartTime());
        if (request.getEndTime() != null)
            rule.setEndTime(request.getEndTime());
        if (request.getPricePerSlot() != null)
            rule.setPricePerSlot(request.getPricePerSlot());
        if (request.getEffectiveFrom() != null)
            rule.setEffectiveFrom(request.getEffectiveFrom());
        if (request.getEffectiveTo() != null)
            rule.setEffectiveTo(request.getEffectiveTo());

        priceRuleRepository.save(rule);
    }

    @Transactional
    public void deletePriceRule(Integer accountId, Integer ruleId) {
        FacilityPriceRule rule = priceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));
        facilitySportRepository.findByIdAndFacility_Owner_Id(rule.getFacilitySport().getId(), accountId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        priceRuleRepository.delete(rule);
    }

    @Transactional
    public void batchSavePriceRules(Integer accountId, BatchSavePriceRulesRequest request) {
        FacilitySport fs = facilitySportRepository.findByIdAndFacility_Owner_Id(request.getFacilitySportId(), accountId)
                .orElseThrow(() -> new RuntimeException("Facility Sport not found or access denied"));

        LocalTime openTime = fs.getFacility().getOpenTime();
        LocalTime closeTime = fs.getFacility().getCloseTime();
        int step = fs.getSlotStepMinutes();

        java.util.List<java.util.Map<String, Object>> errors = new java.util.ArrayList<>();
        java.util.List<BatchSavePriceRulesRequest.PriceRuleRow> rows = request.getRows();

        if (rows == null || rows.isEmpty()) {
            java.util.Map<String, Object> err = new java.util.HashMap<>();
            err.put("message", "Price list cannot be empty");
            errors.add(err);
            throw new BatchValidationException("Validation failed", errors);
        }

        // Validate individual rows
        for (int i = 0; i < rows.size(); i++) {
            BatchSavePriceRulesRequest.PriceRuleRow row = rows.get(i);
            LocalTime startTime = LocalTime.parse(row.getStartTime());
            LocalTime endTime = LocalTime.parse(row.getEndTime());

            if (!startTime.isBefore(endTime)) {
                java.util.Map<String, Object> err = new java.util.HashMap<>();
                err.put("row", i);
                err.put("message", "Start time must be before end time");
                errors.add(err);
            }
            if (row.getWeekdayPrice() == null || row.getWeekdayPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                java.util.Map<String, Object> err = new java.util.HashMap<>();
                err.put("row", i);
                err.put("message", "Weekday price must be greater than 0");
                errors.add(err);
            }
            if (row.getWeekendPrice() == null || row.getWeekendPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                java.util.Map<String, Object> err = new java.util.HashMap<>();
                err.put("row", i);
                err.put("message", "Weekend price must be greater than 0");
                errors.add(err);
            }
            if (!isOnSlotBoundary(startTime, openTime, step) || !isOnSlotBoundary(endTime, openTime, step)) {
                java.util.Map<String, Object> err = new java.util.HashMap<>();
                err.put("row", i);
                err.put("message", "Time frame must align with slot intervals (" + step + " minutes)");
                errors.add(err);
            }
            if (startTime.isBefore(openTime) || endTime.isAfter(closeTime)) {
                java.util.Map<String, Object> err = new java.util.HashMap<>();
                err.put("row", i);
                err.put("message", "Time frame must be within facility operating hours (" + openTime + " - " + closeTime + ")");
                errors.add(err);
            }
        }

        // Check for overlaps among rows
        for (int i = 0; i < rows.size(); i++) {
            LocalTime startI = LocalTime.parse(rows.get(i).getStartTime());
            LocalTime endI = LocalTime.parse(rows.get(i).getEndTime());
            for (int j = i + 1; j < rows.size(); j++) {
                LocalTime startJ = LocalTime.parse(rows.get(j).getStartTime());
                LocalTime endJ = LocalTime.parse(rows.get(j).getEndTime());
                if (isOverlap(startI, endI, startJ, endJ)) {
                    java.util.Map<String, Object> errI = new java.util.HashMap<>();
                    errI.put("row", i);
                    errI.put("message", "Time frame overlaps with row " + (j + 1));
                    errors.add(errI);
                    
                    java.util.Map<String, Object> errJ = new java.util.HashMap<>();
                    errJ.put("row", j);
                    errJ.put("message", "Time frame overlaps with row " + (i + 1));
                    errors.add(errJ);
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new BatchValidationException("Validation failed", errors);
        }

        // Delete existing rules for this sport
        priceRuleRepository.deleteByFacilitySport_Id(fs.getId());

        // Save new rules
        for (BatchSavePriceRulesRequest.PriceRuleRow row : rows) {
            LocalTime startTime = LocalTime.parse(row.getStartTime());
            LocalTime endTime = LocalTime.parse(row.getEndTime());

            FacilityPriceRule weekdayRule = new FacilityPriceRule();
            weekdayRule.setFacilitySport(fs);
            weekdayRule.setDayType(com.mvc.mock_project.entities.enums.DayType.WEEKDAY);
            weekdayRule.setStartTime(startTime);
            weekdayRule.setEndTime(endTime);
            weekdayRule.setPricePerSlot(row.getWeekdayPrice());
            weekdayRule.setIsActive(true);
            priceRuleRepository.save(weekdayRule);

            FacilityPriceRule weekendRule = new FacilityPriceRule();
            weekendRule.setFacilitySport(fs);
            weekendRule.setDayType(com.mvc.mock_project.entities.enums.DayType.WEEKEND);
            weekendRule.setStartTime(startTime);
            weekendRule.setEndTime(endTime);
            weekendRule.setPricePerSlot(row.getWeekendPrice());
            weekendRule.setIsActive(true);
            priceRuleRepository.save(weekendRule);
        }
    }

    @Transactional
    public com.mvc.mock_project.dto.response.facility.FacilityImageDTO addImageToFacility(Integer accountId,
            Integer facilityId, MultipartFile file, String url) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, accountId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));

        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            imageUrl = cloudinaryService.uploadFacilityImage(file, facilityId);
        } else if (url != null && !url.trim().isEmpty()) {
            imageUrl = url.trim();
        } else {
            throw new RuntimeException("Phải cung cấp file hoặc đường dẫn ảnh");
        }

        FacilityImage image = new FacilityImage();
        image.setFacility(facility);
        image.setImagePath(imageUrl);

        // If it's the first image, make it thumbnail
        boolean hasImages = facilityImageRepository.existsByFacilityId(facilityId);
        image.setIsThumbnail(!hasImages);

        image = facilityImageRepository.save(image);
        return facilityMapper.toFacilityImageDTO(image);
    }

    @Transactional
    public void setFacilityThumbnail(Integer accountId, Integer facilityId, Integer imageId) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, accountId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));

        FacilityImage imageToSet = facilityImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        if (!imageToSet.getFacility().getId().equals(facilityId)) {
            throw new RuntimeException("Image does not belong to this facility");
        }

        // Reset others
        if (facility.getImages() != null) {
            for (FacilityImage img : facility.getImages()) {
                if (img.getIsThumbnail()) {
                    img.setIsThumbnail(false);
                    facilityImageRepository.save(img);
                }
            }
        }

        imageToSet.setIsThumbnail(true);
        facilityImageRepository.save(imageToSet);
    }

    @Transactional
    public void deleteFacilityImage(Integer accountId, Integer facilityId, Integer imageId) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, accountId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));

        FacilityImage imageToDelete = facilityImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        if (!imageToDelete.getFacility().getId().equals(facilityId)) {
            throw new RuntimeException("Image does not belong to this facility");
        }

        String path = imageToDelete.getImagePath();
        if (path != null && path.contains("cloudinary.com")) {
            cloudinaryService.deleteImage(path);
        }

        facilityImageRepository.delete(imageToDelete);

        // If it was thumbnail, we might want to set another one as thumbnail
        if (Boolean.TRUE.equals(imageToDelete.getIsThumbnail())) {
            List<FacilityImage> remaining = facilityImageRepository.findByFacilityId(facilityId);
            // wait, findByFacility_Id might not exist or we can just get from
            // facility.getImages() which is lazy loaded.
            // Let's rely on facility.getImages()
            if (facility.getImages() != null) {
                facility.getImages().stream()
                        .filter(img -> !img.getId().equals(imageId))
                        .findFirst()
                        .ifPresent(img -> {
                            img.setIsThumbnail(true);
                            facilityImageRepository.save(img);
                        });
            }
        }
    }
}
