package com.tomiscoding.billsplit.repository;

import com.tomiscoding.billsplit.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
