package com.mvc.mock_project.mapper;

import com.mvc.mock_project.dto.response.facility.*;
import com.mvc.mock_project.entities.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FacilityMapper {

    public OwnerFacilityListDTO toOwnerFacilityListDTO(Facility facility) {
        if (facility == null) return null;
        OwnerFacilityListDTO dto = new OwnerFacilityListDTO();
        dto.setFacilityId(facility.getId());
        dto.setName(facility.getName());
        dto.setAddress(facility.getAddress());
        
        // set thumbnail
        if (facility.getImages() != null) {
            facility.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .findFirst()
                .ifPresent(img -> dto.setThumbnailUrl(img.getImagePath()));
        }
        
        dto.setApprovalStatus(facility.getApprovalStatus());
        dto.setIsActive(facility.getIsActive());
        
        int totalSports = facility.getFacilitySports() != null ? facility.getFacilitySports().size() : 0;
        dto.setTotalSports(totalSports);
        
        int totalCourts = 0;
        if (facility.getFacilitySports() != null) {
            for (FacilitySport fs : facility.getFacilitySports()) {
                if (fs.getCourts() != null) {
                    totalCourts += fs.getCourts().size();
                }
            }
        }
        dto.setTotalCourts(totalCourts);
        dto.setCreatedAt(facility.getCreatedAt());
        return dto;
    }

    public OwnerFacilityDetailDTO toOwnerFacilityDetailDTO(Facility facility) {
        if (facility == null) return null;
        OwnerFacilityDetailDTO dto = new OwnerFacilityDetailDTO();
        dto.setFacilityId(facility.getId());
        dto.setName(facility.getName());
        dto.setAddress(facility.getAddress());
        dto.setProvince(facility.getProvince());
        dto.setDistrict(facility.getDistrict());
        dto.setWard(facility.getWard());
        dto.setLatitude(facility.getLatitude());
        dto.setLongitude(facility.getLongitude());
        dto.setDescription(facility.getDescription());
        dto.setOpenTime(facility.getOpenTime());
        dto.setCloseTime(facility.getCloseTime());
        dto.setApprovalStatus(facility.getApprovalStatus());
        dto.setRejectionReason(facility.getRejectionReason());
        dto.setIsActive(facility.getIsActive());
        dto.setCreatedAt(facility.getCreatedAt());

        if (facility.getImages() != null) {
            List<FacilityImageDTO> galleryImages = new ArrayList<>();
            for (FacilityImage img : facility.getImages()) {
                if (Boolean.TRUE.equals(img.getIsThumbnail())) {
                    dto.setThumbnailUrl(img.getImagePath());
                } else {
                    galleryImages.add(toFacilityImageDTO(img));
                }
            }
            dto.setGalleryImages(galleryImages);
        }

        if (facility.getFacilitySports() != null) {
            dto.setSports(facility.getFacilitySports().stream()
                    .map(this::toFacilitySportDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public FacilityImageDTO toFacilityImageDTO(FacilityImage image) {
        if (image == null) return null;
        FacilityImageDTO dto = new FacilityImageDTO();
        dto.setImageId(image.getId());
        dto.setImageUrl(image.getImagePath());
        dto.setIsThumbnail(image.getIsThumbnail());
        return dto;
    }

    public FacilitySportDTO toFacilitySportDTO(FacilitySport fs) {
        if (fs == null) return null;
        FacilitySportDTO dto = new FacilitySportDTO();
        dto.setFacilitySportId(fs.getId());
        if (fs.getSport() != null) {
            dto.setSportId(fs.getSport().getId());
            dto.setSportName(fs.getSport().getSportName());
            dto.setSportCode(fs.getSport().getSportCode());
        }
        dto.setMinDurationMinutes(fs.getMinDurationMinutes());
        dto.setSlotStepMinutes(fs.getSlotStepMinutes());
        dto.setIsActive(fs.getIsActive());

        if (fs.getCourts() != null) {
            dto.setCourts(fs.getCourts().stream().map(this::toCourtDTO).collect(Collectors.toList()));
            dto.setCourtCount(fs.getCourts().size());
        } else {
            dto.setCourtCount(0);
        }

        if (fs.getPriceRules() != null) {
            dto.setPriceRules(fs.getPriceRules().stream().map(this::toPriceRuleDetailDTO).collect(Collectors.toList()));
        }
        
        // slot boundaries calculate frontend based on facility opentime/closetime and step
        
        return dto;
    }

    public CourtDTO toCourtDTO(Court court) {
        if (court == null) return null;
        CourtDTO dto = new CourtDTO();
        dto.setCourtId(court.getId());
        dto.setCourtName(court.getCourtName());
        dto.setDescription(court.getDescription());
        dto.setIsActive(court.getIsActive());
        if (court.getAttributeValues() != null) {
            dto.setAttributes(court.getAttributeValues().stream().map(this::toCourtAttributeDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    public CourtAttributeDTO toCourtAttributeDTO(CourtAttributeValue cav) {
        if (cav == null) return null;
        CourtAttributeDTO dto = new CourtAttributeDTO();
        dto.setAttributeId(cav.getAttribute().getId());
        dto.setAttributeCode(cav.getAttribute().getAttributeCode());
        dto.setAttributeName(cav.getAttribute().getAttributeName());
        dto.setDataType(cav.getAttribute().getDataType());
        dto.setOptionsJson(cav.getAttribute().getOptionsJson());
        dto.setValue(cav.getValue());
        dto.setIsRequired(cav.getAttribute().getIsRequired());
        return dto;
    }

    public PriceRuleDetailDTO toPriceRuleDetailDTO(FacilityPriceRule rule) {
        if (rule == null) return null;
        PriceRuleDetailDTO dto = new PriceRuleDetailDTO();
        dto.setPriceRuleId(rule.getId());
        dto.setDayType(rule.getDayType());
        dto.setStartTime(rule.getStartTime());
        dto.setEndTime(rule.getEndTime());
        dto.setPricePerSlot(rule.getPricePerSlot());
        dto.setEffectiveFrom(rule.getEffectiveFrom());
        dto.setEffectiveTo(rule.getEffectiveTo());
        dto.setIsActive(rule.getIsActive());
        return dto;
    }

    public AdminFacilityReviewDTO toAdminFacilityReviewDTO(Facility facility) {
        if (facility == null) return null;
        AdminFacilityReviewDTO dto = new AdminFacilityReviewDTO();
        dto.setFacilityId(facility.getId());
        dto.setName(facility.getName());
        dto.setAddress(facility.getAddress());
        
        if (facility.getOwner() != null) {
            dto.setOwnerName(facility.getOwner().getFullName());
            dto.setOwnerEmail(facility.getOwner().getEmail());
            dto.setOwnerPhone(facility.getOwner().getPhone());
            if (facility.getOwner().getOwnerProfile() != null) {
                dto.setBusinessName(facility.getOwner().getOwnerProfile().getBusinessName());
            }
        }
        
        dto.setApprovalStatus(facility.getApprovalStatus());
        dto.setCreatedAt(facility.getCreatedAt());
        
        if (facility.getImages() != null) {
            facility.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                .findFirst()
                .ifPresent(img -> dto.setThumbnailUrl(img.getImagePath()));
        }
        
        int totalCourts = 0;
        if (facility.getFacilitySports() != null) {
            for (FacilitySport fs : facility.getFacilitySports()) {
                if (fs.getCourts() != null) {
                    totalCourts += fs.getCourts().size();
                }
            }
        }
        dto.setTotalCourts(totalCourts);
        
        return dto;
    }
}
