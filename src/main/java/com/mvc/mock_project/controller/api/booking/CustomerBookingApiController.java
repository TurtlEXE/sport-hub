package com.mvc.mock_project.controller.api.booking;

import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.Booking;
import com.mvc.mock_project.entities.BookingSlot;
import com.mvc.mock_project.entities.Invoice;
import com.mvc.mock_project.entities.OrderItem;
import com.mvc.mock_project.entities.enums.BookingStatus;
import com.mvc.mock_project.entities.enums.InvoiceStatus;
import com.mvc.mock_project.repository.BookingRepository;
import com.mvc.mock_project.repository.InvoiceRepository;
import com.mvc.mock_project.security.CustomOAuth2User;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/customer/bookings")
@RequiredArgsConstructor
public class CustomerBookingApiController {

    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final VNPayService vnPayService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getMyBookings() {
        Account account = getAuthenticatedAccount();
        if (account == null) {
            return ResponseEntity.status(401).build();
        }

        List<Booking> bookings = bookingRepository.findCustomerActiveBookings(account.getId(), LocalDateTime.now());
        List<Map<String, Object>> result = new ArrayList<>();

        for (Booking b : bookings) {
            Map<String, Object> map = new HashMap<>();
            map.put("bookingId", b.getId());
            map.put("bookingStatus", b.getBookingStatus() != null ? b.getBookingStatus().name() : "PENDING");
            map.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : "");
            map.put("holdExpiredAt", b.getHoldExpiredAt() != null ? b.getHoldExpiredAt().toString() : "");

            // Facility info
            if (b.getFacility() != null) {
                map.put("facilityId", b.getFacility().getId());
                map.put("facilityName", b.getFacility().getName());
                String address = b.getFacility().getAddress() != null ? b.getFacility().getAddress() : "";
                if (b.getFacility().getWard() != null) address += ", " + b.getFacility().getWard();
                if (b.getFacility().getDistrict() != null) address += ", " + b.getFacility().getDistrict();
                if (b.getFacility().getProvince() != null) address += ", " + b.getFacility().getProvince();
                map.put("facilityAddress", address);
            }

            // Customer / Guest info
            String customerName = account.getFullName();
            String customerPhone = account.getPhone();
            if (b.getGuest() != null) {
                customerName = b.getGuest().getGuestName();
                customerPhone = b.getGuest().getPhone();
            }
            map.put("customerName", customerName != null ? customerName : "Customer");
            map.put("customerPhone", customerPhone != null ? customerPhone : "N/A");

            // Raw slots calculation & slot merging
            BigDecimal calculatedCourtTotal = BigDecimal.ZERO;
            if (b.getBookingSlots() != null) {
                for (BookingSlot bs : b.getBookingSlots()) {
                    if (bs.getPriceSnapshot() != null) {
                        calculatedCourtTotal = calculatedCourtTotal.add(bs.getPriceSnapshot());
                    }
                }
            }
            List<Map<String, Object>> mergedSlots = mergeConsecutiveSlots(b.getBookingSlots());
            map.put("slots", mergedSlots);

            // Read-only Add-on products / services
            List<Map<String, Object>> productList = new ArrayList<>();
            BigDecimal calculatedProductTotal = BigDecimal.ZERO;
            if (b.getOrderItems() != null) {
                for (OrderItem oi : b.getOrderItems()) {
                    Map<String, Object> pMap = new HashMap<>();
                    pMap.put("id", oi.getId());
                    pMap.put("productName", oi.getProduct() != null ? oi.getProduct().getProductName() : "Product/Service");
                    pMap.put("quantity", oi.getQuantity() != null ? oi.getQuantity() : 1);
                    BigDecimal unitPrice = oi.getUnitPriceSnapshot() != null ? oi.getUnitPriceSnapshot() : BigDecimal.ZERO;
                    BigDecimal totalAmount = oi.getTotalAmount() != null ? oi.getTotalAmount() : unitPrice.multiply(BigDecimal.valueOf(oi.getQuantity() != null ? oi.getQuantity() : 1));
                    pMap.put("unitPrice", unitPrice);
                    pMap.put("totalAmount", totalAmount);
                    calculatedProductTotal = calculatedProductTotal.add(totalAmount);
                    productList.add(pMap);
                }
            }
            map.put("products", productList);

            // Invoice / Payment info
            BigDecimal totalAmount = calculatedCourtTotal.add(calculatedProductTotal);
            BigDecimal paidAmount = BigDecimal.ZERO;
            String paymentStatusText = "Unpaid";
            boolean isHold = b.getBookingStatus() == BookingStatus.PENDING;

            if (b.getInvoice() != null) {
                Invoice inv = b.getInvoice();
                if (inv.getTotalAmount() != null && inv.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                    totalAmount = inv.getTotalAmount();
                }
                if (inv.getPaymentStatus() == InvoiceStatus.PAID) {
                    paidAmount = totalAmount;
                    paymentStatusText = "Fully Paid";
                } else if (inv.getPaymentStatus() == InvoiceStatus.PARTIAL) {
                    paidAmount = inv.getCourtAmount() != null ? inv.getCourtAmount() : BigDecimal.ZERO;
                    paymentStatusText = "Deposit Paid";
                }
            } else if (b.getBookingStatus() == BookingStatus.CONFIRMED) {
                paymentStatusText = "Deposit Paid";
            }

            map.put("totalAmount", totalAmount);
            map.put("paidAmount", paidAmount);
            map.put("remainingAmount", totalAmount.subtract(paidAmount));
            map.put("paymentStatusText", paymentStatusText);
            map.put("isHold", isHold);

            result.add(map);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable Integer bookingId) {
        Account account = getAuthenticatedAccount();
        if (account == null) {
            return ResponseEntity.status(401).build();
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getAccount() == null || !booking.getAccount().getId().equals(account.getId())) {
            return ResponseEntity.status(403).build();
        }

        if (booking.getBookingStatus() == BookingStatus.PENDING) {
            booking.setBookingStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Booking cancelled successfully.");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{bookingId}/continue-payment")
    public ResponseEntity<Map<String, Object>> continuePayment(
            @PathVariable Integer bookingId,
            HttpServletRequest request) {

        Account account = getAuthenticatedAccount();
        if (account == null) {
            return ResponseEntity.status(401).build();
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getAccount() == null || !booking.getAccount().getId().equals(account.getId())) {
            return ResponseEntity.status(403).build();
        }

        if (booking.getBookingStatus() != BookingStatus.PENDING || 
            (booking.getHoldExpiredAt() != null && booking.getHoldExpiredAt().isBefore(LocalDateTime.now()))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Reservation expired or cannot be paid."));
        }

        Invoice invoice = booking.getInvoice();
        if (invoice == null) {
            invoice = invoiceRepository.findByBookingId(bookingId).orElse(null);
        }

        if (invoice == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invoice not found for this booking."));
        }

        BigDecimal courtPayable = invoice.getCourtAmount() != null ? invoice.getCourtAmount() : invoice.getTotalAmount();
        if (invoice.getDiscountAmount() != null) {
            courtPayable = courtPayable.subtract(invoice.getDiscountAmount());
        }
        if (courtPayable.compareTo(BigDecimal.ZERO) < 0) {
            courtPayable = BigDecimal.ZERO;
        }

        String orderInfo = "Payment for Venue Booking - Invoice ID: " + invoice.getId();
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String returnUrl = baseUrl + "/api/payment/vnpay-return";

        String paymentUrl = vnPayService.createOrder(
                String.valueOf(invoice.getId()),
                courtPayable,
                orderInfo,
                returnUrl,
                request
        );

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("paymentUrl", paymentUrl);
        return ResponseEntity.ok(resp);
    }

    private List<Map<String, Object>> mergeConsecutiveSlots(List<BookingSlot> rawSlots) {
        if (rawSlots == null || rawSlots.isEmpty()) {
            return Collections.emptyList();
        }

        // Group slots by Court Name and Booking Date
        Map<String, List<BookingSlot>> groupedMap = new LinkedHashMap<>();
        for (BookingSlot slot : rawSlots) {
            String courtName = (slot.getCourt() != null && slot.getCourt().getCourtName() != null) 
                    ? slot.getCourt().getCourtName() : "Court";
            String dateStr = slot.getBookingDate() != null ? slot.getBookingDate().toString() : "";
            String groupKey = courtName + "___" + dateStr;

            groupedMap.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(slot);
        }

        List<Map<String, Object>> mergedList = new ArrayList<>();

        for (Map.Entry<String, List<BookingSlot>> entry : groupedMap.entrySet()) {
            List<BookingSlot> slotsInGroup = entry.getValue();
            slotsInGroup.sort(Comparator.comparing(BookingSlot::getStartTime));

            String courtName = (slotsInGroup.get(0).getCourt() != null && slotsInGroup.get(0).getCourt().getCourtName() != null)
                    ? slotsInGroup.get(0).getCourt().getCourtName() : "Court";
            String dateStr = slotsInGroup.get(0).getBookingDate() != null ? slotsInGroup.get(0).getBookingDate().toString() : "";

            LocalTime currentStart = slotsInGroup.get(0).getStartTime();
            LocalTime currentEnd = slotsInGroup.get(0).getEndTime();
            BigDecimal currentPriceSum = slotsInGroup.get(0).getPriceSnapshot() != null ? slotsInGroup.get(0).getPriceSnapshot() : BigDecimal.ZERO;

            for (int i = 1; i < slotsInGroup.size(); i++) {
                BookingSlot nextSlot = slotsInGroup.get(i);
                BigDecimal nextPrice = nextSlot.getPriceSnapshot() != null ? nextSlot.getPriceSnapshot() : BigDecimal.ZERO;

                if (nextSlot.getStartTime() != null && currentEnd != null && nextSlot.getStartTime().equals(currentEnd)) {
                    currentEnd = nextSlot.getEndTime();
                    currentPriceSum = currentPriceSum.add(nextPrice);
                } else {
                    Map<String, Object> mergedMap = new HashMap<>();
                    mergedMap.put("courtName", courtName);
                    mergedMap.put("bookingDate", dateStr);
                    mergedMap.put("startTime", currentStart != null ? currentStart.toString() : "");
                    mergedMap.put("endTime", currentEnd != null ? currentEnd.toString() : "");
                    mergedMap.put("price", currentPriceSum);
                    mergedList.add(mergedMap);

                    currentStart = nextSlot.getStartTime();
                    currentEnd = nextSlot.getEndTime();
                    currentPriceSum = nextPrice;
                }
            }

            Map<String, Object> mergedMap = new HashMap<>();
            mergedMap.put("courtName", courtName);
            mergedMap.put("bookingDate", dateStr);
            mergedMap.put("startTime", currentStart != null ? currentStart.toString() : "");
            mergedMap.put("endTime", currentEnd != null ? currentEnd.toString() : "");
            mergedMap.put("price", currentPriceSum);
            mergedList.add(mergedMap);
        }

        return mergedList;
    }

    private Account getAuthenticatedAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getAccount();
            } else if (principal instanceof CustomOAuth2User) {
                return ((CustomOAuth2User) principal).getAccount();
            }
        }
        return null;
    }
}
