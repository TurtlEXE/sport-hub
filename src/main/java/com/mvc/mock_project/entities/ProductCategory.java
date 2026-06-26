package com.mvc.mock_project.entities;

import com.mvc.mock_project.entities.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ProductCategory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer id;

    @Column(name = "category_code", unique = true, nullable = false, length = 30)
    private String categoryCode;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
