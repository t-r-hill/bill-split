package com.tomiscoding.billsplit.dto;

import com.tomiscoding.billsplit.model.Expense;
import com.tomiscoding.billsplit.model.Payment;
import com.tomiscoding.billsplit.model.User;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupOverview {

    private Long userId;

    private BigDecimal currentGroupExpenses;
    private BigDecimal currentUserExpenses;
    private BigDecimal currentUserBalance;

    private BigDecimal totalGroupExpenses;
    private BigDecimal totalUserExpenses;
    private BigDecimal confirmedUserPayments;
    private BigDecimal notConfirmedUserPayments;

    private List<Payment> userPayments;
    private List<Expense> recentExpenses;
    private List<User> groupMemberUsers;

}
