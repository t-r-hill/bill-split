package com.tomiscoding.billsplit.controller;

import com.tomiscoding.billsplit.exceptions.PaymentNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.Payment;
import com.tomiscoding.billsplit.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // If status = PAID_PENDING only allow fromUser. If status = PAID_CONFIRMED only allow toUser
    @PreAuthorize("hasPermission(#id,'payment',#status)")
    @GetMapping("/{id}")
    public String updatePaymentStatus(@PathVariable Long id,
                                      @RequestParam String status) throws PaymentNotFoundException, ValidationException {
        Payment payment = paymentService.getPaymentById(id);
        paymentService.updatePaymentStatus(payment, status);
        return "redirect:/splitGroup/" + payment.getSplitGroup().getId();
    }
}
