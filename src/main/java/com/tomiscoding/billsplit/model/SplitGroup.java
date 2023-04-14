package com.tomiscoding.billsplit.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "split_group")
public class SplitGroup {

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String groupName;

    private String groupDescription;

    @Column(nullable = false)
    private String inviteCode;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Currency baseCurrency;

    @OneToMany(mappedBy = "splitGroup", cascade = CascadeType.ALL)
    private List<GroupMember> groupMembers;

    @OneToMany(mappedBy = "splitGroup")
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "splitGroup")
    private List<Payment> payments = new ArrayList<>();

    @Transient
    public BigDecimal getConfirmedPaymentsTotalByUserId(Long id){
        return payments.stream()
                .filter(p -> p.getToUser().getId() == id
                    || p.getFromUser().getId() == id)
                .filter(p -> p.getPaymentStatus().equals(PaymentStatus.PAID_CONFIRMED))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getNotConfirmedPaymentsTotalByUserId(Long id){
        return payments.stream()
                .filter(p -> p.getToUser().getId() == id
                        || p.getFromUser().getId() == id)
                .filter(p -> !p.getPaymentStatus().equals(PaymentStatus.PAID_CONFIRMED))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getExpensesTotal(){
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getExpensesNotSplitTotal(){
        return expenses.stream()
                .filter(e -> !e.isSplit())
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    @Transient
    public BigDecimal getExpensesTotalByUserId(Long userId){
        return expenses.stream()
                .filter(e -> e.getUser().getId() == userId)
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getExpensesNotSplitTotalByUserId(Long userId){
        return expenses.stream()
                .filter(e -> e.getUser().getId() == userId
                && !e.isSplit())
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getAmountOwedByUserId(Long userId){
        return getExpensesNotSplitTotal().divide(BigDecimal.valueOf(groupMembers.size()),2, RoundingMode.HALF_EVEN)
                .subtract(getExpensesNotSplitTotalByUserId(userId));
    }

}
