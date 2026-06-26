package com.mvc.mock_project.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BookingChangeLog")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_court_id")
    private Court oldCourt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_court_id")
    private Court newCourt;

    @Column(name = "old_start_time")
    private java.time.LocalTime oldStartTime;

    @Column(name = "new_start_time")
    private java.time.LocalTime newStartTime;

    @Column(name = "old_end_time")
    private java.time.LocalTime oldEndTime;

    @Column(name = "new_end_time")
    private java.time.LocalTime newEndTime;

    @Column(name = "old_booking_date")
    private java.time.LocalDate oldBookingDate;

    @Column(name = "new_booking_date")
    private java.time.LocalDate newBookingDate;

    @Column(name = "change_type", length = 20)
    private String changeType; // CHANGE_COURT, CHANGE_TIME, CHANGE_DATE, CHANGE_MULTIPLE

    @Column(name = "change_time")
    private LocalDateTime changeTime;

    @Column(length = 255)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_staff_id")
    private Staff actorStaff;

    @Column(name = "change_action", length = 30)
    private String changeAction;

    @Column(name = "before_data", columnDefinition = "NVARCHAR(MAX)")
    private String beforeData;

    @Column(name = "after_data", columnDefinition = "NVARCHAR(MAX)")
    private String afterData;

    @Column(length = 500)
    private String reason;

    @Column(name = "etag_before", length = 64)
    private String etagBefore;

    @Column(name = "etag_after", length = 64)
    private String etagAfter;

    @Column(name = "refund_due", precision = 12, scale = 2)
    private java.math.BigDecimal refundDue;

    @PrePersist
    public void prePersist() {
        this.changeTime = LocalDateTime.now();
    }
}
