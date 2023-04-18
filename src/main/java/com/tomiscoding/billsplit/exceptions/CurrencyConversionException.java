package com.tomiscoding.billsplit.exceptions;

public class CurrencyConversionException extends Exception{

    public CurrencyConversionException(String message){
        super(message);
    }

    public CurrencyConversionException(){};
}
