package com.mvc.mock_project.controller.api.booking;

import com.mvc.mock_project.dto.request.OnSiteBookingRequestDTO;
import com.mvc.mock_project.entities.Invoice;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.BookingService;
import com.mvc.mock_project.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner/booking")
@RequiredArgsConstructor
public class OwnerBookingApiController {

    private final BookingService bookingService;
    private final VNPayService vnPayService;
    private final com.mvc.mock_project.repository.AccountRepository accountRepository;

    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhoneAccount(@RequestParam String phone) {
        Map<String, Object> resp = new HashMap<>();
        if (phone == null || phone.trim().isEmpty()) {
            resp.put("exists", false);
            return ResponseEntity.ok(resp);
        }
        java.util.Optional<com.mvc.mock_project.entities.Account> accOpt = accountRepository.findByPhone(phone.trim());
        if (accOpt.isPresent()) {
            com.mvc.mock_project.entities.Account acc = accOpt.get();
            resp.put("exists", true);
            Map<String, Object> accMap = new HashMap<>();
            accMap.put("id", acc.getId());
            accMap.put("fullName", acc.getFullName());
            accMap.put("email", acc.getEmail());
            accMap.put("phone", acc.getPhone());
            resp.put("account", accMap);
        } else {
            resp.put("exists", false);
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/timeline")
    public ResponseEntity<Map<String, Object>> getOwnerBookingTimeline(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Integer facilitySportId,
            @RequestParam String date) {

        Integer ownerId = userDetails.getAccount().getId();
        Map<String, Object> timeline = bookingService.getOwnerBookingTimeline(facilitySportId, date, ownerId);
        return ResponseEntity.ok(timeline);
    }

    @PostMapping("/create-onsite")
    public ResponseEntity<Map<String, Object>> createOnSiteBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody OnSiteBookingRequestDTO request,
            HttpServletRequest httpRequest) {

        Invoice invoice = bookingService.createOnSiteBooking(request, userDetails != null ? userDetails.getAccount() : null);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("invoiceId", invoice.getId());

        String pm = request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "CASH";
        resp.put("paymentMethod", pm);
        resp.put("message", "🎉 On-site reservation created successfully! Payment recorded.");

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/products")
    public ResponseEntity<List<Map<String, Object>>> getFacilityProducts(@RequestParam Integer facilityId) {
        List<Map<String, Object>> products = bookingService.getFacilityProducts(facilityId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/detail/{bookingId}")
    public ResponseEntity<Map<String, Object>> getBookingDetail(@PathVariable Integer bookingId) {
        Map<String, Object> detail = bookingService.getBookingDetailForOwner(bookingId);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/checkin")
    public ResponseEntity<Map<String, Object>> checkInSlots(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, Object> body) {

        List<Integer> slotIds = null;
        if (body.get("slotIds") instanceof List) {
            List<?> rawList = (List<?>) body.get("slotIds");
            slotIds = rawList.stream().map(o -> Integer.parseInt(o.toString())).toList();
        }
        Map<String, Object> resp = bookingService.checkInSlots(slotIds, userDetails != null ? userDetails.getAccount() : null);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkOutAndSettle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, Object> body) {

        Integer bookingId = body.get("bookingId") != null ? Integer.parseInt(body.get("bookingId").toString()) : null;
        List<Integer> slotIds = null;
        if (body.get("slotIds") instanceof List) {
            List<?> rawList = (List<?>) body.get("slotIds");
            slotIds = rawList.stream().map(o -> Integer.parseInt(o.toString())).toList();
        }
        String paymentMethod = body.get("paymentMethod") != null ? body.get("paymentMethod").toString() : "CASH";

        Map<String, Object> resp = bookingService.checkOutAndSettle(bookingId, slotIds, paymentMethod, userDetails != null ? userDetails.getAccount() : null);
        return ResponseEntity.ok(resp);
    }
}
