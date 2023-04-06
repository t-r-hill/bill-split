package com.tomiscoding.billsplit.exceptions;

public class ValidationException extends Exception{

    public ValidationException(String message){
        super(message);
    }
    
    public ValidationException(){};
}
