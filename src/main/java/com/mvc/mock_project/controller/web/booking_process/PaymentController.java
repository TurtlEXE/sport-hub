package com.mvc.mock_project.controller.web.booking_process;

import com.mvc.mock_project.service.VNPayService;
import com.mvc.mock_project.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mvc.mock_project.service.BookingService;
import com.mvc.mock_project.repository.InvoiceRepository;
import com.mvc.mock_project.repository.BookingSlotRepository;
import com.mvc.mock_project.repository.BookingRepository;
import com.mvc.mock_project.entities.Invoice;
import com.mvc.mock_project.entities.Booking;
import com.mvc.mock_project.entities.BookingSlot;
import com.mvc.mock_project.entities.enums.InvoiceStatus;
import com.mvc.mock_project.entities.enums.BookingStatus;
import com.mvc.mock_project.entities.enums.SlotStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;
    private final EmailService emailService;
    private final BookingService bookingService;
    private final InvoiceRepository invoiceRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final BookingRepository bookingRepository;

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
            HttpServletRequest request) {
        
        // Extract logged-in Account if available
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.mvc.mock_project.entities.Account account = null;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.mvc.mock_project.security.CustomUserDetails) {
                account = ((com.mvc.mock_project.security.CustomUserDetails) principal).getAccount();
            } else if (principal instanceof com.mvc.mock_project.security.CustomOAuth2User) {
                account = ((com.mvc.mock_project.security.CustomOAuth2User) principal).getAccount();
            }
        }
        
        // If logged in and email is not provided, use the account's email
        if (account != null && (email == null || email.trim().isEmpty())) {
            email = account.getEmail();
        }

        // 1. Lưu Guest/Account, Booking, BookingSlot, Invoice vào DB trước
        BigDecimal baseCourt = (originalCourtAmount != null) ? originalCourtAmount : courtAmount;
        Invoice invoice = bookingService.createBookingTransaction(
                guestName, guestPhone, email, baseCourt, productAmount, venueId, slotsJson, bookingDate, account, voucherId);
        
        String orderInfo = "Thanh toan tien san - Invoice ID: " + invoice.getId();
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        
        // Gắn invoiceId vào url callback
        String returnUrl = baseUrl + "/api/payment/vnpay-return?invoiceId=" + invoice.getId() + "&email=" + email;
        
        // 2. Lấy URL VNPay
        String paymentUrl = vnPayService.createOrder(courtAmount, orderInfo, returnUrl, request);
        return "redirect:" + paymentUrl;
    }

    @GetMapping("/vnpay-return")
    public String paymentCompleted(HttpServletRequest request, Model model) {
        int paymentStatus = vnPayService.orderReturn(request);
        String email = request.getParameter("email");
        String invoiceIdStr = request.getParameter("invoiceId");

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount"); 

        if (paymentStatus == 1) {
            // Thanh toán thành công -> Update DB
            if (invoiceIdStr != null && !invoiceIdStr.isEmpty()) {
                try {
                    Integer invId = Integer.parseInt(invoiceIdStr);
                    Invoice invoice = invoiceRepository.findById(invId).orElse(null);
                    if (invoice != null) {
                        invoice.setPaymentStatus(InvoiceStatus.PARTIAL);
                        if (totalPrice != null) {
                            try {
                                invoice.setPaidAmount(new java.math.BigDecimal(totalPrice).divide(new java.math.BigDecimal(100)));
                            } catch (Exception e) {}
                        }
                        if (invoice.getBooking() != null) {
                            Booking booking = invoice.getBooking();
                            booking.setBookingStatus(BookingStatus.CONFIRMED);
                            
                            // Update booking slots if necessary
                            List<BookingSlot> slots = booking.getBookingSlots();
                            if (slots != null) {
                                for (BookingSlot slot : slots) {
                                    slot.setSlotStatus(SlotStatus.PENDING); // Slot remains PENDING until check-in
                                }
                                bookingSlotRepository.saveAll(slots);
                            }
                            bookingRepository.save(booking);
                        }
                        invoiceRepository.save(invoice);
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // Log error processing invoice ID
                }
            }
            
            // Gửi email xác nhận
            if (email != null && !email.isEmpty() && !email.equals("null")) {
                String bookingDetails = "Mã giao dịch: " + transactionId + "\n"
                        + "Thời gian: " + paymentTime + "\n"
                        + "Nội dung: " + orderInfo;
                emailService.sendPaymentSuccessEmail(email, bookingDetails);
            }
            
            model.addAttribute("status", "SUCCESS");
            model.addAttribute("message", "Thanh toán thành công. Trạng thái đơn hàng: PARTIAL (Đã trả tiền sân, chưa trả tiền dịch vụ).");
        } else if (paymentStatus == 0) {
            // Thanh toán thất bại
            model.addAttribute("status", "FAILED");
            model.addAttribute("message", "Thanh toán thất bại hoặc đã bị hủy.");
        } else {
            // Lỗi checksum
            model.addAttribute("status", "ERROR");
            model.addAttribute("message", "Chữ ký không hợp lệ, phát hiện can thiệp dữ liệu!");
        }

        model.addAttribute("orderId", orderInfo);
        if (totalPrice != null) {
            BigDecimal actualAmount = new BigDecimal(totalPrice).divide(new BigDecimal(100));
            model.addAttribute("totalPrice", actualAmount + " VND");
        } else {
            model.addAttribute("totalPrice", "N/A");
        }
        model.addAttribute("paymentTime", paymentTime);
        model.addAttribute("transactionId", transactionId);

        return "booking/payment-result"; // View kết quả
    }
}
