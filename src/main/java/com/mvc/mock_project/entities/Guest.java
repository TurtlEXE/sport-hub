package com.mvc.mock_project.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Guest")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guest_id")
    private Integer id;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Column(nullable = false)
    private String phone;

    private String email;
}
