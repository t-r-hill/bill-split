package com.tomiscoding.billsplit.exceptions;

public class EmailSendException extends Exception{

    public EmailSendException(String message){
        super(message);
    }

    public EmailSendException(){};
}
