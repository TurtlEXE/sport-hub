package com.mvc.mock_project.controller.web.booking_process;

import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.Invoice;
import com.mvc.mock_project.security.CustomOAuth2User;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.BookingService;
import com.mvc.mock_project.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;
    private final BookingService bookingService;

    @PostMapping("/create-payment")
    public String createPayment(
            @RequestParam(value = "courtAmount", defaultValue = "0") BigDecimal courtAmount,
            @RequestParam(value = "originalCourtAmount", required = false) BigDecimal originalCourtAmount,
            @RequestParam(value = "productAmount", defaultValue = "0") BigDecimal productAmount,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "guestName", required = false) String guestName,
            @RequestParam(value = "guestPhone", required = false) String guestPhone,
            @RequestParam(value = "venueId", defaultValue = "1") Integer venueId,
            @RequestParam(value = "slotsJson", required = false) String slotsJson,
            @RequestParam(value = "bookingDate", required = false) String bookingDate,
            @RequestParam(value = "voucherId", required = false) Integer voucherId,
            @RequestParam(value = "voucherPlatformId", required = false) Integer voucherPlatformId,
            HttpServletRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                account = ((CustomUserDetails) principal).getAccount();
            } else if (principal instanceof CustomOAuth2User) {
                account = ((CustomOAuth2User) principal).getAccount();
            }
        }

        if (account != null && (email == null || email.trim().isEmpty())) {
            email = account.getEmail();
        }

        BigDecimal baseCourt = (originalCourtAmount != null) ? originalCourtAmount : courtAmount;
        Invoice invoice = bookingService.createBookingTransaction(
                guestName, guestPhone, email, baseCourt, productAmount, venueId, slotsJson, bookingDate, account, voucherId, voucherPlatformId);

        String orderInfo = "Thanh toan tien san - Invoice ID: " + invoice.getId();
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        String returnUrl = baseUrl + "/api/payment/vnpay-return";

        String paymentUrl = vnPayService.createOrder(invoice.getId().toString(), courtAmount, orderInfo, returnUrl, request);
        return "redirect:" + paymentUrl;
    }

    @GetMapping("/vnpay-return")
    public String paymentCompleted(HttpServletRequest request, Model model) {
        int paymentStatus = vnPayService.orderReturn(request);
        String invoiceIdStr = request.getParameter("vnp_TxnRef");
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                email = ((CustomUserDetails) principal).getAccount().getEmail();
            } else if (principal instanceof CustomOAuth2User) {
                email = ((CustomOAuth2User) principal).getAccount().getEmail();
            }
        }

        if (paymentStatus == 1) {
            if (invoiceIdStr != null && !invoiceIdStr.isEmpty()) {
                try {
                    Integer invId = Integer.parseInt(invoiceIdStr);
                    bookingService.processPaymentSuccess(invId, totalPrice, transactionId, paymentTime, email);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            model.addAttribute("status", "SUCCESS");
            model.addAttribute("message", "Thanh toán thành công. Trạng thái đơn hàng: PARTIAL (Đã trả tiền sân, chưa trả tiền dịch vụ).");
        } else if (paymentStatus == 0) {
            model.addAttribute("status", "FAILED");
            model.addAttribute("message", "Thanh toán thất bại hoặc đã bị hủy.");
        } else {
            model.addAttribute("status", "ERROR");
            model.addAttribute("message", "Chữ ký không hợp lệ, phát hiện can thiệp dữ liệu!");
        }

        model.addAttribute("orderId", orderInfo != null ? orderInfo : invoiceIdStr);
        if (totalPrice != null) {
            BigDecimal actualAmount = new BigDecimal(totalPrice).divide(new BigDecimal(100));
            model.addAttribute("totalPrice", actualAmount + " VND");
        } else {
            model.addAttribute("totalPrice", "N/A");
        }
        model.addAttribute("paymentTime", paymentTime);
        model.addAttribute("transactionId", transactionId);

        return "booking/payment-result";
    }

    @GetMapping("/vnpay-ipn")
    @ResponseBody
    public Map<String, String> paymentIpn(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        int paymentStatus = vnPayService.orderReturn(request);
        String invoiceIdStr = request.getParameter("vnp_TxnRef");
        String totalPrice = request.getParameter("vnp_Amount");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");

        if (paymentStatus == 1) {
            if (invoiceIdStr != null && !invoiceIdStr.isEmpty()) {
                try {
                    Integer invId = Integer.parseInt(invoiceIdStr);
                    bookingService.processPaymentSuccess(invId, totalPrice, transactionId, paymentTime, null);
                    response.put("RspCode", "00");
                    response.put("Message", "Confirm Success");
                    return response;
                } catch (Exception e) {
                    response.put("RspCode", "99");
                    response.put("Message", "Unknown error");
                    return response;
                }
            }
        } else if (paymentStatus == 0) {
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return response;
        }

        response.put("RspCode", "97");
        response.put("Message", "Invalid Checksum");
        return response;
    }
}
