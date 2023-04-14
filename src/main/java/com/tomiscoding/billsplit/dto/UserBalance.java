package com.tomiscoding.billsplit.dto;

import com.tomiscoding.billsplit.model.User;
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
