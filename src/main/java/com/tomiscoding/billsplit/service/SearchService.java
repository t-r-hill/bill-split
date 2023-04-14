package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.dto.ExpenseSearchFilter;
import com.tomiscoding.billsplit.model.GroupMember;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    GroupService groupService;

    public ExpenseSearchFilter populateExpenseSearchOptions(User user){
        List<SplitGroup> splitGroups = groupService.getGroupsWithGroupMembersByUser(user);
        List<User> users = splitGroups.stream()
                .flatMap(sg -> sg.getGroupMembers().stream()
                        .map(GroupMember::getUser))
                .collect(Collectors.toList());
        return ExpenseSearchFilter.builder()
                .splitGroups(splitGroups)
                .users(users)
                .build();
    }
}
