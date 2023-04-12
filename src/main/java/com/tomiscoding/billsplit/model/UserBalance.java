package com.tomiscoding.billsplit.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBalance implements Comparable<UserBalance>{

    private User user;
    private BigDecimal balanceOwed;

    @Override
    public int compareTo(UserBalance o){
        return balanceOwed.compareTo(o.getBalanceOwed());
    }
}
