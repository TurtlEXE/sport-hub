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
    // cloudinaryService could be added later for images

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
        if (account.getOwnerProfile() == null || account.getOwnerProfile().getApprovalStatus() != ApprovalStatus.APPROVED) {
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
                        List<FacilityPriceRule> rules = priceRuleRepository.findByFacilitySportIdAndIsActiveTrue(fs.getId());
                        for (FacilityPriceRule rule : rules) {
                            if (rule.getStartTime().isBefore(newOpenTime) || rule.getEndTime().isAfter(newCloseTime)) {
                                throw new InvalidTimeFormatException("Không thể thu hẹp giờ hoạt động vì có bảng giá nằm ngoài khung giờ mới");
                            }
                        }
                    }
                }
            }
        }

        if (request.getName() != null) facility.setName(request.getName());
        if (request.getAddress() != null) facility.setAddress(request.getAddress());
        if (request.getProvince() != null) facility.setProvince(request.getProvince());
        if (request.getDistrict() != null) facility.setDistrict(request.getDistrict());
        if (request.getWard() != null) facility.setWard(request.getWard());
        if (request.getLatitude() != null) facility.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) facility.setLongitude(request.getLongitude());
        if (request.getDescription() != null) facility.setDescription(request.getDescription());
        if (request.getOpenTime() != null) facility.setOpenTime(newOpenTime);
        if (request.getCloseTime() != null) facility.setCloseTime(newCloseTime);

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
    public void addSportToFacility(Integer accountId, Integer facilityId, AddFacilitySportRequest request) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, accountId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));
        
        if (facilitySportRepository.existsByFacility_IdAndSport_Id(facilityId, request.getSportId())) {
            throw new DuplicateFacilitySportException("Môn thể thao này đã được thêm");
        }

        if (request.getSlotStepMinutes() % 30 != 0 || request.getSlotStepMinutes() < 30) {
            throw new InvalidSlotConfigException("Bước nhảy slot phải là bội số của 30 phút");
        }
        if (request.getMinDurationMinutes() < request.getSlotStepMinutes() || request.getMinDurationMinutes() % request.getSlotStepMinutes() != 0) {
            throw new InvalidSlotConfigException("Thời lượng tối thiểu phải >= bước nhảy slot và là bội số của bước nhảy");
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
                throw new InvalidSlotConfigException("Không thể thay đổi bước nhảy slot khi đã có bảng giá");
            }
        }

        if (request.getSlotStepMinutes() % 30 != 0 || request.getSlotStepMinutes() < 30) {
            throw new InvalidSlotConfigException("Bước nhảy slot phải là bội số của 30 phút");
        }
        if (request.getMinDurationMinutes() < request.getSlotStepMinutes() || request.getMinDurationMinutes() % request.getSlotStepMinutes() != 0) {
            throw new InvalidSlotConfigException("Thời lượng tối thiểu phải >= bước nhảy slot và là bội số của bước nhảy");
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
        
        if (request.getCourtName() != null) court.setCourtName(request.getCourtName());
        if (request.getDescription() != null) court.setDescription(request.getDescription());
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
    public void deleteCourt(Integer accountId, Integer courtId) {
        Court court = courtRepository.findByIdAndFacilitySport_Facility_Owner_Id(courtId, accountId)
                .orElseThrow(() -> new RuntimeException("Court not found"));
        // TODO: check active bookings
        court.setIsActive(false);
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

        List<FacilityPriceRule> existingRules = priceRuleRepository.findByFacilitySportIdAndDayTypeAndIsActiveTrue(fs.getId(), request.getDayType());
        for (FacilityPriceRule existing : existingRules) {
            if (isOverlap(existing.getStartTime(), existing.getEndTime(), request.getStartTime(), request.getEndTime())) {
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
        
        FacilitySport fs = facilitySportRepository.findByIdAndFacility_Owner_Id(rule.getFacilitySport().getId(), accountId)
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
            com.mvc.mock_project.entities.enums.DayType dayType = request.getDayType() != null ? request.getDayType() : rule.getDayType();
            List<FacilityPriceRule> existingRules = priceRuleRepository.findByFacilitySportIdAndDayTypeAndIsActiveTrue(fs.getId(), dayType);
            for (FacilityPriceRule existing : existingRules) {
                if (!existing.getId().equals(ruleId) && isOverlap(existing.getStartTime(), existing.getEndTime(), newStart, newEnd)) {
                    throw new PriceRuleOverlapException("Khung giờ trùng với bảng giá đã tồn tại");
                }
            }
        }

        if (request.getDayType() != null) rule.setDayType(request.getDayType());
        if (request.getStartTime() != null) rule.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) rule.setEndTime(request.getEndTime());
        if (request.getPricePerSlot() != null) rule.setPricePerSlot(request.getPricePerSlot());
        if (request.getEffectiveFrom() != null) rule.setEffectiveFrom(request.getEffectiveFrom());
        if (request.getEffectiveTo() != null) rule.setEffectiveTo(request.getEffectiveTo());

        priceRuleRepository.save(rule);
    }

    @Transactional
    public void deletePriceRule(Integer accountId, Integer ruleId) {
        FacilityPriceRule rule = priceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));
        facilitySportRepository.findByIdAndFacility_Owner_Id(rule.getFacilitySport().getId(), accountId)
                .orElseThrow(() -> new RuntimeException("Access denied"));
        
        rule.setIsActive(false);
        priceRuleRepository.save(rule);
    }
}
