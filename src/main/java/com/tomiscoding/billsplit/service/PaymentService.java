package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    UserService userService;

    public List<Payment> savePayments(List<Payment> payments){
        return paymentRepository.saveAll(payments);
    }

    public List<Payment> calculateAndSavePayments(SplitGroup splitGroup){
        List<Payment> payments = calculatePayments(splitGroup);
        return savePayments(payments);
    }

    private List<UserBalance> calculateUserBalances(SplitGroup splitGroup){
        List<User> users = userService.getUsersBySplitGroup(splitGroup);
        int numUsers = users.size();

        return users.stream().map(
                        u -> UserBalance.builder()
                                .user(u)
                                .balanceOwed(splitGroup.getAmountOwedByUserId(u.getId(), numUsers))
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
