package com.tomiscoding.billsplit.exceptions;

public class ExpenseNotFoundException extends Exception{

    public ExpenseNotFoundException(String message){
        super(message);
    }

    public ExpenseNotFoundException(){};
}
