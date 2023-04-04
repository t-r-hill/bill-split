package com.tomiscoding.billsplit.exceptions;

public class UserValidationException extends Exception{

    public UserValidationException(String message){
        super(message);
    }
    
    public UserValidationException(){};
}
