package com.mvc.mock_project.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CourtAttributeValue",
       uniqueConstraints = @UniqueConstraint(columnNames = {"court_id", "attribute_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourtAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private SportAttribute attribute;

    @Column(nullable = false)
    private String value;
}
