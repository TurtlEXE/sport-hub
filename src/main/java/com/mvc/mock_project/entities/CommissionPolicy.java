package com.mvc.mock_project.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CommissionPolicy")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommissionPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Integer id;

    @Column(name = "min_notice_days", nullable = false)
    private Integer minNoticeDays = 14;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private Account updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
