package com.tomiscoding.billsplit.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyConversionRate {

    private String code;
    private BigDecimal value;

}
