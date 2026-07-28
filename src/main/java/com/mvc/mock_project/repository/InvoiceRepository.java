package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    java.util.Optional<Invoice> findByBookingId(Integer bookingId);
}
