package com.mvc.mock_project.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "Court")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "court_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_sport_id", nullable = false)
    private FacilitySport facilitySport;

    @Column(name = "court_name", nullable = false)
    private String courtName;

    @Column(length = 500)
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL)
    private List<CourtAttributeValue> attributeValues;

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL)
    private List<BookingSlot> bookingSlots;
}
