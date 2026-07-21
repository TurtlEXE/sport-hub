package com.mvc.mock_project.controller.web.profile;

import com.mvc.mock_project.dto.request.CreateReviewRequest;
import com.mvc.mock_project.dto.response.booking.MyBookingDTO;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.enums.BookingStatus;
import com.mvc.mock_project.security.CustomOAuth2User;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.CustomerBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CustomerBookingController {

    private final CustomerBookingService customerBookingService;

    @GetMapping({"/profile/bookings", "/my-bookings"})
    public String showMyBookings(@RequestParam(value = "status", required = false) String status,
                                 Authentication authentication,
                                 Model model) {
        Account account = extractAccount(authentication);
        List<MyBookingDTO> allBookings = customerBookingService.getMyBookings(account.getId(), account.getEmail());

        List<MyBookingDTO> filteredBookings = allBookings;
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            filteredBookings = allBookings.stream()
                    .filter(b -> b.getBookingStatus().name().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        long totalCount = allBookings.size();
        long confirmedCount = allBookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED).count();
        long pendingCount = allBookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.PENDING).count();
        long completedCount = allBookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.COMPLETED).count();
        long cancelledCount = allBookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.CANCELLED).count();

        model.addAttribute("bookings", filteredBookings);
        model.addAttribute("currentStatus", status != null ? status.toUpperCase() : "ALL");
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);

        return "profile/my-bookings";
    }

    @PostMapping("/profile/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Integer id,
                                @RequestParam(value = "reason", required = false) String reason,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            Account account = extractAccount(authentication);
            customerBookingService.cancelBooking(id, account.getId(), account.getEmail(), reason);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy lịch đặt sân thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hủy lịch đặt sân: " + e.getMessage());
        }
        return "redirect:/profile/bookings";
    }

    @PostMapping("/profile/bookings/review")
    public String submitReview(@ModelAttribute CreateReviewRequest request,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            Account account = extractAccount(authentication);
            customerBookingService.submitReview(account.getId(), account.getEmail(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá chất lượng sân!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi gửi đánh giá: " + e.getMessage());
        }
        return "redirect:/profile/bookings";
    }

    private Account extractAccount(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Vui lòng đăng nhập để xem lịch sử đặt sân");
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
