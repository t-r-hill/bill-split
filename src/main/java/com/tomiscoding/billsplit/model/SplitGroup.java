package com.tomiscoding.billsplit.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
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
    @Builder.Default
    private List<GroupMember> groupMembers = new ArrayList<>();

    @OneToMany(mappedBy = "splitGroup")
    @Builder.Default
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "splitGroup")
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @Transient
    public BigDecimal getConfirmedPaymentsTotalForUserId(Long id){
        return payments.stream()
                .filter(p -> p.getToUser().getId() == id
                    && p.getPaymentStatus().equals(PaymentStatus.PAID_CONFIRMED))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(
                        payments.stream()
                        .filter(p -> p.getFromUser().getId() == id
                                && p.getPaymentStatus().equals(PaymentStatus.PAID_CONFIRMED))
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                );
    }

    @Transient
    public BigDecimal getNotConfirmedPaymentsTotalForUserId(Long id){
        return payments.stream()
                .filter(p -> p.getToUser().getId() == id
                        && !p.getPaymentStatus().equals(PaymentStatus.PAID_CONFIRMED))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(
                        payments.stream()
                                .filter(p -> p.getFromUser().getId() == id
                                        && !p.getPaymentStatus().equals(PaymentStatus.PAID_CONFIRMED))
                                .map(Payment::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                );
    }

    @Transient
    public boolean isUserAdmin(Long userId){
        return groupMembers.stream()
                .filter(m -> m.getUser().getId() == userId)
                .findFirst()
                .get()
                .isAdmin();
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
    public BigDecimal getOutstandingBalanceByUserId(Long userId){
        return getExpensesNotSplitTotal().divide(BigDecimal.valueOf(groupMembers.size()),2, RoundingMode.HALF_EVEN)
                .subtract(getExpensesNotSplitTotalByUserId(userId));
    }

}
