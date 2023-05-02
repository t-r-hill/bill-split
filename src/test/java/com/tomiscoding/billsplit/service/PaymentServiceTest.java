package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.Payment;
import com.tomiscoding.billsplit.model.PaymentStatus;
import com.tomiscoding.billsplit.repository.PaymentRepository;
import com.tomiscoding.billsplit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {PaymentService.class})
class PaymentServiceTest {

    @MockBean
    PaymentRepository paymentRepository;

    @MockBean
    ExpenseService expenseService;

    @MockBean
    UserRepository userRepository;

    @Autowired
    PaymentService paymentService;

    Payment newPayment(PaymentStatus paymentStatus){
        return Payment.builder()
                .paymentStatus(paymentStatus)
                .build();
    }

    @Test
    void updatePaymentStatusToPendingSuccess() throws ValidationException {
        Payment paymentNotPaid = newPayment(PaymentStatus.NOT_PAID);
        Payment paymentPending = newPayment(PaymentStatus.PAID_PENDING);

        when(paymentRepository.save(ArgumentMatchers.argThat(
                p -> p.getPaymentStatus().equals(PaymentStatus.PAID_PENDING))))
                .thenReturn(paymentPending);

        assertThat(paymentService.updatePaymentStatus(paymentNotPaid, "PAID_PENDING").getPaymentStatus()).isEqualTo(paymentPending.getPaymentStatus());
    }

    @Test
    void updatePaymentStatusToConfirmedSuccess() throws ValidationException {
        Payment paymentPending = newPayment(PaymentStatus.PAID_PENDING);
        Payment paymentConfirmed = newPayment(PaymentStatus.PAID_CONFIRMED);

        when(paymentRepository.save(ArgumentMatchers.argThat(
                p -> p.getPaymentStatus().equals(PaymentStatus.PAID_CONFIRMED))))
                .thenReturn(paymentConfirmed);

        assertThat(paymentService.updatePaymentStatus(paymentPending, "PAID_CONFIRMED").getPaymentStatus()).isEqualTo(paymentConfirmed.getPaymentStatus());
    }

    @Test
    void updatePaymentStatusToPendingFailure() throws ValidationException {
        Payment paymentConfirmed = newPayment(PaymentStatus.PAID_CONFIRMED);
        Payment paymentPending = newPayment(PaymentStatus.PAID_PENDING);

        when(paymentRepository.save(ArgumentMatchers.argThat(
                p -> p.getPaymentStatus().equals(PaymentStatus.PAID_PENDING))))
                .thenReturn(paymentPending);

        assertThrows(ValidationException.class,
                () -> paymentService.updatePaymentStatus(paymentConfirmed, "PAID_PENDING"));
    }

    @Test
    void updatePaymentStatusToConfirmedFailure() throws ValidationException {
        Payment paymentNotPaid = newPayment(PaymentStatus.NOT_PAID);
        Payment paymentConfirmed = newPayment(PaymentStatus.PAID_CONFIRMED);

        when(paymentRepository.save(ArgumentMatchers.argThat(
                p -> p.getPaymentStatus().equals(PaymentStatus.PAID_CONFIRMED))))
                .thenReturn(paymentConfirmed);

        assertThrows(ValidationException.class,
                () -> paymentService.updatePaymentStatus(paymentNotPaid, "PAID_CONFIRMED"));
    }

    @Test
    void updatePaymentStatusFailure() throws ValidationException {
        Payment paymentNotPaid = newPayment(PaymentStatus.NOT_PAID);
        Payment paymentPending = newPayment(PaymentStatus.PAID_PENDING);

        when(paymentRepository.save(ArgumentMatchers.argThat(
                p -> p.getPaymentStatus().equals(PaymentStatus.PAID_PENDING))))
                .thenReturn(paymentPending);

        assertThrows(ValidationException.class,
                () -> paymentService.updatePaymentStatus(paymentNotPaid, "PAID"));
    }


    @Test
    void calculateAndSavePayments() {
    }
}