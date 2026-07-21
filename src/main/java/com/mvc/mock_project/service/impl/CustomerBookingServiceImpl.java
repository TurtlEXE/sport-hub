package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.CreateReviewRequest;
import com.mvc.mock_project.dto.response.ReviewDTO;
import com.mvc.mock_project.dto.response.booking.BookingSlotDetailDTO;
import com.mvc.mock_project.dto.response.booking.MyBookingDTO;
import com.mvc.mock_project.entities.*;
import com.mvc.mock_project.entities.enums.BookingStatus;
import com.mvc.mock_project.entities.enums.SlotStatus;
import com.mvc.mock_project.repository.*;
import com.mvc.mock_project.service.CustomerBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerBookingServiceImpl implements CustomerBookingService {

    private final BookingRepository bookingRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final AccountRepository accountRepository;
    private final ReviewRepository reviewRepository;
    private final FacilityRepository facilityRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MyBookingDTO> getMyBookings(Integer accountId, String email) {
        List<Booking> bookings = bookingRepository.findMyBookings(accountId, email != null ? email : "");
        return bookings.stream()
                .map(this::toMyBookingDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MyBookingDTO getBookingDetail(Integer bookingId, Integer accountId, String email) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đặt sân"));
        verifyOwnership(booking, accountId, email);
        return toMyBookingDTO(booking);
    }

    @Override
    @Transactional
    public void cancelBooking(Integer bookingId, Integer accountId, String email, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đặt sân"));
        verifyOwnership(booking, accountId, email);

        if (booking.getBookingStatus() != BookingStatus.CONFIRMED && booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy lịch đặt ở trạng thái Đang chờ xử lý hoặc Đã xác nhận.");
        }

        List<BookingSlot> slots = booking.getBookingSlots();
        if (slots != null && !slots.isEmpty()) {
            LocalDate earliestDate = slots.stream()
                    .map(BookingSlot::getBookingDate)
                    .min(Comparator.naturalOrder())
                    .orElse(LocalDate.now());

            LocalTime earliestTime = slots.stream()
                    .filter(s -> s.getBookingDate().equals(earliestDate))
                    .map(BookingSlot::getStartTime)
                    .min(Comparator.naturalOrder())
                    .orElse(LocalTime.now());

            LocalDateTime earliestSlotDateTime = LocalDateTime.of(earliestDate, earliestTime);
            if (earliestSlotDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new RuntimeException("Bạn chỉ có thể hủy lịch đặt trước tối thiểu 2 giờ so với giờ thi đấu.");
            }

            for (BookingSlot slot : slots) {
                slot.setSlotStatus(SlotStatus.CANCELLED);
            }
            bookingSlotRepository.saveAll(slots);
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        if (reason != null && !reason.trim().isEmpty()) {
            booking.setNote((booking.getNote() != null ? booking.getNote() + "\n" : "") + "Lý do hủy: " + reason);
        }
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void submitReview(Integer accountId, String email, CreateReviewRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đặt sân"));
        verifyOwnership(booking, accountId, email);

        if (booking.getReview() != null) {
            throw new RuntimeException("Đơn đặt sân này đã được đánh giá trước đó.");
        }

        if (booking.getBookingStatus() != BookingStatus.CONFIRMED && booking.getBookingStatus() != BookingStatus.COMPLETED) {
            throw new RuntimeException("Chỉ có thể đánh giá các lịch đặt sân đã hoàn thành hoặc xác nhận.");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không hợp lệ"));

        Review review = Review.builder()
                .booking(booking)
                .account(account)
                .rating(request.getRating())
                .comment(request.getComment() != null ? request.getComment().trim() : "")
                .build();

        reviewRepository.save(review);
    }

    private void verifyOwnership(Booking booking, Integer accountId, String email) {
        boolean isOwnerByAccount = booking.getAccount() != null && booking.getAccount().getId().equals(accountId);
        boolean isOwnerByEmail = booking.getGuest() != null && booking.getGuest().getEmail() != null &&
                email != null && booking.getGuest().getEmail().equalsIgnoreCase(email);

        if (!isOwnerByAccount && !isOwnerByEmail) {
            throw new RuntimeException("Bạn không có quyền truy cập thông tin đặt sân này");
        }
    }

    private MyBookingDTO toMyBookingDTO(Booking booking) {
        Facility facility = booking.getFacility();
        Invoice invoice = booking.getInvoice();
        Review review = booking.getReview();
        List<BookingSlot> slots = booking.getBookingSlots();

        String facilityName = facility != null ? facility.getName() : "Sân thể thao";
        String facilityAddress = facility != null ? facility.getAddress() : "";
        String facilityPhone = (facility != null && facility.getOwner() != null) ? facility.getOwner().getPhone() : "";
        String facilityImage = (facility != null && facility.getImages() != null && !facility.getImages().isEmpty())
                ? facility.getImages().get(0).getImagePath()
                : "/images/venues/default-venue.jpg";

        String statusText = "Đang xử lý";
        String statusClass = "bg-amber-50 text-amber-600 border border-amber-200";
        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            statusText = "Đã xác nhận (Thành công)";
            statusClass = "bg-emerald-50 text-emerald-600 border border-emerald-200";
        } else if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            statusText = "Đã hủy";
            statusClass = "bg-rose-50 text-rose-600 border border-rose-200";
        } else if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
            statusText = "Hoàn thành";
            statusClass = "bg-blue-50 text-primary-blue border border-blue-200";
        } else if (booking.getBookingStatus() == BookingStatus.EXPIRED) {
            statusText = "Đã hết hạn";
            statusClass = "bg-slate-100 text-slate-600 border border-slate-300";
        }

        LocalDate primaryDate = LocalDate.now();
        List<String> courtNames = new ArrayList<>();
        List<BookingSlotDetailDTO> slotDTOs = new ArrayList<>();
        String timeRangeSummary = "--:--";

        if (slots != null && !slots.isEmpty()) {
            slots.sort(Comparator.comparing(BookingSlot::getBookingDate).thenComparing(BookingSlot::getStartTime));
            primaryDate = slots.get(0).getBookingDate();

            LocalTime minStartTime = slots.get(0).getStartTime();
            LocalTime maxEndTime = slots.get(0).getEndTime();

            for (BookingSlot slot : slots) {
                String cName = slot.getCourt() != null ? slot.getCourt().getCourtName() : "Sân";
                if (!courtNames.contains(cName)) {
                    courtNames.add(cName);
                }
                if (slot.getStartTime().isBefore(minStartTime)) minStartTime = slot.getStartTime();
                if (slot.getEndTime().isAfter(maxEndTime)) maxEndTime = slot.getEndTime();

                slotDTOs.add(BookingSlotDetailDTO.builder()
                        .id(slot.getId())
                        .courtName(cName)
                        .bookingDate(slot.getBookingDate())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .priceSnapshot(slot.getPriceSnapshot() != null ? slot.getPriceSnapshot() : BigDecimal.ZERO)
                        .slotStatus(slot.getSlotStatus() != null ? slot.getSlotStatus().name() : "PENDING")
                        .build());
            }

            timeRangeSummary = minStartTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + maxEndTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        BigDecimal courtAmount = invoice != null && invoice.getCourtAmount() != null ? invoice.getCourtAmount() : BigDecimal.ZERO;
        BigDecimal productAmount = invoice != null && invoice.getProductAmount() != null ? invoice.getProductAmount() : BigDecimal.ZERO;
        BigDecimal subtotal = invoice != null && invoice.getSubtotal() != null ? invoice.getSubtotal() : BigDecimal.ZERO;
        BigDecimal discountAmount = invoice != null && invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = invoice != null && invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paidAmount = invoice != null && invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
        String paymentStatus = invoice != null && invoice.getPaymentStatus() != null ? invoice.getPaymentStatus().name() : "UNPAID";
        String paymentStatusText = "Chưa thanh toán";
        if ("PAID".equals(paymentStatus)) paymentStatusText = "Đã thanh toán";
        else if ("PARTIALLY_PAID".equals(paymentStatus)) paymentStatusText = "Đã cọc 1 phần";
        else if ("REFUNDED".equals(paymentStatus)) paymentStatusText = "Đã hoàn tiền";

        String transCode = (invoice != null && invoice.getId() != null) ? "INV-" + invoice.getId() : "BKG-" + booking.getId();

        boolean canCancel = false;
        if (booking.getBookingStatus() == BookingStatus.CONFIRMED || booking.getBookingStatus() == BookingStatus.PENDING) {
            if (slots != null && !slots.isEmpty()) {
                BookingSlot firstSlot = slots.get(0);
                LocalDateTime slotDateTime = LocalDateTime.of(firstSlot.getBookingDate(), firstSlot.getStartTime());
                if (slotDateTime.isAfter(LocalDateTime.now().plusHours(2))) {
                    canCancel = true;
                }
            } else if (booking.getCreatedAt() != null && booking.getCreatedAt().plusDays(1).isAfter(LocalDateTime.now())) {
                canCancel = true;
            }
        }

        boolean canReview = false;
        if (review == null && (booking.getBookingStatus() == BookingStatus.CONFIRMED || booking.getBookingStatus() == BookingStatus.COMPLETED)) {
            if (slots != null && !slots.isEmpty()) {
                BookingSlot lastSlot = slots.get(slots.size() - 1);
                LocalDateTime slotEndDateTime = LocalDateTime.of(lastSlot.getBookingDate(), lastSlot.getEndTime());
                if (slotEndDateTime.isBefore(LocalDateTime.now()) || primaryDate.isBefore(LocalDate.now())) {
                    canReview = true;
                }
            } else {
                canReview = true;
            }
        }

        ReviewDTO reviewDTO = null;
        if (review != null) {
            reviewDTO = ReviewDTO.builder()
                    .id(review.getId())
                    .reviewerName(review.getAccount() != null ? review.getAccount().getFullName() : "Bạn")
                    .rating(review.getRating())
                    .comment(review.getComment())
                    .createdAt(review.getCreatedAt())
                    .build();
        }

        return MyBookingDTO.builder()
                .id(booking.getId())
                .transactionCode(transCode)
                .facilityId(facility != null ? facility.getId() : null)
                .facilityName(facilityName)
                .facilityAddress(facilityAddress)
                .facilityImage(facilityImage)
                .facilityPhone(facilityPhone)
                .bookingStatus(booking.getBookingStatus())
                .bookingStatusText(statusText)
                .statusBadgeClass(statusClass)
                .primaryBookingDate(primaryDate)
                .timeRangeSummary(timeRangeSummary)
                .courtNames(courtNames)
                .courtAmount(courtAmount)
                .productAmount(productAmount)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .paymentStatus(paymentStatus)
                .paymentStatusText(paymentStatusText)
                .createdAt(booking.getCreatedAt())
                .note(booking.getNote())
                .canCancel(canCancel)
                .canReview(canReview)
                .review(reviewDTO)
                .slots(slotDTOs)
                .build();
    }
}
