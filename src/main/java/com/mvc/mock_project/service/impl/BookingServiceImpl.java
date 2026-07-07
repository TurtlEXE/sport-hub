package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.entities.*;
import com.mvc.mock_project.entities.enums.DayType;
import com.mvc.mock_project.entities.enums.SlotStatus;
import com.mvc.mock_project.repository.*;
import com.mvc.mock_project.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final FacilitySportRepository facilitySportRepository;
    private final CourtRepository courtRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;

    @Override
    public List<Map<String, Object>> getFacilitySportsByVenue(Integer venueId) {
        List<FacilitySport> sports = facilitySportRepository.findByFacilityIdAndIsActiveTrue(venueId);
        return sports.stream().map(fs -> {
            Map<String, Object> map = new HashMap<>();
            map.put("facilitySportId", fs.getId());
            map.put("sportName", fs.getSport().getSportName());
            
            List<Map<String, Object>> groupedRules = new ArrayList<>();
            if (fs.getPriceRules() != null) {
                Map<DayType, List<FacilityPriceRule>> rulesByDay = fs.getPriceRules().stream()
                        .filter(FacilityPriceRule::getIsActive)
                        .collect(Collectors.groupingBy(FacilityPriceRule::getDayType));
                
                for (Map.Entry<DayType, List<FacilityPriceRule>> entry : rulesByDay.entrySet()) {
                    Map<String, Object> groupMap = new HashMap<>();
                    String dayTypeName = entry.getKey() == DayType.WEEKDAY ? "T2 - T6" : 
                                        (entry.getKey() == DayType.WEEKEND ? "T7 - CN" : "Ngày lễ");
                    groupMap.put("dayTypeName", dayTypeName);
                    groupMap.put("rowspan", entry.getValue().size());
                    
                    List<Map<String, Object>> ruleMaps = new ArrayList<>();
                    for (FacilityPriceRule rule : entry.getValue()) {
                        Map<String, Object> rMap = new HashMap<>();
                        rMap.put("startTime", formatTime(rule.getStartTime()));
                        rMap.put("endTime", formatTime(rule.getEndTime()));
                        rMap.put("pricePerSlot", rule.getPricePerSlot());
                        ruleMaps.add(rMap);
                    }
                    groupMap.put("rules", ruleMaps);
                    groupedRules.add(groupMap);
                }
            }
            map.put("priceRulesGrouped", groupedRules);
            map.put("slotDurationMinutes", fs.getSlotStepMinutes());
            
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getBookingTimeline(Integer facilitySportId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        FacilitySport fs = facilitySportRepository.findById(facilitySportId)
                .orElseThrow(() -> new RuntimeException("FacilitySport not found"));
        
        Facility facility = fs.getFacility();
        
        Map<String, Object> response = new HashMap<>();
        response.put("date", dateStr);
        response.put("openingTime", facility.getOpenTime().toString());
        response.put("closingTime", facility.getCloseTime().toString());
        response.put("slotDurationMinutes", fs.getSlotStepMinutes());
        response.put("minBookingDurationMinutes", fs.getMinDurationMinutes());

        List<Court> courtsEntity = courtRepository.findByFacilitySportIdAndIsActiveTrue(facilitySportId);
        List<Integer> courtIds = courtsEntity.stream().map(Court::getId).collect(Collectors.toList());
        
        List<BookingSlot> bookedSlots = new ArrayList<>();
        if (!courtIds.isEmpty()) {
            bookedSlots = bookingSlotRepository.findActiveSlotsByCourtIdsAndDate(courtIds, date);
        }
        
        // Fetch price rules for this facility sport
        List<FacilityPriceRule> priceRules = fs.getPriceRules();
        if (priceRules == null) priceRules = new ArrayList<>();
        
        DayType dayType = getDayType(date);
        
        // Check if there are active price rules for this day type
        boolean hasPriceRuleForDay = priceRules.stream()
                .anyMatch(r -> r.getIsActive() && r.getDayType() == dayType);
                
        // If there are price rules in general but none for today, it's closed.
        // If there are NO price rules at all, we might fallback to default, or we can just mark it closed.
        // The safest approach is marking it closed if hasPriceRuleForDay is false.
        boolean isClosedToday = !priceRules.isEmpty() && !hasPriceRuleForDay;
        response.put("isClosedToday", isClosedToday);

        List<Map<String, Object>> courtsData = new ArrayList<>();
        for (Court court : courtsEntity) {
            Map<String, Object> cMap = new HashMap<>();
            cMap.put("courtId", court.getId());
            cMap.put("courtName", court.getCourtName());
            
            List<Map<String, Object>> slots = generateSlots(
                    facility.getOpenTime(), 
                    facility.getCloseTime(), 
                    fs.getSlotStepMinutes(),
                    court.getId(),
                    bookedSlots,
                    priceRules,
                    dayType
            );
            cMap.put("slots", slots);
            courtsData.add(cMap);
        }
        
        response.put("courts", courtsData);
        return response;
    }
    
    private List<Map<String, Object>> generateSlots(
            LocalTime openTime, LocalTime closeTime, int stepMins,
            Integer courtId, List<BookingSlot> bookedSlots,
            List<FacilityPriceRule> priceRules, DayType dayType) {
        
        List<Map<String, Object>> slots = new ArrayList<>();
        int slotIndex = 0;
        
        LocalTime curr = openTime;
        while (curr.isBefore(closeTime)) {
            LocalTime next = curr.plusMinutes(stepMins);
            if (next.isAfter(closeTime) || (next.isBefore(curr) && next.equals(LocalTime.MIDNIGHT))) {
                // Adjust if over midnight or past closing (simplification: assume no overnight booking in MVP unless closeTime is midnight)
                if (next.equals(LocalTime.MIDNIGHT)) {
                    next = LocalTime.MAX;
                } else {
                    next = closeTime;
                }
            }
            
            Map<String, Object> slot = new HashMap<>();
            slot.put("slotIndex", slotIndex);
            slot.put("startTime", formatTime(curr));
            slot.put("endTime", formatTime(next));
            
            // Check if booked
            boolean isBooked = false;
            for (BookingSlot bs : bookedSlots) {
                if (bs.getCourt().getId().equals(courtId)) {
                    // Overlap check
                    if (curr.isBefore(bs.getEndTime()) && next.isAfter(bs.getStartTime())) {
                        isBooked = true;
                        break;
                    }
                }
            }
            
            slot.put("status", isBooked ? "BOOKED" : "AVAILABLE");
            
            // Calculate price based on FacilityPriceRule
            BigDecimal price = calculatePrice(curr, next, priceRules, dayType);
            slot.put("price", price);
            
            slots.add(slot);
            slotIndex++;
            curr = next;
            if (curr.equals(LocalTime.MAX) || curr.equals(closeTime)) {
                break;
            }
        }
        
        return slots;
    }
    
    private BigDecimal calculatePrice(LocalTime start, LocalTime end, List<FacilityPriceRule> rules, DayType dayType) {
        // Fallback default price if no rules
        BigDecimal defaultPrice = new BigDecimal("50000");
        
        for (FacilityPriceRule rule : rules) {
            if (!rule.getIsActive()) continue;
            if (rule.getDayType() != dayType) continue;
            
            // Time overlap
            // If the slot is completely inside the rule's time
            if (!start.isBefore(rule.getStartTime()) && !end.isAfter(rule.getEndTime())) {
                return rule.getPricePerSlot();
            }
            // Simplification: if it partially overlaps, we just take the first rule that matches start time
            if (!start.isBefore(rule.getStartTime()) && start.isBefore(rule.getEndTime())) {
                return rule.getPricePerSlot();
            }
        }
        return defaultPrice;
    }
    
    private String formatTime(LocalTime time) {
        if (time.equals(LocalTime.MAX)) return "23:59";
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
    
    private DayType getDayType(LocalDate date) {
        java.time.DayOfWeek dow = date.getDayOfWeek();
        if (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY) {
            return DayType.WEEKEND;
        }
        return DayType.WEEKDAY;
    }

    @Override
    public List<Map<String, Object>> getFacilityProducts(Integer facilityId) {
        List<Product> products = productRepository.findByFacility_IdAndIsActiveTrue(facilityId);
        return products.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", p.getId());
            map.put("productName", p.getProductName());
            map.put("price", p.getPrice());
            map.put("unit", p.getRentalUnit());
            map.put("stock", p.getStockQuantity());
            map.put("imagePath", p.getImagePath());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getFacilityVouchers(Integer facilityId) {
        List<Voucher> vouchers = voucherRepository.findActiveVouchersForFacility(facilityId, java.time.LocalDateTime.now());
        return vouchers.stream().map(v -> {
            Map<String, Object> map = new HashMap<>();
            map.put("voucherId", v.getId());
            map.put("code", v.getCode());
            map.put("name", v.getName());
            map.put("discountType", v.getDiscountType().name());
            map.put("discountValue", v.getDiscountValue());
            map.put("minOrderAmount", v.getMinOrderAmount());
            map.put("maxDiscountAmount", v.getMaxDiscountAmount());
            map.put("issuerType", v.getIssuerType().name());
            return map;
        }).collect(Collectors.toList());
    }
}
