package com.tomiscoding.billsplit.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailVariableGroup {

    private String email;
    private List<EmailSubstitution> substitutions;

}
