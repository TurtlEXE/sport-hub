package com.mvc.mock_project.entities;

import com.mvc.mock_project.entities.enums.CommissionChangeType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CommissionChangeLog")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommissionChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_log_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_tier_id")
    private CommissionTier oldTier;

    @Column(name = "old_rate", precision = 5, scale = 4)
    private BigDecimal oldRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_tier_id", nullable = false)
    private CommissionTier newTier;

    @Column(name = "new_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal newRate;

    @Column(name = "min_price_per_minute", nullable = false, precision = 12, scale = 2)
    private BigDecimal minPricePerMinute;

    @Column(name = "max_price_per_minute", precision = 12, scale = 2)
    private BigDecimal maxPricePerMinute;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "announced_at", nullable = false)
    private LocalDateTime announcedAt;

    @Column(name = "notice_days", nullable = false)
    private Integer noticeDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private CommissionChangeType changeType;

    @Column(length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private Account changedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
