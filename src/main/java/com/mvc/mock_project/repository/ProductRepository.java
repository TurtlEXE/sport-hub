package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mvc.mock_project.entities.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByFacility_IdAndIsActiveTrue(Integer facilityId);
    List<Product> findByFacility_Id(Integer facilityId);

    @Query("SELECT p FROM Product p WHERE p.facility.id = :facilityId " +
           "AND (:search IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:type IS NULL OR p.productType = :type) " +
           "AND (:status IS NULL OR p.isActive = :status)")
    Page<Product> findFilteredProducts(@Param("facilityId") Integer facilityId,
                                       @Param("search") String search,
                                       @Param("categoryId") Integer categoryId,
                                       @Param("type") ProductType type,
                                       @Param("status") Boolean status,
                                       Pageable pageable);
}
