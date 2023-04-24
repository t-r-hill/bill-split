package com.tomiscoding.billsplit.dto;

import com.tomiscoding.billsplit.model.PaymentStatus;
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
public class PaymentSearchFilter {

    private List<SplitGroup> splitGroups;
    private Set<User> users;

    private SplitGroup splitGroup;
    private User fromUser;
    private User toUser;
    private PaymentStatus paymentStatus;

    private Integer selectedPageNum;
    private Integer currentPageNum;
    private Integer numPages;
}
