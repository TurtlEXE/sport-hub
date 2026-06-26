package com.mvc.mock_project.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "EmailQueue")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_id")
    private Integer id;

    @Column(name = "email_type", nullable = false, length = 30)
    private String emailType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "to_email", nullable = false)
    private String toEmail;

    @Column(name = "payload_json", columnDefinition = "NVARCHAR(MAX)")
    private String payloadJson;

    @Column(name = "reminder_at")
    private LocalDateTime reminderAt;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.nextAttemptAt == null) this.nextAttemptAt = LocalDateTime.now();
    }
}
