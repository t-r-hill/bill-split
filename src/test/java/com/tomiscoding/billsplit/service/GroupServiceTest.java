package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.dto.GroupOverview;
import com.tomiscoding.billsplit.exceptions.DuplicateGroupMemberException;
import com.tomiscoding.billsplit.exceptions.SplitGroupNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {GroupService.class})
class GroupServiceTest {

    @Autowired
    GroupService groupService;

    @MockBean
    GroupRepository groupRepository;

    @MockBean
    GroupMemberService groupMemberService;

    SplitGroup newGroup(int num){
        return SplitGroup.builder()
                .id((long) num)
                .groupName("group" + num)
                .groupDescription("group" + num)
                .baseCurrency(Currency.GBP)
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

    GroupMember newGroupMember(User user, SplitGroup splitGroup){
        return GroupMember.builder()
                .id(user.getId())
                .splitGroup(splitGroup)
                .user(user)
                .build();
    }

    private Expense expenseGood(String name, int num, User user, SplitGroup splitGroup, BigDecimal amount, int day){
        return Expense.builder()
                .id((long) num)
                .name(name)
                .expenseDescription("For jumping in muddy puddles")
                .expenseDate(LocalDate.of(2023,4,day))
                .amount(amount)
                .user(user)
                .splitGroup(splitGroup)
                .build();
    }

    @Test
    void createGroupSuccess() throws ValidationException {
        SplitGroup splitGroup = newGroup(1);
        User user = newUser(1);

        when(groupRepository.findByInviteCode(any())).thenReturn(Optional.empty());
        groupService.createGroup(splitGroup, user);
        verify(groupRepository).save(argThat(sg ->
                sg.getGroupMembers().get(0).isAdmin() &&
                sg.getInviteCode().length() == 8));
    }

    @Test
    void addUserToGroupByInviteCodeSuccess() throws ValidationException, DuplicateGroupMemberException, SplitGroupNotFoundException {
        SplitGroup splitGroup = newGroup(1);
        User user = newUser(1);

        when(groupRepository.findByInviteCode(any())).thenReturn(Optional.ofNullable(splitGroup));
        groupService.addUserToGroupByInviteCode(user, "inviteCode");
        verify(groupMemberService).createGroupMember(argThat(gm ->
                gm.getSplitGroup().getId() == 1L &&
                gm.getUser().getId() == 1L));
    }

    @Test
    void addUserToGroupByInviteCodeNotFound() throws ValidationException, DuplicateGroupMemberException, SplitGroupNotFoundException {
        SplitGroup splitGroup = newGroup(1);
        User user = newUser(1);

        when(groupRepository.findByInviteCode(any())).thenReturn(Optional.empty());
        assertThrows(SplitGroupNotFoundException.class,
                () -> groupService.addUserToGroupByInviteCode(user, "inviteCode"));
    }

    @Test
    void generateGroupOverviewSuccess() throws SplitGroupNotFoundException {
        //Creating users, group, group members
        User user1 = newUser(1);
        User user2 = newUser(2);
        User user3 = newUser(3);
        User user4 = newUser(4);
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
                        BigDecimal.valueOf(id * 10L + i),
                        id + i);
                expenses.add(expense);
            }
        }
        splitGroup.setExpenses(expenses);

        List<Payment> payments = new ArrayList<>();
        for (PaymentStatus paymentStatus : PaymentStatus.values()){
            Payment paymentTo = Payment.builder()
                    .splitGroup(splitGroup)
                    .amount(BigDecimal.valueOf(10L))
                    .toUser(user1)
                    .fromUser(user2)
                    .paymentStatus(paymentStatus)
                    .build();
            Payment paymentFrom = Payment.builder()
                    .splitGroup(splitGroup)
                    .amount(BigDecimal.valueOf(5L))
                    .toUser(user3)
                    .fromUser(user1)
                    .paymentStatus(paymentStatus)
                    .build();
            payments.add(paymentTo);
            payments.add(paymentFrom);
        }
        splitGroup.setPayments(payments);

        when(groupRepository.getSplitGroupWithExpensesById(eq(splitGroup.getId()))).thenReturn(Optional.of(splitGroup));
        when(groupRepository.getSplitGroupWithGroupMembersById(eq(splitGroup.getId()))).thenReturn(Optional.of(splitGroup));
        when(groupRepository.getSplitGroupWithPaymentsById(eq(splitGroup.getId()))).thenReturn(Optional.of(splitGroup));

        GroupOverview groupOverview = groupService.generateGroupOverview(splitGroup.getId(), user1.getId());

        assert(groupOverview.getUserId() == user1.getId());
        assert(groupOverview.getGroupMemberUsers().size() == 4);
        assert(groupOverview.getRecentExpenses().size() == 10);
        assert(groupOverview.getRecentExpenses().get(0).getId() == 44L);
        assert(groupOverview.getUserPayments().size() == 4);
        assert(groupOverview.getCurrentGroupExpenses().compareTo(BigDecimal.valueOf(320L)) == 0);
        assert(groupOverview.getConfirmedUserPayments().compareTo(BigDecimal.valueOf(5L)) == 0);
        assert(groupOverview.getCurrentUserExpenses().compareTo(BigDecimal.valueOf(11L)) == 0);
        assert(groupOverview.getNotConfirmedUserPayments().compareTo(BigDecimal.valueOf(10L)) == 0);
        assert(groupOverview.getCurrentUserBalance().compareTo(BigDecimal.valueOf(69L)) == 0);
    }

    @Test
    void generateAdminGroupOverviewSuccess() {
    }
}