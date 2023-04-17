package com.tomiscoding.billsplit.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyConversionResponse {

    private Map<String, CurrencyConversionRate> data;
}
