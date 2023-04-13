package com.tomiscoding.billsplit.exceptions;

public class PaymentNotFoundException extends Exception{

    public PaymentNotFoundException(String message){
        super(message);
    }

    public PaymentNotFoundException(){};
}
