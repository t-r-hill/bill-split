package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.dto.UserBalance;
import com.tomiscoding.billsplit.exceptions.PaymentNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.repository.PaymentRepository;
import com.tomiscoding.billsplit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This service class provides methods to create, retrieve, update and delete payment objects
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ExpenseService expenseService;
    private final UserRepository userRepository;

    public Payment getPaymentById(Long id) throws PaymentNotFoundException {
        return paymentRepository.findById(id).orElseThrow(
                () -> new PaymentNotFoundException("There is no payment with id: " + id)
        );
    }

    public List<Payment> savePayments(List<Payment> payments){
        return paymentRepository.saveAll(payments);
    }

    public Payment updatePaymentStatus(Payment payment, String status) throws ValidationException {
        if (status.equals("PAID_PENDING")){
            return editPaymentAsPaid(payment);
        } else if (status.equals("PAID_CONFIRMED")) {
            return editPaymentAsConfirmed(payment);
        } else {
            throw new ValidationException(status + " is not a valid status");
        }
    }

    // Helper method to update the status of a payment to 'PAID_PENDING'
    // Validation to ensure only a payment in status 'NOT_PAID' can be edited
    public Payment editPaymentAsPaid(Payment payment) throws ValidationException {
        if (!payment.getPaymentStatus().equals(PaymentStatus.NOT_PAID)){
            throw new ValidationException("This payment (id = " + payment.getId() + ") must be in a status of 'Not paid' to be changed to 'Paid - Pending'");
        }
        payment.setPaymentStatus(PaymentStatus.PAID_PENDING);
        return paymentRepository.save(payment);
    }

    // Helper method to update the status of a payment to 'PAID_CONFIRMED'
    // Validation to ensure only a payment in status 'PAID_PENDING' can be edited
    public Payment editPaymentAsConfirmed(Payment payment) throws ValidationException {
        if (!payment.getPaymentStatus().equals(PaymentStatus.PAID_PENDING)){
            throw new ValidationException("This payment (id = " + payment.getId() + ") must be in a status of 'Paid - Pending' to be changed to 'Paid - Confirmed'");
        }
        payment.setPaymentStatus(PaymentStatus.PAID_CONFIRMED);
        return paymentRepository.save(payment);
    }

    /**
     * Will calculate payments for the group from 'un-split' expenses using calculatePayments() and then set the
     * expenses to a 'split' status
     * @param splitGroup the group for calculating payments - must have expenses and group members loaded
     * @return a list of the newly created payments in the default 'NOT_PAID' status
     */
    public List<Payment> calculateAndSavePayments(SplitGroup splitGroup){
        List<Payment> payments = calculatePayments(splitGroup);
        expenseService.setExpensesAsSplitByGroup(splitGroup);
        return savePayments(payments);
    }

    /**
     * Calculates the payments from/to each user based on the value of expenses a user has entered that has not
     * yet been split. Uses groupService.calculateUserBalances() to retrieve the sum of 'un-split' expenses for each user
     * and then sorts the list from 'most owed' (-ve balance) to 'owes most' (+ve balance). Then iterates over user balances
     * to allocate payments from +ve balances to -ve balances so that all balances equal 0 and creates the corresponding payment objects.
     * @param splitGroup the group for calculating payments - must have expenses and group members loaded
     * @return a list of the payments for the group
     */
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

    /**
     * Helper method to populate UserBalances list for calculating payments. Sums all 'un-split' expenses for each user
     * within a group
     * @param splitGroup for which the user balances should be calculated
     * @return A list of UserBalances objects containing the amount spent by each user based on their outstanding expenses
     */
    private List<UserBalance> calculateUserBalances(SplitGroup splitGroup){
        List<User> users = userRepository.getByGroupMembers_SplitGroupId(splitGroup.getId());

        return users.stream().map(
                        u -> UserBalance.builder()
                                .user(u)
                                .balanceOwed(splitGroup.getOutstandingBalanceByUserId(u.getId()))
                                .build())
                .collect(Collectors.toList());
    }
}
