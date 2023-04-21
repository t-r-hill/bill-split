package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.dto.ExpenseSearchFilter;
import com.tomiscoding.billsplit.model.GroupMember;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a service class to assist with search functionality in the app
 * which creates _SearchFilter objects utilised to provide and retrieve search terms
 * to the user
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final GroupService groupService;

    /**
     * @param user a User object
     * @return ExpenseSearchFilter containing the splitGroups which a user belongs to along with the users which
     * are members of those groups
     */
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
