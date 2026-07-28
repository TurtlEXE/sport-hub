package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {
    List<ProductCategory> findByIsActiveTrue();
    boolean existsByCategoryCode(String categoryCode);
}
