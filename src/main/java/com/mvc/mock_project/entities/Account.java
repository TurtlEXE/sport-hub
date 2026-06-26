package com.mvc.mock_project.entities;

import com.mvc.mock_project.entities.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Account")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer id;

    @Column(unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true)
    private String phone;

    @Column(name = "avatar_path")
    private String avatarPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Relations
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private OwnerProfile ownerProfile;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Staff> staffAssignments;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Booking> bookings;
}
