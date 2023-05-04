package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.DuplicateGroupMemberException;
import com.tomiscoding.billsplit.exceptions.SplitGroupNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
    void generateGroupOverview() {
    }

    @Test
    void generateAdminGroupOverview() {
    }
}