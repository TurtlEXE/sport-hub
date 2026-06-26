package com.mvc.mock_project.entities;

import com.mvc.mock_project.entities.enums.InvoiceStatus;
import com.mvc.mock_project.entities.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Invoice")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "court_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal courtAmount = BigDecimal.ZERO;

    @Column(name = "product_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal productAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "deposit_percent")
    private Integer depositPercent = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private InvoiceStatus paymentStatus = InvoiceStatus.UNPAID;

    @Column(name = "refund_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundDue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", length = 20)
    private RefundStatus refundStatus = RefundStatus.NONE;

    @Column(name = "refund_note", length = 500)
    private String refundNote;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
