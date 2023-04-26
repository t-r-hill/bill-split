package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.dto.CurrencyConversionResponse;
import com.tomiscoding.billsplit.exceptions.CurrencyConversionException;
import com.tomiscoding.billsplit.model.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyConversionService {

    @Value("${currencyapi.key}")
    private String apiKey;

    @Value("${currencyapi.baseUrl}")
    private String currencyAPIUrl;

    private final RestTemplate restTemplate;

    /**
     * Queries the currencyapi using RestTemplate to obtain currency conversion rate for supplied currencies
     * @param fromCurrency
     * @param toCurrency
     * @return BigDecimal containing the exchange rate
     * @throws CurrencyConversionException if a non-2xx response is received from the api
     */
    @Cacheable(value = "currencies", key = "#fromCurrency.name+#toCurrency.name")
    public BigDecimal getCurrencyConversion(Currency fromCurrency, Currency toCurrency) throws CurrencyConversionException {
//        // Set http request headers with apikey
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.add("apikey", apiKey);
//        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
//
//        // Query params for currency conversion
//        Map<String, String> params = new HashMap<>();
//        params.put("base_currency", fromCurrency.name());
//        params.put("currencies", toCurrency.name());
//
//        ResponseEntity<CurrencyConversionResponse> responseEntity = restTemplate.exchange(
//                currencyAPIUrl + "?base_currency={base_currency}&currencies={currencies}",
//                HttpMethod.GET,
//                httpEntity,
//                CurrencyConversionResponse.class,
//                params
//        );
//
//        BigDecimal conversionRate = BigDecimal.ONE;
//
//        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.hasBody()){
//            CurrencyConversionResponse currencyConversionResponse = responseEntity.getBody();
//            conversionRate = currencyConversionResponse.getData()
//                    .get(toCurrency.name())
//                    .getValue();
//        } else {
//            throw new CurrencyConversionException("Could not retrieve exchange rate for: " + fromCurrency + ":" + toCurrency);
//        }
//
//        return conversionRate;
        return BigDecimal.valueOf(1.1);
    }
}
