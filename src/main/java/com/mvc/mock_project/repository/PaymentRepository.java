package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Payment;
import com.mvc.mock_project.entities.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByInvoiceId(Integer invoiceId);

    boolean existsByInvoiceIdAndPaymentStatus(Integer invoiceId, PaymentStatus paymentStatus);
}
