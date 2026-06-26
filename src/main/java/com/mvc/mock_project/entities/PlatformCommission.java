package com.mvc.mock_project.entities;

import com.mvc.mock_project.entities.enums.CommissionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PlatformCommission")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlatformCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commission_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_account_id", nullable = false)
    private Account ownerAccount;

    @Column(name = "court_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal courtRevenue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_tier_id")
    private CommissionTier commissionTier;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate;

    @Column(name = "commission_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "voucher_cost_owner", precision = 12, scale = 2)
    private BigDecimal voucherCostOwner = BigDecimal.ZERO;

    @Column(name = "voucher_cost_platform", precision = 12, scale = 2)
    private BigDecimal voucherCostPlatform = BigDecimal.ZERO;

    @Column(name = "owner_payout", nullable = false, precision = 12, scale = 2)
    private BigDecimal ownerPayout;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CommissionStatus status = CommissionStatus.PENDING;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
