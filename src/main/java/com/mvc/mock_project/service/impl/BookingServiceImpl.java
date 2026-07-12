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
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final FacilitySportRepository facilitySportRepository;
    private final CourtRepository courtRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final FacilityRepository facilityRepository;

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
        List<Voucher> vouchers = voucherRepository.findValidVouchers(facilityId, -1, java.time.LocalDateTime.now());
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

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Invoice createBookingTransaction(String guestName, String guestPhone, String email, 
                                            BigDecimal courtAmount, BigDecimal productAmount, Integer facilityId,
                                            String slotsJson, String bookingDateStr, Account account, Integer voucherId) {
        
        // 1. Save Guest ONLY if account is not present
        Guest guest = null;
        if (account == null) {
            guest = Guest.builder()
                    .guestName(guestName != null && !guestName.isEmpty() ? guestName : "Khách vãng lai")
                    .phone(guestPhone != null && !guestPhone.isEmpty() ? guestPhone : "N/A")
                    .email(email)
                    .build();
            guest = guestRepository.save(guest);
        }

        // 2. Fetch Facility
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        // 3. Save Booking
        Booking booking = Booking.builder()
                .facility(facility)
                .guest(guest)
                .account(account)
                .bookingStatus(com.mvc.mock_project.entities.enums.BookingStatus.PENDING)
                .build();
        booking = bookingRepository.save(booking);
        
        // 4. Parse and Save Booking Slots
        if (slotsJson != null && !slotsJson.isEmpty() && !slotsJson.equals("[]")) {
            try {
                JsonParser springParser = JsonParserFactory.getJsonParser();
                List<Object> rawList = springParser.parseList(slotsJson);
                
                LocalDate bookingDate = LocalDate.now();
                if (bookingDateStr != null && !bookingDateStr.isEmpty()) {
                    bookingDate = LocalDate.parse(bookingDateStr);
                }
                
                for (Object item : rawList) {
                    Map<String, Object> slotMap = (Map<String, Object>) item;
                    Integer courtId = slotMap.get("courtId") != null ? Integer.parseInt(slotMap.get("courtId").toString()) : null;
                    if (courtId == null) continue;
                    
                    Court court = courtRepository.findById(courtId).orElse(null);
                    if (court == null) continue;
                    
                    LocalTime startTime = LocalTime.parse(slotMap.get("startTime").toString());
                    String endStr = slotMap.get("endTime").toString();
                    LocalTime endTime = endStr.equals("23:59") ? LocalTime.MAX : LocalTime.parse(endStr);
                    BigDecimal price = new BigDecimal(slotMap.get("price").toString());
                    
                    BookingSlot bookingSlot = BookingSlot.builder()
                            .booking(booking)
                            .court(court)
                            .bookingDate(bookingDate)
                            .startTime(startTime)
                            .endTime(endTime)
                            .priceSnapshot(price)
                            .slotStatus(com.mvc.mock_project.entities.enums.SlotStatus.PENDING)
                            .build();
                    bookingSlotRepository.save(bookingSlot);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 5. Fetch Voucher and Calculate Discounts
        com.mvc.mock_project.entities.Voucher voucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal subtotal = courtAmount.add(productAmount);

        if (voucherId != null) {
            voucher = voucherRepository.findById(voucherId).orElse(null);
            if (voucher != null) {
                if (com.mvc.mock_project.entities.enums.DiscountType.PERCENTAGE.equals(voucher.getDiscountType())) {
                    discountAmount = subtotal.multiply(voucher.getDiscountValue().divide(new BigDecimal("100")));
                    if (voucher.getMaxDiscountAmount() != null && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                        discountAmount = voucher.getMaxDiscountAmount();
                    }
                } else {
                    discountAmount = voucher.getDiscountValue();
                }
            }
        }

        BigDecimal totalAmount = subtotal.subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) totalAmount = BigDecimal.ZERO;

        // 6. Save Invoice
        Invoice invoice = Invoice.builder()
                .booking(booking)
                .courtAmount(courtAmount) // Original Court Amount passed from PaymentController
                .productAmount(productAmount)
                .subtotal(subtotal)
                .discountAmount(discountAmount) 
                .totalAmount(totalAmount) 
                .voucher(voucher)
                .paymentStatus(com.mvc.mock_project.entities.enums.InvoiceStatus.UNPAID)
                .paidAmount(BigDecimal.ZERO)
                .depositPercent(100)
                .refundDue(BigDecimal.ZERO)
                .refundStatus(com.mvc.mock_project.entities.enums.RefundStatus.NONE)
                .build();
        return invoiceRepository.save(invoice);
    }
}
