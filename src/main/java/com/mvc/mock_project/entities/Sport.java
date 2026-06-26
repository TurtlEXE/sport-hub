package com.mvc.mock_project.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "Sport")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sport_id")
    private Integer id;

    @Column(name = "sport_code", unique = true, nullable = false, length = 30)
    private String sportCode;

    @Column(name = "sport_name", nullable = false)
    private String sportName;

    @Column(name = "icon_path")
    private String iconPath;

    @Column(name = "default_min_duration_minutes", nullable = false)
    private Integer defaultMinDurationMinutes = 30;

    @Column(name = "default_slot_step_minutes", nullable = false)
    private Integer defaultSlotStepMinutes = 30;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "sport", cascade = CascadeType.ALL)
    private List<SportAttribute> attributes;

    @OneToMany(mappedBy = "sport", cascade = CascadeType.ALL)
    private List<FacilitySport> facilitySports;
}
