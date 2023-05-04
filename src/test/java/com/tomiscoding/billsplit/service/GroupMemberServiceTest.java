package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.DuplicateGroupMemberException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.repository.ExpenseRepository;
import com.tomiscoding.billsplit.repository.GroupMemberRepository;
import com.tomiscoding.billsplit.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {GroupMemberService.class, ExpenseService.class})
class GroupMemberServiceTest {

    @MockBean
    GroupMemberRepository groupMemberRepository;

    @MockBean
    ExpenseRepository expenseRepository;

    @MockBean
    PaymentRepository paymentRepository;

    @MockBean
    CurrencyConversionService currencyConversionService;

    @Autowired
    GroupMemberService groupMemberService;

    @SpyBean
    ExpenseService expenseService;

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

    private Expense expenseGood(String name, int num, User user, SplitGroup splitGroup){
        return Expense.builder()
                .id((long) num)
                .name(name)
                .expenseDescription("For jumping in muddy puddles")
                .expenseDate(LocalDate.of(2023,4,20))
                .currencyAmount(BigDecimal.valueOf(10.50))
                .currency(Currency.GBP)
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

    Payment newPayment(PaymentStatus paymentStatus){
        return Payment.builder()
                .paymentStatus(paymentStatus)
                .build();
    }

    @Test
    void createGroupMemberSuccess() throws ValidationException, DuplicateGroupMemberException {
        User user = newUser(1);
        SplitGroup splitGroup = newGroup(1);
        GroupMember groupMember = GroupMember.builder()
                .id(1)
                .user(user)
                .splitGroup(splitGroup)
                .build();

        when(groupMemberRepository.existsByUserAndSplitGroup(user, splitGroup)).thenReturn(false);
        when(groupMemberRepository.save(ArgumentMatchers.argThat(gm -> gm.getId() == 1L))).thenReturn(groupMember);

        assertThat(groupMemberService.createGroupMember(groupMember)).isEqualTo(groupMember);
    }

    @Test
    void createGroupMemberDuplicate() throws ValidationException, DuplicateGroupMemberException {
        User user = newUser(1);
        SplitGroup splitGroup = newGroup(1);
        GroupMember groupMember = GroupMember.builder()
                .id(1)
                .user(user)
                .splitGroup(splitGroup)
                .build();

        when(groupMemberRepository.existsByUserAndSplitGroup(user, splitGroup)).thenReturn(true);
        when(groupMemberRepository.save(ArgumentMatchers.argThat(gm -> gm.getId() == 1L))).thenReturn(groupMember);

        assertThrows(DuplicateGroupMemberException.class,
                () -> groupMemberService.createGroupMember(groupMember));
    }

    @Test
    void makeGroupMemberAdmin() throws ValidationException {
        User user = newUser(1);
        SplitGroup splitGroup = newGroup(1);
        GroupMember groupMember = GroupMember.builder()
                .id(1)
                .user(user)
                .splitGroup(splitGroup)
                .build();
        GroupMember admin = GroupMember.builder()
                .id(1)
                .user(user)
                .splitGroup(splitGroup)
                .isAdmin(true)
                .build();

        when(groupMemberRepository.save(argThat(GroupMember::isAdmin))).thenReturn(admin);

        assertThat(groupMemberService.makeGroupMemberAdmin(groupMember)).isEqualTo(admin);
    }

    @Test
    void removeGroupMemberAdminSuccess() throws ValidationException {
        User user = newUser(1);
        User user2 = newUser(2);
        SplitGroup splitGroup = newGroup(1);
        GroupMember admin = GroupMember.builder()
                .id(1)
                .user(user)
                .splitGroup(splitGroup)
                .isAdmin(true)
                .build();
        GroupMember admin2 = GroupMember.builder()
                .id(2)
                .user(user2)
                .splitGroup(splitGroup)
                .isAdmin(true)
                .build();

        when(groupMemberRepository.getGroupMembersBySplitGroupIdAndIsAdmin(ArgumentMatchers.anyLong(), eq(true)))
                .thenReturn(List.of(admin,admin2));

        groupMemberService.removeGroupMemberAdmin(admin, splitGroup.getId());

        verify(groupMemberRepository).save(argThat(gm -> !gm.isAdmin()));
    }

    @Test
    void removeGroupMemberAdminFailure() throws ValidationException {
        User user = newUser(1);
        SplitGroup splitGroup = newGroup(1);
        GroupMember admin = GroupMember.builder()
                .id(1)
                .user(user)
                .splitGroup(splitGroup)
                .isAdmin(true)
                .build();

        when(groupMemberRepository.getGroupMembersBySplitGroupIdAndIsAdmin(ArgumentMatchers.anyLong(), eq(true)))
                .thenReturn(List.of(admin));

        assertThrows(ValidationException.class,
                () -> groupMemberService.removeGroupMemberAdmin(admin, splitGroup.getId()));
    }

    @Test
    void deleteGroupMemberSuccess() throws ValidationException {
        User user = newUser(1);
        User user2 = newUser(2);
        SplitGroup splitGroup = newGroup(1);
        GroupMember admin = GroupMember.builder()
                .id(1)
                .user(user)
                .splitGroup(splitGroup)
                .isAdmin(true)
                .build();
        GroupMember admin2 = GroupMember.builder()
                .id(2)
                .user(user2)
                .splitGroup(splitGroup)
                .isAdmin(true)
                .build();

        Expense expense1 = expenseGood("expense1", 1, user, splitGroup);
        Expense expense2 = expenseGood("expense2", 2, user, splitGroup);
        Expense expenseSplit = expenseSplit(user, splitGroup);
        List<Expense> expenses = new ArrayList<>();
        expenses.add(expense1);
        expenses.add(expense2);
        expenses.add(expenseSplit);

        Payment paymentConfirmed = newPayment(PaymentStatus.PAID_CONFIRMED);

        when(expenseRepository.getExpensesByUserIdAndSplitGroupId(longThat(l -> admin.getUser().getId() == l), longThat(l -> splitGroup.getId() == l)))
                .thenReturn(expenses);
        when(paymentRepository.getPaymentsBySplitGroupAndUser(
                argThat(sg -> splitGroup.getId() == sg.getId()),
                argThat(u -> admin.getUser().getId() == u.getId())))
                .thenReturn(Collections.singletonList(paymentConfirmed));
        when(groupMemberRepository.getGroupMembersBySplitGroupIdAndIsAdmin(longThat(l -> splitGroup.getId() == l), eq(true)))
                .thenReturn(List.of(admin,admin2));

        groupMemberService.deleteGroupMember(admin, splitGroup.getId());

        verify(expenseRepository).deleteAllInBatch(argThat(it -> ((Collection<?>) it).size() == 2));
        verify(groupMemberRepository).save(argThat(gm -> !gm.isAdmin()));
        verify(groupMemberRepository).delete(argThat(gm -> gm.getId() == admin.getId()));
    }

    @Test
    void deleteGroupMemberFailure() throws ValidationException {
        User user = newUser(1);
        User user2 = newUser(2);
        SplitGroup splitGroup = newGroup(1);
        GroupMember admin = GroupMember.builder()
                .id(1)
                .user(user)
                .splitGroup(splitGroup)
                .isAdmin(true)
                .build();
        GroupMember admin2 = GroupMember.builder()
                .id(2)
                .user(user2)
                .splitGroup(splitGroup)
                .isAdmin(true)
                .build();

        Expense expense1 = expenseGood("expense1", 1, user, splitGroup);
        Expense expense2 = expenseGood("expense2", 2, user, splitGroup);
        Expense expenseSplit = expenseSplit(user, splitGroup);
        List<Expense> expenses = new ArrayList<>();
        expenses.add(expense1);
        expenses.add(expense2);
        expenses.add(expenseSplit);

        Payment paymentPending = newPayment(PaymentStatus.PAID_PENDING);

        when(expenseRepository.getExpensesByUserIdAndSplitGroupId(longThat(l -> admin.getUser().getId() == l), longThat(l -> splitGroup.getId() == l)))
                .thenReturn(expenses);
        when(paymentRepository.getPaymentsBySplitGroupAndUser(
                argThat(sg -> splitGroup.getId() == sg.getId()),
                argThat(u -> admin.getUser().getId() == u.getId())))
                .thenReturn(Collections.singletonList(paymentPending));
        when(groupMemberRepository.getGroupMembersBySplitGroupIdAndIsAdmin(longThat(l -> splitGroup.getId() == l), eq(true)))
                .thenReturn(List.of(admin,admin2));

        assertThrows(ValidationException.class,
                () -> groupMemberService.deleteGroupMember(admin, splitGroup.getId()));
    }
}