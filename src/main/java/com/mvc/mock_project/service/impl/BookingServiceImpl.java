package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.OnSiteBookingRequestDTO;
import com.mvc.mock_project.entities.*;
import com.mvc.mock_project.entities.enums.BookingStatus;
import com.mvc.mock_project.entities.enums.DayType;
import com.mvc.mock_project.entities.enums.InvoiceStatus;
import com.mvc.mock_project.entities.enums.PaymentMethod;
import com.mvc.mock_project.entities.enums.PaymentStatus;
import com.mvc.mock_project.entities.enums.PaymentType;
import com.mvc.mock_project.entities.enums.SlotStatus;
import com.mvc.mock_project.repository.*;
import com.mvc.mock_project.service.BookingService;
import com.mvc.mock_project.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final FacilityRepository facilityRepository;
    private final CourtSlotBookingRepository courtSlotBookingRepository;
    private final StaffRepository staffRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final FacilityPriceRuleRepository facilityPriceRuleRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;

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
        
        List<CourtSlotBooking> bookedSlots = new ArrayList<>();
        if (!courtIds.isEmpty()) {
            bookedSlots = courtSlotBookingRepository.findActiveHoldsByCourtIdsAndDate(courtIds, date);
        }
        
        // Fetch price rules for this facility sport
        List<FacilityPriceRule> priceRules = fs.getPriceRules();
        if (priceRules == null) priceRules = new ArrayList<>();
        
        DayType dayType = getDayType(date);
        
        // Check if there are active price rules for this day type
        boolean hasPriceRuleForDay = priceRules.stream()
                .anyMatch(r -> r.getIsActive() && r.getDayType() == dayType);
                
        // If there are NO price rules at all for this day type, it's closed.
        boolean isClosedToday = !hasPriceRuleForDay;
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
            Integer courtId, List<CourtSlotBooking> bookedSlots,
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
            for (CourtSlotBooking bs : bookedSlots) {
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
        for (FacilityPriceRule rule : rules) {
            if (!Boolean.TRUE.equals(rule.getIsActive())) continue;
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
        return null; // Return null if no rule is found, to hide the slot
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
                                            Integer facilityId,
                                            String slotsJson, String productsJson, String bookingDateStr, Account account, Integer voucherId, Integer voucherPlatformId) {
        
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
                .bookingStatus(BookingStatus.PENDING)
                .holdExpiredAt(LocalDateTime.now().plusMinutes(10))
                .build();
        booking = bookingRepository.save(booking);
        
        // 4. Parse and Save Booking Slots — CALCULATE PRICE FROM DB
        BigDecimal serverCourtAmount = BigDecimal.ZERO;
        if (slotsJson != null && !slotsJson.isEmpty() && !slotsJson.equals("[]")) {
            try {
                JsonParser springParser = JsonParserFactory.getJsonParser();
                List<Object> rawList = springParser.parseList(slotsJson);
                
                LocalDate bookingDate = LocalDate.now();
                if (bookingDateStr != null && !bookingDateStr.isEmpty()) {
                    bookingDate = LocalDate.parse(bookingDateStr);
                }
                
                DayType dayType = getDayType(bookingDate);
                
                // Cache price rules per facilitySportId to avoid repeated DB queries
                Map<Integer, List<FacilityPriceRule>> priceRulesCache = new HashMap<>();
                
                for (Object item : rawList) {
                    Map<String, Object> slotMap = (Map<String, Object>) item;
                    Integer courtId = slotMap.get("courtId") != null ? Integer.parseInt(slotMap.get("courtId").toString()) : null;
                    if (courtId == null) continue;
                    
                    Court court = courtRepository.findById(courtId).orElse(null);
                    if (court == null) continue;
                    
                    LocalTime startTime = LocalTime.parse(slotMap.get("startTime").toString());
                    String endStr = slotMap.get("endTime").toString();
                    LocalTime endTime = endStr.equals("23:59") ? LocalTime.MAX : LocalTime.parse(endStr);
                    
                    // ★ SERVER-SIDE PRICE CALCULATION — do NOT trust frontend price ★
                    Integer fSportId = court.getFacilitySport().getId();
                    List<FacilityPriceRule> priceRules = priceRulesCache.computeIfAbsent(fSportId,
                            id -> facilityPriceRuleRepository.findByFacilitySportIdAndIsActiveTrue(id));
                    BigDecimal price = calculatePrice(startTime, endTime, priceRules, dayType);
                    if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("No valid price rule for slot: " + startTime + "-" + endTime + " on court " + courtId);
                    }
                    serverCourtAmount = serverCourtAmount.add(price);
                    
                    BookingSlot bookingSlot = BookingSlot.builder()
                            .booking(booking)
                            .court(court)
                            .bookingDate(bookingDate)
                            .startTime(startTime)
                            .endTime(endTime)
                            .priceSnapshot(price) // Price from DB, NOT from frontend
                            .slotStatus(com.mvc.mock_project.entities.enums.SlotStatus.PENDING)
                            .build();
                    bookingSlot = bookingSlotRepository.save(bookingSlot);
                    
                    CourtSlotBooking csb = CourtSlotBooking.builder()
                            .court(court)
                            .bookingDate(bookingDate)
                            .startTime(startTime)
                            .endTime(endTime)
                            .bookingSlot(bookingSlot)
                            .build();
                    courtSlotBookingRepository.save(csb);
                }
            } catch (IllegalArgumentException e) {
                throw e; // Re-throw validation errors
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 4b. Parse and Save Products — CALCULATE PRICE FROM DB
        BigDecimal serverProductAmount = BigDecimal.ZERO;
        if (productsJson != null && !productsJson.isEmpty() && !productsJson.equals("[]")) {
            try {
                JsonParser springParser = JsonParserFactory.getJsonParser();
                List<Object> productList = springParser.parseList(productsJson);
                
                for (Object item : productList) {
                    Map<String, Object> prodMap = (Map<String, Object>) item;
                    Integer productId = prodMap.get("productId") != null ? Integer.parseInt(prodMap.get("productId").toString()) : null;
                    int quantity = prodMap.get("quantity") != null ? Integer.parseInt(prodMap.get("quantity").toString()) : 0;
                    if (productId == null || quantity <= 0) continue;
                    
                    Product product = productRepository.findById(productId).orElse(null);
                    if (product == null || !Boolean.TRUE.equals(product.getIsActive())) continue;
                    
                    // ★ Use price from DB, NOT from frontend ★
                    BigDecimal unitPrice = product.getPrice();
                    BigDecimal lineTotal = unitPrice.multiply(new BigDecimal(quantity));
                    serverProductAmount = serverProductAmount.add(lineTotal);
                    
                    OrderItem orderItem = OrderItem.builder()
                            .booking(booking)
                            .product(product)
                            .quantity(quantity)
                            .unitPriceSnapshot(unitPrice)
                            .totalAmount(lineTotal)
                            .addedBy("CUSTOMER")
                            .build();
                    orderItemRepository.save(orderItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 5. Fetch Vouchers and Calculate Discounts — applied ONLY to court amount
        com.mvc.mock_project.entities.Voucher voucher = null;
        com.mvc.mock_project.entities.Voucher voucherPlatform = null;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal subtotal = serverCourtAmount.add(serverProductAmount);
        LocalDateTime now = LocalDateTime.now();

        if (voucherId != null) {
            voucher = voucherRepository.findById(voucherId).orElse(null);
            if (voucher != null && Boolean.TRUE.equals(voucher.getIsActive())
                    && !now.isBefore(voucher.getValidFrom())
                    && !now.isAfter(voucher.getValidTo())
                    && (voucher.getMinOrderAmount() == null || serverCourtAmount.compareTo(voucher.getMinOrderAmount()) >= 0)) {
                if (com.mvc.mock_project.entities.enums.DiscountType.PERCENTAGE.equals(voucher.getDiscountType())) {
                    BigDecimal d = serverCourtAmount.multiply(voucher.getDiscountValue().divide(new BigDecimal("100")));
                    if (voucher.getMaxDiscountAmount() != null && d.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                        d = voucher.getMaxDiscountAmount();
                    }
                    discountAmount = discountAmount.add(d);
                } else {
                    discountAmount = discountAmount.add(voucher.getDiscountValue());
                }
            } else {
                voucher = null; // Voucher invalid → ignore
            }
        }
        
        if (voucherPlatformId != null) {
            voucherPlatform = voucherRepository.findById(voucherPlatformId).orElse(null);
            if (voucherPlatform != null && Boolean.TRUE.equals(voucherPlatform.getIsActive())
                    && !now.isBefore(voucherPlatform.getValidFrom())
                    && !now.isAfter(voucherPlatform.getValidTo())
                    && (voucherPlatform.getMinOrderAmount() == null || serverCourtAmount.compareTo(voucherPlatform.getMinOrderAmount()) >= 0)) {
                if (com.mvc.mock_project.entities.enums.DiscountType.PERCENTAGE.equals(voucherPlatform.getDiscountType())) {
                    BigDecimal d = serverCourtAmount.multiply(voucherPlatform.getDiscountValue().divide(new BigDecimal("100")));
                    if (voucherPlatform.getMaxDiscountAmount() != null && d.compareTo(voucherPlatform.getMaxDiscountAmount()) > 0) {
                        d = voucherPlatform.getMaxDiscountAmount();
                    }
                    discountAmount = discountAmount.add(d);
                } else {
                    discountAmount = discountAmount.add(voucherPlatform.getDiscountValue());
                }
            } else {
                voucherPlatform = null; // Voucher invalid → ignore
            }
        }

        if (discountAmount.compareTo(serverCourtAmount) > 0) {
            discountAmount = serverCourtAmount;
        }

        BigDecimal totalAmount = subtotal.subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) totalAmount = BigDecimal.ZERO;

        // 6. Save Invoice — all amounts calculated from DB
        Invoice invoice = Invoice.builder()
                .booking(booking)
                .courtAmount(serverCourtAmount)
                .productAmount(serverProductAmount)
                .subtotal(subtotal)
                .discountAmount(discountAmount) 
                .totalAmount(totalAmount) 
                .voucher(voucher)
                .voucherPlatform(voucherPlatform)
                .paymentStatus(com.mvc.mock_project.entities.enums.InvoiceStatus.UNPAID)
                .paidAmount(BigDecimal.ZERO)
                .depositPercent(100)
                .refundDue(BigDecimal.ZERO)
                .refundStatus(com.mvc.mock_project.entities.enums.RefundStatus.NONE)
                .build();
        return invoiceRepository.save(invoice);
    }
    
    @Override
    @Transactional
    public void cancelExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findExpiredPendingBookings(now);
        
        for (Booking b : expiredBookings) {
            if (b.getInvoice() != null && (paymentRepository.existsByInvoiceIdAndPaymentStatus(b.getInvoice().getId(), PaymentStatus.PAID)
                    || paymentRepository.existsByInvoiceIdAndPaymentStatus(b.getInvoice().getId(), PaymentStatus.PARTIAL))) {
                continue;
            }

            b.setBookingStatus(BookingStatus.CANCELLED);
            
            if (b.getInvoice() != null) {
                b.getInvoice().setPaymentStatus(InvoiceStatus.CANCELLED);
            }
            
            if (b.getBookingSlots() != null) {
                for (BookingSlot bs : b.getBookingSlots()) {
                    bs.setSlotStatus(SlotStatus.CANCELLED);
                }
            }

            List<CourtSlotBooking> holds = courtSlotBookingRepository.findByBookingId(b.getId());
            if (holds != null && !holds.isEmpty()) {
                courtSlotBookingRepository.deleteAll(holds);
            }
        }
        bookingRepository.saveAll(expiredBookings);
    }
    
    @Override
    @Transactional
    public void processPaymentSuccess(Integer invoiceId, String totalPrice, String transactionId, String paymentTime, String email) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice == null) {
            return;
        }

        if (paymentRepository.existsByInvoiceIdAndPaymentStatus(invoiceId, PaymentStatus.PAID)
                || paymentRepository.existsByInvoiceIdAndPaymentStatus(invoiceId, PaymentStatus.PARTIAL)) {
            return;
        }

        BigDecimal paidAmount = invoice.getTotalAmount();
        if (totalPrice != null && !totalPrice.trim().isEmpty()) {
            try {
                paidAmount = new BigDecimal(totalPrice).divide(new BigDecimal(100));
            } catch (Exception ignored) {}
        }

        invoice.setPaymentStatus(InvoiceStatus.PARTIAL);
        invoice.setPaidAmount(paidAmount);

        if (invoice.getBooking() != null) {
            Booking booking = invoice.getBooking();
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            
            List<BookingSlot> slots = booking.getBookingSlots();
            if (slots != null) {
                for (BookingSlot slot : slots) {
                    slot.setSlotStatus(SlotStatus.PENDING);
                }
                bookingSlotRepository.saveAll(slots);
            }
            bookingRepository.save(booking);
        }
        invoiceRepository.save(invoice);

        LocalDateTime payTime = LocalDateTime.now();
        if (paymentTime != null && !paymentTime.isEmpty() && !"null".equals(paymentTime)) {
            try {
                payTime = LocalDateTime.parse(paymentTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            } catch (Exception ignored) {}
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .vnpayTxnNo(transactionId)
                .vnpayResponseCode("00")
                .transactionCode(String.valueOf(invoiceId))
                .paidAmount(paidAmount)
                .paymentTime(payTime)
                .paymentType(PaymentType.DEPOSIT)
                .method(PaymentMethod.VNPAY)
                .paymentStatus(PaymentStatus.PARTIAL)
                .build();
        paymentRepository.save(payment);
        
        if (email != null && !email.isEmpty() && !"null".equals(email)) {
            String formattedTime = payTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            String facilityName = (invoice.getBooking() != null && invoice.getBooking().getFacility() != null) 
                    ? invoice.getBooking().getFacility().getName() : "Venue";
            StringBuilder slotsInfo = new StringBuilder();
            if (invoice.getBooking() != null && invoice.getBooking().getBookingSlots() != null) {
                for (BookingSlot bs : invoice.getBooking().getBookingSlots()) {
                    slotsInfo.append(bs.getCourt().getCourtName()).append(" (")
                             .append(bs.getStartTime()).append("-").append(bs.getEndTime()).append("), ");
                }
            }
            
            String bookingDetails = "Mã giao dịch: " + transactionId + "\n"
                    + "Thời gian: " + formattedTime + "\n"
                    + "Cơ sở: " + facilityName + "\n"
                    + "Chi tiết sân: " + slotsInfo.toString();
            emailService.sendPaymentSuccessEmail(email, bookingDetails);
        }
    }

    @Override
    @Transactional
    public Invoice createOnSiteBooking(OnSiteBookingRequestDTO request, Account creatorAccount) {
        // 1. Fetch Facility
        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        // 2. Determine Account vs Staff creator ownership and validate access
        Account ownerAccount = null;
        Staff staffEntity = null;

        if (creatorAccount != null) {
            Optional<Staff> staffOpt = staffRepository.findByAccountId(creatorAccount.getId());
            if (staffOpt.isPresent()) {
                staffEntity = staffOpt.get();
                if (staffEntity.getFacility() == null || !staffEntity.getFacility().getId().equals(facility.getId())) {
                    throw new org.springframework.security.access.AccessDeniedException("Staff member does not have permission to manage this facility");
                }
                ownerAccount = null; // Do not attach owner_id when Staff creates booking
            } else {
                ownerAccount = creatorAccount;
                if (facility.getOwner() == null || !facility.getOwner().getId().equals(creatorAccount.getId())) {
                    throw new org.springframework.security.access.AccessDeniedException("You are not the owner of this facility");
                }
            }
        }

        // 3. Determine Customer link (Account vs Walk-in Guest)
        Account targetCustomerAccount = null;
        if (request.getTargetAccountId() != null) {
            targetCustomerAccount = accountRepository.findById(request.getTargetAccountId()).orElse(null);
        }

        Guest guest = null;
        if (targetCustomerAccount == null) {
            guest = Guest.builder()
                    .guestName(request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty() 
                            ? request.getCustomerName().trim() : "Walk-in Guest")
                    .phone(request.getCustomerPhone() != null && !request.getCustomerPhone().trim().isEmpty() 
                            ? request.getCustomerPhone().trim() : "N/A")
                    .email(request.getCustomerEmail())
                    .build();
            guest = guestRepository.save(guest);
        }

        String reqPm = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "CASH";
        PaymentMethod pMethod;
        try {
            pMethod = PaymentMethod.valueOf(reqPm);
        } catch (Exception e) {
            pMethod = PaymentMethod.CASH;
        }

        BookingStatus bStatus = BookingStatus.CONFIRMED;

        // 4. Save Booking with both target customer account and owner_id
        Booking booking = Booking.builder()
                .facility(facility)
                .guest(guest)
                .account(targetCustomerAccount)
                .owner(ownerAccount)
                .staff(staffEntity)
                .bookingStatus(bStatus)
                .note(request.getNote())
                .build();
        booking = bookingRepository.save(booking);

        // 5. Save Booking Slots & CourtSlotBooking — Calculate prices from DB rules
        BigDecimal totalCourtAmount = BigDecimal.ZERO;
        LocalDate bookingDate = LocalDate.parse(request.getBookingDate());
        DayType dayType = getDayType(bookingDate);
        Map<Integer, List<FacilityPriceRule>> priceRulesCache = new HashMap<>();

        if (request.getSlots() != null && !request.getSlots().isEmpty()) {
            for (OnSiteBookingRequestDTO.SlotItemDTO slotDto : request.getSlots()) {
                Court court = courtRepository.findById(slotDto.getCourtId()).orElse(null);
                if (court == null) continue;

                LocalTime startTime = LocalTime.parse(slotDto.getStartTime());
                String endStr = slotDto.getEndTime();
                LocalTime endTime = endStr.equals("23:59") ? LocalTime.MAX : LocalTime.parse(endStr);
                
                // Server-side slot price lookup from DB rules
                Integer fSportId = court.getFacilitySport().getId();
                List<FacilityPriceRule> priceRules = priceRulesCache.computeIfAbsent(fSportId,
                        id -> facilityPriceRuleRepository.findByFacilitySportIdAndIsActiveTrue(id));
                BigDecimal price = calculatePrice(startTime, endTime, priceRules, dayType);
                if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                    price = slotDto.getPrice() != null ? slotDto.getPrice() : BigDecimal.ZERO;
                }
                totalCourtAmount = totalCourtAmount.add(price);

                SlotStatus slotSt = SlotStatus.PENDING;
                BookingSlot bookingSlot = BookingSlot.builder()
                        .booking(booking)
                        .court(court)
                        .bookingDate(bookingDate)
                        .startTime(startTime)
                        .endTime(endTime)
                        .priceSnapshot(price)
                        .slotStatus(slotSt)
                        .build();
                bookingSlot = bookingSlotRepository.save(bookingSlot);

                CourtSlotBooking csb = CourtSlotBooking.builder()
                        .court(court)
                        .bookingDate(bookingDate)
                        .startTime(startTime)
                        .endTime(endTime)
                        .bookingSlot(bookingSlot)
                        .build();
                courtSlotBookingRepository.save(csb);
            }
        }

        // 6. Save Add-on Services (OrderItems) — Use product price from DB
        BigDecimal totalProductAmount = BigDecimal.ZERO;
        if (request.getServices() != null && !request.getServices().isEmpty()) {
            for (OnSiteBookingRequestDTO.ServiceItemDTO svcDto : request.getServices()) {
                Product product = productRepository.findById(svcDto.getProductId()).orElse(null);
                if (product == null || !Boolean.TRUE.equals(product.getIsActive())) continue;

                int qty = svcDto.getQuantity() != null && svcDto.getQuantity() > 0 ? svcDto.getQuantity() : 1;
                BigDecimal unitPrice = product.getPrice(); // Always use DB unit price for security
                BigDecimal lineTotal = unitPrice.multiply(new BigDecimal(qty));
                totalProductAmount = totalProductAmount.add(lineTotal);

                OrderItem item = OrderItem.builder()
                        .booking(booking)
                        .product(product)
                        .quantity(qty)
                        .unitPriceSnapshot(unitPrice)
                        .totalAmount(lineTotal)
                        .addedBy(staffEntity != null ? "STAFF" : "OWNER")
                        .build();
                orderItemRepository.save(item);
            }
        }

        // 7. Save Invoice
        BigDecimal subtotal = totalCourtAmount.add(totalProductAmount);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.subtract(discountAmount);

        BigDecimal paidAmount = totalCourtAmount; // Tiền sân đã trả tại quầy (tiền mặt / QR chuyển khoản)
        InvoiceStatus iStatus = (totalProductAmount.compareTo(BigDecimal.ZERO) == 0) ? InvoiceStatus.PAID : InvoiceStatus.PARTIAL;

        Invoice invoice = Invoice.builder()
                .booking(booking)
                .courtAmount(totalCourtAmount)
                .productAmount(totalProductAmount)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .depositPercent(100)
                .paymentStatus(iStatus)
                .refundDue(BigDecimal.ZERO)
                .refundStatus(com.mvc.mock_project.entities.enums.RefundStatus.NONE)
                .build();
        invoice = invoiceRepository.save(invoice);

        booking.setInvoice(invoice);
        bookingRepository.save(booking);

        // Record Payment for Court Fee (CASH / BANK_TRANSFER)
        if (totalCourtAmount.compareTo(BigDecimal.ZERO) > 0) {
            Payment payment = Payment.builder()
                    .invoice(invoice)
                    .paidAmount(totalCourtAmount)
                    .paymentTime(LocalDateTime.now())
                    .paymentType(PaymentType.DEPOSIT)
                    .method(pMethod)
                    .paymentStatus(PaymentStatus.PAID)
                    .transactionCode("ONSITE-" + pMethod.name() + "-" + System.currentTimeMillis())
                    .staffConfirm(staffEntity)
                    .confirmTime(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);
        }

        return invoice;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBookingDetailForOwner(Integer bookingId) {
        if (bookingId == null) return null;
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return null;

        Map<String, Object> detail = new HashMap<>();
        detail.put("bookingId", booking.getId());
        detail.put("bookingStatus", booking.getBookingStatus() != null ? booking.getBookingStatus().name() : "PENDING");
        detail.put("note", booking.getNote() != null ? booking.getNote() : "");
        detail.put("createdAt", booking.getCreatedAt() != null ? booking.getCreatedAt().toString() : "");

        String name = "N/A";
        String phone = "N/A";
        String email = "N/A";
        if (booking.getGuest() != null) {
            name = booking.getGuest().getGuestName();
            phone = booking.getGuest().getPhone();
            email = booking.getGuest().getEmail();
        } else if (booking.getAccount() != null) {
            name = booking.getAccount().getFullName();
            phone = booking.getAccount().getPhone();
            email = booking.getAccount().getEmail();
        }
        detail.put("bookerName", name != null ? name : "N/A");
        detail.put("bookerPhone", phone != null ? phone : "N/A");
        detail.put("bookerEmail", email != null ? email : "N/A");

        Invoice invoice = booking.getInvoice();
        BigDecimal courtAmt = BigDecimal.ZERO;
        BigDecimal productAmt = BigDecimal.ZERO;
        BigDecimal totalAmt = BigDecimal.ZERO;
        BigDecimal paidAmt = BigDecimal.ZERO;

        if (invoice != null) {
            courtAmt = invoice.getCourtAmount() != null ? invoice.getCourtAmount() : BigDecimal.ZERO;
            productAmt = invoice.getProductAmount() != null ? invoice.getProductAmount() : BigDecimal.ZERO;
            totalAmt = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
            paidAmt = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
            detail.put("paymentStatus", invoice.getPaymentStatus() != null ? invoice.getPaymentStatus().name() : "UNPAID");
        } else {
            detail.put("paymentStatus", "UNPAID");
        }

        BigDecimal remainingAmt = totalAmt.subtract(paidAmt);
        if (remainingAmt.compareTo(BigDecimal.ZERO) < 0) remainingAmt = BigDecimal.ZERO;

        detail.put("courtAmount", courtAmt);
        detail.put("productAmount", productAmt);
        detail.put("totalAmount", totalAmt);
        detail.put("paidAmount", paidAmt);
        detail.put("remainingAmount", remainingAmt);

        List<Map<String, Object>> slots = new ArrayList<>();
        List<Map<String, Object>> groupedBlocks = new ArrayList<>();

        if (booking.getBookingSlots() != null && !booking.getBookingSlots().isEmpty()) {
            List<BookingSlot> sortedSlots = new ArrayList<>(booking.getBookingSlots());
            sortedSlots.sort(Comparator.comparing(BookingSlot::getBookingDate)
                    .thenComparing(s -> s.getCourt() != null ? s.getCourt().getId() : 0)
                    .thenComparing(BookingSlot::getStartTime));

            for (BookingSlot bs : sortedSlots) {
                Map<String, Object> sMap = new HashMap<>();
                sMap.put("slotId", bs.getId());
                String cName = "Sân";
                try {
                    if (bs.getCourt() != null && bs.getCourt().getCourtName() != null) {
                        cName = bs.getCourt().getCourtName();
                    }
                } catch (Exception ignored) {}
                sMap.put("courtName", cName);
                sMap.put("bookingDate", bs.getBookingDate() != null ? bs.getBookingDate().toString() : "");
                sMap.put("startTime", bs.getStartTime() != null ? formatTime(bs.getStartTime()) : "");
                sMap.put("endTime", bs.getEndTime() != null ? formatTime(bs.getEndTime()) : "");
                sMap.put("price", bs.getPriceSnapshot() != null ? bs.getPriceSnapshot() : BigDecimal.ZERO);
                sMap.put("slotStatus", bs.getSlotStatus() != null ? bs.getSlotStatus().name() : "PENDING");
                sMap.put("checkinTime", bs.getCheckinTime() != null ? bs.getCheckinTime().toString() : "");
                sMap.put("checkoutTime", bs.getCheckoutTime() != null ? bs.getCheckoutTime().toString() : "");
                slots.add(sMap);
            }

            Map<Integer, List<BookingSlot>> courtSlotGroup = sortedSlots.stream()
                    .collect(Collectors.groupingBy(s -> s.getCourt() != null ? s.getCourt().getId() : 0, LinkedHashMap::new, Collectors.toList()));

            for (List<BookingSlot> cSlots : courtSlotGroup.values()) {
                if (cSlots.isEmpty()) continue;
                List<BookingSlot> currentBlock = new ArrayList<>();
                currentBlock.add(cSlots.get(0));

                for (int i = 1; i < cSlots.size(); i++) {
                    BookingSlot prev = cSlots.get(i - 1);
                    BookingSlot curr = cSlots.get(i);
                    boolean isContinuous = prev.getEndTime().equals(curr.getStartTime())
                            && prev.getSlotStatus() == curr.getSlotStatus()
                            && Objects.equals(prev.getBookingDate(), curr.getBookingDate());
                    if (isContinuous) {
                        currentBlock.add(curr);
                    } else {
                        groupedBlocks.add(buildSlotBlockMap(currentBlock));
                        currentBlock = new ArrayList<>();
                        currentBlock.add(curr);
                    }
                }
                if (!currentBlock.isEmpty()) {
                    groupedBlocks.add(buildSlotBlockMap(currentBlock));
                }
            }
        }
        detail.put("slots", slots);
        detail.put("groupedSlotBlocks", groupedBlocks);

        List<Map<String, Object>> services = new ArrayList<>();
        if (booking.getOrderItems() != null) {
            for (OrderItem item : booking.getOrderItems()) {
                Map<String, Object> iMap = new HashMap<>();
                String pName = "Dịch vụ";
                String unitStr = "";
                try {
                    if (item.getProduct() != null) {
                        if (item.getProduct().getProductName() != null) pName = item.getProduct().getProductName();
                        if (item.getProduct().getRentalUnit() != null) unitStr = item.getProduct().getRentalUnit();
                    }
                } catch (Exception ignored) {}
                iMap.put("productName", pName);
                iMap.put("unit", unitStr);
                iMap.put("quantity", item.getQuantity() != null ? item.getQuantity() : 1);
                iMap.put("unitPrice", item.getUnitPriceSnapshot() != null ? item.getUnitPriceSnapshot() : BigDecimal.ZERO);
                iMap.put("totalAmount", item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO);
                services.add(iMap);
            }
        }
        detail.put("services", services);

        return detail;
    }

    private Map<String, Object> buildSlotBlockMap(List<BookingSlot> block) {
        Map<String, Object> map = new HashMap<>();
        BookingSlot first = block.get(0);
        BookingSlot last = block.get(block.size() - 1);

        List<Integer> slotIds = block.stream().map(BookingSlot::getId).collect(Collectors.toList());
        String cName = first.getCourt() != null ? first.getCourt().getCourtName() : "Sân";
        BigDecimal totalPrice = block.stream()
                .map(s -> s.getPriceSnapshot() != null ? s.getPriceSnapshot() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        map.put("slotIds", slotIds);
        map.put("courtName", cName);
        map.put("bookingDate", first.getBookingDate() != null ? first.getBookingDate().toString() : "");
        map.put("startTime", formatTime(first.getStartTime()));
        map.put("endTime", formatTime(last.getEndTime()));
        map.put("slotStatus", first.getSlotStatus() != null ? first.getSlotStatus().name() : "PENDING");
        map.put("totalPrice", totalPrice);
        return map;
    }

    @Override
    @Transactional
    public Map<String, Object> checkInSlots(List<Integer> slotIds, Account ownerAccount) {
        if (slotIds == null || slotIds.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one court slot to check-in");
        }

        List<BookingSlot> slots = bookingSlotRepository.findAllById(slotIds);
        if (slots.isEmpty()) {
            throw new IllegalArgumentException("Court slot information not found");
        }

        Booking booking = slots.get(0).getBooking();
        if (ownerAccount != null && booking.getFacility() != null) {
            Facility facility = booking.getFacility();
            boolean isOwner = facility.getOwner() != null && facility.getOwner().getId().equals(ownerAccount.getId());
            boolean isStaff = staffRepository.findByAccountId(ownerAccount.getId())
                    .map(s -> s.getFacility() != null && s.getFacility().getId().equals(facility.getId()))
                    .orElse(false);
            if (!isOwner && !isStaff) {
                throw new org.springframework.security.access.AccessDeniedException("You do not have permission to manage this order");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        for (BookingSlot bs : slots) {
            bs.setSlotStatus(SlotStatus.CHECKED_IN);
            if (bs.getCheckinTime() == null) {
                bs.setCheckinTime(now);
            }
        }
        bookingSlotRepository.saveAll(slots);

        if (booking.getCheckinTime() == null) {
            booking.setCheckinTime(now);
        }
        if (booking.getBookingStatus() == BookingStatus.PENDING) {
            booking.setBookingStatus(BookingStatus.CONFIRMED);
        }
        bookingRepository.save(booking);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Court slot check-in successful!");
        resp.put("bookingId", booking.getId());
        return resp;
    }

    @Override
    @Transactional
    public Map<String, Object> checkOutAndSettle(Integer bookingId, List<Integer> slotIds, String paymentMethod, Account ownerAccount) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking order not found #" + bookingId));

        if (ownerAccount != null && booking.getFacility() != null) {
            Facility facility = booking.getFacility();
            boolean isOwner = facility.getOwner() != null && facility.getOwner().getId().equals(ownerAccount.getId());
            boolean isStaff = staffRepository.findByAccountId(ownerAccount.getId())
                    .map(s -> s.getFacility() != null && s.getFacility().getId().equals(facility.getId()))
                    .orElse(false);
            if (!isOwner && !isStaff) {
                throw new org.springframework.security.access.AccessDeniedException("You do not have permission to manage this order");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<BookingSlot> targetSlots;
        if (slotIds != null && !slotIds.isEmpty()) {
            targetSlots = bookingSlotRepository.findAllById(slotIds);
        } else {
            targetSlots = booking.getBookingSlots() != null ? booking.getBookingSlots() : Collections.emptyList();
        }

        for (BookingSlot bs : targetSlots) {
            bs.setSlotStatus(SlotStatus.CHECKED_OUT);
            if (bs.getCheckoutTime() == null) {
                bs.setCheckoutTime(now);
            }
        }
        if (!targetSlots.isEmpty()) {
            bookingSlotRepository.saveAll(targetSlots);
        }

        List<BookingSlot> allSlots = booking.getBookingSlots();
        boolean allCheckedOut = allSlots != null && !allSlots.isEmpty() && allSlots.stream()
                .allMatch(bs -> bs.getSlotStatus() == SlotStatus.CHECKED_OUT);

        if (allCheckedOut) {
            booking.setBookingStatus(BookingStatus.COMPLETED);
            if (booking.getCheckoutTime() == null) {
                booking.setCheckoutTime(now);
            }
            bookingRepository.save(booking);
        }

        Invoice invoice = booking.getInvoice();
        if (invoice != null) {
            BigDecimal totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal paidAmount = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal remaining = totalAmount.subtract(paidAmount);
            if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                Staff staffEntity = null;
                if (ownerAccount != null) {
                    staffEntity = staffRepository.findByAccountId(ownerAccount.getId()).orElse(null);
                }

                PaymentMethod method = PaymentMethod.CASH;
                if ("BANK_TRANSFER".equalsIgnoreCase(paymentMethod) || "QR".equalsIgnoreCase(paymentMethod)) {
                    method = PaymentMethod.BANK_TRANSFER;
                } else if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
                    method = PaymentMethod.VNPAY;
                }
                Payment settlement = Payment.builder()
                        .invoice(invoice)
                        .paidAmount(remaining)
                        .paymentTime(now)
                        .paymentType(PaymentType.REMAINING)
                        .method(method)
                        .paymentStatus(PaymentStatus.PAID)
                        .transactionCode("SETTLE-" + System.currentTimeMillis())
                        .staffConfirm(staffEntity)
                        .confirmTime(now)
                        .build();
                paymentRepository.save(settlement);

                invoice.setPaidAmount(totalAmount);
                invoice.setPaymentStatus(InvoiceStatus.PAID);
                invoiceRepository.save(invoice);
            } else if (allCheckedOut && invoice.getPaymentStatus() != InvoiceStatus.PAID) {
                invoice.setPaymentStatus(InvoiceStatus.PAID);
                invoiceRepository.save(invoice);
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", allCheckedOut ? "Successfully checked out all slots and settled the order!" : "Slot check-out successful!");
        resp.put("bookingId", booking.getId());
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOwnerBookingTimeline(Integer facilitySportId, String date, Integer userAccountId) {
        LocalDate bookingDate = LocalDate.parse(date);

        FacilitySport fs = facilitySportRepository.findById(facilitySportId)
                .orElseThrow(() -> new RuntimeException("FacilitySport not found"));

        Facility facility = fs.getFacility();
        if (userAccountId != null) {
            Optional<Staff> staffOpt = staffRepository.findByAccountId(userAccountId);
            if (staffOpt.isPresent()) {
                Staff staff = staffOpt.get();
                if (staff.getFacility() == null || !staff.getFacility().getId().equals(facility.getId())) {
                    throw new org.springframework.security.access.AccessDeniedException("Nhân viên không có quyền quản lý cơ sở này");
                }
            } else {
                if (facility.getOwner() == null || !facility.getOwner().getId().equals(userAccountId)) {
                    throw new org.springframework.security.access.AccessDeniedException("Bạn không phải là chủ sở hữu của cơ sở này");
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("date", date);
        response.put("slotDurationMinutes", fs.getSlotStepMinutes());
        response.put("minDurationMinutes", fs.getMinDurationMinutes());

        List<Court> courts = courtRepository.findByFacilitySportIdAndIsActiveTrue(facilitySportId);
        List<Integer> courtIds = courts.stream().map(Court::getId).collect(Collectors.toList());

        List<CourtSlotBooking> bookedSlots = new ArrayList<>();
        if (!courtIds.isEmpty()) {
            bookedSlots = courtSlotBookingRepository.findActiveHoldsByCourtIdsAndDate(courtIds, bookingDate);
        }

        List<FacilityPriceRule> priceRules = fs.getPriceRules() != null ? fs.getPriceRules() : new ArrayList<>();
        DayType dayType = getDayType(bookingDate);

        List<Map<String, Object>> courtsData = new ArrayList<>();
        for (Court court : courts) {
            Map<String, Object> cMap = new HashMap<>();
            cMap.put("courtId", court.getId());
            cMap.put("courtName", court.getCourtName());

            List<Map<String, Object>> slots = generateSlots(
                    fs.getFacility().getOpenTime(),
                    fs.getFacility().getCloseTime(),
                    fs.getSlotStepMinutes(),
                    court.getId(),
                    bookedSlots,
                    priceRules,
                    dayType,
                    bookingDate
            );
            cMap.put("slots", slots);
            courtsData.add(cMap);
        }

        response.put("courts", courtsData);
        return response;
    }

    private List<Map<String, Object>> generateSlots(
            LocalTime openTime, LocalTime closeTime, int stepMins,
            Integer courtId, List<CourtSlotBooking> bookedSlots,
            List<FacilityPriceRule> priceRules,
            DayType dayType,
            LocalDate bookingDate) {

        List<Map<String, Object>> slots = new ArrayList<>();
        int slotIndex = 0;
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        LocalTime curr = openTime;
        while (curr.isBefore(closeTime)) {
            LocalTime next = curr.plusMinutes(stepMins);
            if (next.isAfter(closeTime) || (next.isBefore(curr) && next.equals(LocalTime.MIDNIGHT))) {
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

            String status = "AVAILABLE";
            String bookerName = "";
            String bookerPhone = "";
            Integer bookingId = null;

            for (CourtSlotBooking bs : bookedSlots) {
                if (bs.getCourt().getId().equals(courtId)) {
                    if (curr.isBefore(bs.getEndTime()) && next.isAfter(bs.getStartTime())) {
                        if (bs.getBookingSlot() != null && bs.getBookingSlot().getBooking() != null) {
                            Booking booking = bs.getBookingSlot().getBooking();
                            bookingId = booking.getId();
                            if (booking.getBookingStatus() == BookingStatus.PENDING) {
                                status = "HOLD";
                            } else {
                                status = "BOOKED";
                            }
                            if (booking.getGuest() != null) {
                                bookerName = booking.getGuest().getGuestName();
                                bookerPhone = booking.getGuest().getPhone();
                            } else if (booking.getAccount() != null) {
                                bookerName = booking.getAccount().getFullName();
                                bookerPhone = booking.getAccount().getPhone();
                            }
                        } else {
                            status = "HOLD";
                        }
                        break;
                    }
                }
            }

            BigDecimal price = calculatePrice(curr, next, priceRules, dayType);
            boolean isPast = bookingDate.isBefore(today) || (bookingDate.isEqual(today) && next.isBefore(nowTime));

            if ("AVAILABLE".equals(status)) {
                if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                    status = "UNPRICED";
                } else if (isPast) {
                    status = "PAST";
                }
            }

            slot.put("status", status);
            slot.put("bookerName", bookerName);
            slot.put("bookerPhone", bookerPhone);
            slot.put("bookingId", bookingId);
            slot.put("price", price != null ? price : BigDecimal.ZERO);
            slot.put("isPast", isPast);

            slots.add(slot);
            slotIndex++;
            curr = next;
            if (curr.equals(LocalTime.MAX) || curr.equals(closeTime)) {
                break;
            }
        }

        return slots;
    }

}
