package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    boolean existsByProductId(Integer productId);
}
