package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.DuplicateGroupMemberException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.repository.GroupMemberRepository;
import com.tomiscoding.billsplit.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {GroupMemberService.class})
class GroupMemberServiceTest {

    @MockBean
    GroupMemberRepository groupMemberRepository;

    @MockBean
    ExpenseService expenseService;

    @MockBean
    PaymentRepository paymentRepository;

    @Autowired
    GroupMemberService groupMemberService;

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
        GroupMember admin2 = GroupMember.builder()
                .id(2)
                .user(user2)
                .splitGroup(splitGroup)
                .build();

        when(groupMemberRepository.getGroupMembersBySplitGroupIdAndIsAdmin(ArgumentMatchers.anyLong(), true))
                .thenReturn(List.of(admin,admin2));
        when(groupMemberRepository.save(argThat(gm -> !gm.isAdmin()))).thenReturn(groupMember);

        groupMemberService.removeGroupMemberAdmin(admin, splitGroup.getId());

        // What do I now assert?
    }

    @Test
    void removeGroupMemberAdminFailure() throws ValidationException {
        User user = newUser(1);
        User user2 = newUser(2);
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
        GroupMember admin2 = GroupMember.builder()
                .id(2)
                .user(user2)
                .splitGroup(splitGroup)
                .build();

        when(groupMemberRepository.getGroupMembersBySplitGroupIdAndIsAdmin(ArgumentMatchers.anyLong(), true))
                .thenReturn(List.of(admin));
        when(groupMemberRepository.save(argThat(gm -> !gm.isAdmin()))).thenReturn(groupMember);

        assertThrows(ValidationException.class,
                () -> groupMemberService.removeGroupMemberAdmin(admin, splitGroup.getId()));

        // What do I now assert?
    }

    @Test
    void deleteGroupMember() {
    }
}