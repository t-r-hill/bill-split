package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.repository.PaymentRepository;
import com.tomiscoding.billsplit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

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

    final ArgumentCaptor<List<Payment>> paymentsCaptor
            = ArgumentCaptor.forClass((Class) List.class);

    Payment newPayment(PaymentStatus paymentStatus){
        return Payment.builder()
                .paymentStatus(paymentStatus)
                .build();
    }

    User newUser(int num){
        return User.builder()
                .id((long) num)
                .fullName("user" + num)
                .username("user" + num)
                .authorities(Collections.singletonList(new Authority(Authority.Roles.ROLE_USER)))
                .build();
    }

    SplitGroup newGroup(int num){
        return SplitGroup.builder()
                .id((long) num)
                .groupName("group" + num)
                .groupDescription("group" + num)
                .baseCurrency(Currency.GBP)
                .build();
    }

    GroupMember newGroupMember(User user, SplitGroup splitGroup){
        return GroupMember.builder()
                .id(user.getId())
                .splitGroup(splitGroup)
                .user(user)
                .build();
    }

    private Expense expenseGood(String name, int num, User user, SplitGroup splitGroup, BigDecimal amount){
        return Expense.builder()
                .id((long) num)
                .name(name)
                .expenseDescription("For jumping in muddy puddles")
                .expenseDate(LocalDate.of(2023,4,20))
                .amount(amount)
                .user(user)
                .splitGroup(splitGroup)
                .build();
    }

    private Expense expenseSplit(User user, SplitGroup splitGroup){
        return Expense.builder()
                .id(10L)
                .name("Split wellies")
                .expenseDescription("For jumping in muddy puddles")
                .expenseDate(LocalDate.of(2023,4,20))
                .currencyAmount(BigDecimal.valueOf(10.50))
                .currency(Currency.GBP)
                .isSplit(true)
                .user(user)
                .splitGroup(splitGroup)
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
        //Creating users, group, group members and expenses
        User user1 = newUser(1);
        User user2 = newUser(2);
        User user3 = newUser(3);
        User user4 = newUser(4);
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        users.add(user3);
        users.add(user4);
        SplitGroup splitGroup = newGroup(1);
        GroupMember member1 = newGroupMember(user1, splitGroup);
        GroupMember member2 = newGroupMember(user2, splitGroup);
        GroupMember member3 = newGroupMember(user3, splitGroup);
        GroupMember member4 = newGroupMember(user4, splitGroup);
        List<GroupMember> groupMembers = new ArrayList<>();
        groupMembers.add(member1);
        groupMembers.add(member2);
        groupMembers.add(member3);
        groupMembers.add(member4);
        splitGroup.setGroupMembers(groupMembers);
        List<Expense> expenses = new ArrayList<>();
        for (GroupMember groupMember : groupMembers){
            int id = (int) groupMember.getUser().getId();
            for (int i = 1; i <= id; i++){
                Expense expense = expenseGood("expense" + id + i,
                        id * 10 + i,
                        groupMember.getUser(),
                        splitGroup,
                        BigDecimal.valueOf(id * 10L + i));
                expenses.add(expense);
            }
        }
        splitGroup.setExpenses(expenses);

        when(userRepository.getByGroupMembers_SplitGroupId(splitGroup.getId())).thenReturn(users);
        paymentService.calculateAndSavePayments(splitGroup);

        verify(expenseService).setExpensesAsSplitByGroup(argThat(sg -> sg.getId() == splitGroup.getId()));
        verify(paymentRepository).saveAll(paymentsCaptor.capture());
        List<Payment> payments = paymentsCaptor.getValue();

        List<BigDecimal> balances = new ArrayList<>();
        for(User eachUser : users){
            BigDecimal totalExpenses = expenses.stream()
                    .filter(e -> e.getUser().getId() == eachUser.getId())
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO,BigDecimal::add);
            BigDecimal paymentsFrom = payments.stream()
                    .filter(p -> p.getFromUser().getId() == eachUser.getId())
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal paymentsTo = payments.stream()
                    .filter(p -> p.getToUser().getId() == eachUser.getId())
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            balances.add(totalExpenses.subtract(paymentsTo).add(paymentsFrom));
        }

        for (int i = 0; i < users.size() - 1; i++){
            assert(balances.get(i).compareTo(balances.get(i+1)) == 0);
        }

    }
}