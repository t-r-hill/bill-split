package com.tomiscoding.billsplit.exceptions;

public class DuplicateGroupMemberException extends Exception{

    public DuplicateGroupMemberException(String message){
        super(message);
    }

    public DuplicateGroupMemberException(){};
}
