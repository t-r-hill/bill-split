package com.tomiscoding.billsplit.repository;

import com.tomiscoding.billsplit.model.Payment;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query(value = "SELECT p FROM Payment p WHERE p.splitGroup = :splitGroup " +
            "AND (p.fromUser = :user OR p.toUser = :user)")
    List<Payment> getPaymentsBySplitGroupAndUser(@Param("splitGroup") SplitGroup splitGroup,
                                                 @Param("user") User user);
}
