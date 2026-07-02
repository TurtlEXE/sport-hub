package com.mvc.mock_project.entities;

import com.mvc.mock_project.entities.enums.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Facility")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facility_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_account_id", nullable = false)
    private Account owner;

    @Column(nullable = false)
    private String name;

    private String province;
    private String district;
    private String ward;

    @Column(nullable = false)
    private String address;

    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "open_time", nullable = false)
    private java.time.LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private java.time.LocalTime closeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Relations
    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL)
    private java.util.List<FacilitySport> facilitySports;

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL)
    private java.util.List<FacilityImage> images;

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL)
    private java.util.List<Staff> staffList;
}
