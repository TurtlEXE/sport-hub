package com.mvc.mock_project.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "OwnerProfile")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OwnerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_profile_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "tax_code")
    private String taxCode;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_no")
    private String bankAccountNo;

    @Column(name = "bank_account_name")
    private String bankAccountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private com.mvc.mock_project.entities.enums.ApprovalStatus approvalStatus
            = com.mvc.mock_project.entities.enums.ApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Account approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
