package com.mvc.mock_project.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "FacilitySport",
       uniqueConstraints = @UniqueConstraint(columnNames = {"facility_id", "sport_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FacilitySport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facility_sport_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Column(name = "min_duration_minutes", nullable = false)
    private Integer minDurationMinutes;

    @Column(name = "slot_step_minutes", nullable = false)
    private Integer slotStepMinutes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "facilitySport", cascade = CascadeType.ALL)
    private List<Court> courts;

    @OneToMany(mappedBy = "facilitySport", cascade = CascadeType.ALL)
    private List<FacilityPriceRule> priceRules;
}
