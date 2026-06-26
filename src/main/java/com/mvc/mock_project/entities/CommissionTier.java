package com.mvc.mock_project.entities;

import com.mvc.mock_project.entities.enums.CommissionStatus;
import com.mvc.mock_project.entities.enums.TierStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CommissionTier")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommissionTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tier_id")
    private Integer id;

    @Column(name = "min_price_per_minute", nullable = false, precision = 12, scale = 2)
    private BigDecimal minPricePerMinute;

    @Column(name = "max_price_per_minute", precision = 12, scale = 2)
    private BigDecimal maxPricePerMinute;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TierStatus status = TierStatus.DRAFT;

    @Column(name = "announced_at")
    private LocalDateTime announcedAt;

    @Column(name = "notice_days")
    private Integer noticeDays;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Account createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
