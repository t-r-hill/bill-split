package com.tomiscoding.billsplit.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailSubstitution {

    private String var;
    private String value;

}
