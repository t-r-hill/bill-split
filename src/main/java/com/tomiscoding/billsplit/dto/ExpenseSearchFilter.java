package com.tomiscoding.billsplit.dto;

import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSearchFilter {

    private List<SplitGroup> splitGroups;
    private List<User> users;

    private SplitGroup splitGroup;
    private User user;
    private Boolean isSplit;

    private Integer selectedPageNum;
    private Integer currentPageNum;
    private Integer numPages;
}
