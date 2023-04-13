package com.tomiscoding.billsplit.repository;

import com.tomiscoding.billsplit.model.Payment;
import com.tomiscoding.billsplit.model.SplitGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> getPaymentsBySplitGroup(SplitGroup splitGroup);
}
