package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.dto.UserBalance;
import com.tomiscoding.billsplit.exceptions.PaymentNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    ExpenseService expenseService;

    @Autowired
    UserService userService;

    public List<Payment> getPaymentsBySplitGroupAndUser(SplitGroup splitGroup, User user){
        return paymentRepository.getPaymentsBySplitGroupAndUser(splitGroup, user);
    }

    public Payment getPaymentById(Long id) throws PaymentNotFoundException {
        return paymentRepository.findById(id).orElseThrow(
                () -> new PaymentNotFoundException("There is no payment with id: " + id)
        );
    }

    public List<Payment> savePayments(List<Payment> payments){
        return paymentRepository.saveAll(payments);
    }

    public Payment updatePaymentStatus(Payment payment, String status) throws ValidationException {
        if (status.equals("pending")){
            return editPaymentAsPaid(payment);
        } else if (status.equals("confirmed")) {
            return editPaymentAsConfirmed(payment);
        } else {
            throw new ValidationException(status + " is not a valid status");
        }
    }

    public Payment editPaymentAsPaid(Payment payment) throws ValidationException {
        if (!payment.getPaymentStatus().equals(PaymentStatus.NOT_PAID)){
            throw new ValidationException("This payment (id = " + payment.getId() + ") must be in a status of 'Not paid' to be changed to 'Paid - Pending'");
        }
        payment.setPaymentStatus(PaymentStatus.PAID_PENDING);
        return paymentRepository.save(payment);
    }

    public Payment editPaymentAsConfirmed(Payment payment) throws ValidationException {
        if (!payment.getPaymentStatus().equals(PaymentStatus.PAID_PENDING)){
            throw new ValidationException("This payment (id = " + payment.getId() + ") must be in a status of 'Paid - Pending' to be changed to 'Paid - Confirmed'");
        }
        payment.setPaymentStatus(PaymentStatus.PAID_CONFIRMED);
        return paymentRepository.save(payment);
    }

    public List<Payment> calculateAndSavePayments(SplitGroup splitGroup){
        List<Payment> payments = calculatePayments(splitGroup);
        expenseService.setExpensesAsSplitByGroup(splitGroup);
        return savePayments(payments);
    }

    private List<UserBalance> calculateUserBalances(SplitGroup splitGroup){
        List<User> users = userService.getUsersBySplitGroup(splitGroup);

        return users.stream().map(
                        u -> UserBalance.builder()
                                .user(u)
                                .balanceOwed(splitGroup.getAmountOwedByUserId(u.getId()))
                                .build())
                .collect(Collectors.toList());
    }

    private List<Payment> calculatePayments(SplitGroup splitGroup){
        // Sort user balances list
        List<UserBalance> amountsOwed = calculateUserBalances(splitGroup).stream()
                .sorted()
                .collect(Collectors.toList());

        List<Payment> payments = new ArrayList<>();
        int i = 0;
        int j = amountsOwed.size() - 1;

        while (i < j){
            if (amountsOwed.get(i).getBalanceOwed().abs().compareTo(amountsOwed.get(j).getBalanceOwed().abs()) > 0){

                payments.add(Payment.builder()
                        .amount(amountsOwed.get(j).getBalanceOwed().abs())
                        .fromUser(amountsOwed.get(j).getUser())
                        .toUser(amountsOwed.get(i).getUser())
                        .splitGroup(splitGroup)
                        .calculatedDate(LocalDate.now())
                        .build());

                amountsOwed.get(i).setBalanceOwed(amountsOwed.get(i).getBalanceOwed().add(amountsOwed.get(j).getBalanceOwed()));
                amountsOwed.get(j).setBalanceOwed(BigDecimal.ZERO);
                j--;
            } else if (amountsOwed.get(i).getBalanceOwed().abs().compareTo(amountsOwed.get(j).getBalanceOwed().abs()) < 0) {

                payments.add(Payment.builder()
                        .amount(amountsOwed.get(i).getBalanceOwed().abs())
                        .fromUser(amountsOwed.get(j).getUser())
                        .toUser(amountsOwed.get(i).getUser())
                        .splitGroup(splitGroup)
                        .calculatedDate(LocalDate.now())
                        .build());

                amountsOwed.get(j).setBalanceOwed(amountsOwed.get(j).getBalanceOwed().add(amountsOwed.get(i).getBalanceOwed()));
                amountsOwed.get(i).setBalanceOwed(BigDecimal.ZERO);
                i++;
            } else {

                payments.add(Payment.builder()
                        .amount(amountsOwed.get(i).getBalanceOwed().abs())
                        .fromUser(amountsOwed.get(j).getUser())
                        .toUser(amountsOwed.get(i).getUser())
                        .splitGroup(splitGroup)
                        .calculatedDate(LocalDate.now())
                        .build());

                amountsOwed.get(j).setBalanceOwed(amountsOwed.get(j).getBalanceOwed().add(amountsOwed.get(i).getBalanceOwed()));
                amountsOwed.get(i).setBalanceOwed(BigDecimal.ZERO);
                i++;
                j--;
            }
        }
        return payments;
    }
}
