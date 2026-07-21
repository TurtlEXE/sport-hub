package com.mvc.mock_project.controller.api.profile;

import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.dto.request.CreateReviewRequest;
import com.mvc.mock_project.dto.response.booking.MyBookingDTO;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.security.CustomOAuth2User;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.CustomerBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/profile/bookings", "/api/bookings/my-bookings"})
@RequiredArgsConstructor
public class CustomerBookingApiController {

    private final CustomerBookingService customerBookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MyBookingDTO>>> getMyBookings(Authentication authentication) {
        Account account = extractAccount(authentication);
        List<MyBookingDTO> bookings = customerBookingService.getMyBookings(account.getId(), account.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Thành công", bookings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MyBookingDTO>> getBookingDetail(@PathVariable Integer id, Authentication authentication) {
        Account account = extractAccount(authentication);
        MyBookingDTO detail = customerBookingService.getBookingDetail(id, account.getId(), account.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Thành công", detail));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable Integer id,
                                                           @RequestParam(value = "reason", required = false) String reason,
                                                           Authentication authentication) {
        Account account = extractAccount(authentication);
        customerBookingService.cancelBooking(id, account.getId(), account.getEmail(), reason);
        return ResponseEntity.ok(ApiResponse.success("Đã hủy lịch đặt sân thành công."));
    }

    @PostMapping("/review")
    public ResponseEntity<ApiResponse<Void>> submitReview(@Valid @RequestBody CreateReviewRequest request,
                                                          Authentication authentication) {
        Account account = extractAccount(authentication);
        customerBookingService.submitReview(account.getId(), account.getEmail(), request);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi đánh giá thành công."));
    }

    private Account extractAccount(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Vui lòng đăng nhập");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getAccount();
        } else if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getAccount();
        }
        throw new RuntimeException("Tài khoản không hợp lệ");
    }
}
