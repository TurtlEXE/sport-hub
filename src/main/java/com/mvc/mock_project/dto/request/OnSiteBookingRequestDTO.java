package com.mvc.mock_project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnSiteBookingRequestDTO {

    private Integer facilityId;
    private Integer facilitySportId;
    private String bookingDate;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Integer targetAccountId;
    private String note;
    private String paymentMethod; // CASH, VNPAY

    private List<SlotItemDTO> slots;
    private List<ServiceItemDTO> services;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotItemDTO {
        private Integer courtId;
        private String startTime;
        private String endTime;
        private BigDecimal price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceItemDTO {
        private Integer productId;
        private Integer quantity;
        private BigDecimal price;
    }
}
