package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByFacility_IdAndIsActiveTrue(Integer facilityId);
}
