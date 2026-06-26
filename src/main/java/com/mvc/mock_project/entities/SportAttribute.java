package com.mvc.mock_project.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SportAttribute")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SportAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attribute_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Column(name = "attribute_code", nullable = false, length = 50)
    private String attributeCode;

    @Column(name = "attribute_name", nullable = false)
    private String attributeName;

    @Column(name = "data_type", nullable = false, length = 20)
    private String dataType; // TEXT, NUMBER, BOOLEAN, SELECT

    @Column(name = "options_json", columnDefinition = "NVARCHAR(MAX)")
    private String optionsJson;

    @Column(name = "is_required")
    private Boolean isRequired = false;
}
