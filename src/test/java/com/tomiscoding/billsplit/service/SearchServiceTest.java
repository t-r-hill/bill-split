package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.dto.ExpenseSearchFilter;
import com.tomiscoding.billsplit.exceptions.SplitGroupListNotFoundException;
import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = SearchService.class)
class SearchServiceTest {

    @MockBean
    GroupRepository groupRepository;

    @Autowired
    SearchService searchService;

    @MockBean
    GroupService groupService;

    private User createUser(String name, String username, Long id)
    {
        return User.builder()
                .id(id)
                .fullName(name)
                .username(username)
                .authorities(Collections.singletonList(new Authority(Authority.Roles.ROLE_USER)))
                .build();
    }

    private SplitGroup createGroup(int num) {
        return SplitGroup.builder()
                .groupName("Group" + num)
                .groupDescription("Description")
                .baseCurrency(Currency.GBP)
                .build();
    }

    @Test
    void populateExpenseSearchOptions() throws SplitGroupListNotFoundException {
        User activeUser = createUser("Active user", "activeUser", 10L);

        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 5; i++){
            User user = createUser("user" + i, "user" + i, (long) i);
            users.add(user);
        }

        SplitGroup group1 = createGroup(1);
        SplitGroup group2 = createGroup(2);

        List<GroupMember> group1Members = new ArrayList<>();
        group1Members.add(GroupMember.builder()
                .splitGroup(group1)
                .user(users.get(0))
                .build());
        group1Members.add(GroupMember.builder()
                .splitGroup(group1)
                .user(users.get(1))
                .build());
        group1Members.add(GroupMember.builder()
                .splitGroup(group1)
                .user(users.get(2))
                .build());
        group1Members.add(GroupMember.builder()
                .splitGroup(group1)
                .user(activeUser)
                .build());
        group1.setGroupMembers(group1Members);

        List<GroupMember> group2Members = new ArrayList<>();
        group1Members.add(GroupMember.builder()
                .splitGroup(group1)
                .user(users.get(2))
                .build());
        group1Members.add(GroupMember.builder()
                .splitGroup(group1)
                .user(users.get(3))
                .build());
        group1Members.add(GroupMember.builder()
                .splitGroup(group1)
                .user(users.get(4))
                .build());
        group1Members.add(GroupMember.builder()
                .splitGroup(group1)
                .user(activeUser)
                .build());
        group2.setGroupMembers(group2Members);

        when(groupService.getGroupsByUser(any())).thenReturn(List.of(group1, group2));

        ExpenseSearchFilter expenseSearchFilter = searchService.populateExpenseSearchOptions(activeUser);

        assertThat(expenseSearchFilter.getUsers().size()).isEqualTo(6);
        assertThat(expenseSearchFilter.getSplitGroups().size()).isEqualTo(2);
    }

}