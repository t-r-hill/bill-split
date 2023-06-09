package com.tomiscoding.billsplit.dto;

import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSearchFilter {

    private List<SplitGroup> splitGroups;
    private Set<User> users;

    private SplitGroup splitGroup;
    private User user;
    private Boolean isSplit;

    private Integer selectedPageNum;
    private Integer currentPageNum;
    private Integer numPages;
}
