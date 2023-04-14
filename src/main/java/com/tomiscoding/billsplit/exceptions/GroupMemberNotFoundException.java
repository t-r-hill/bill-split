package com.tomiscoding.billsplit.exceptions;

public class GroupMemberNotFoundException extends Exception{

    public GroupMemberNotFoundException(String message){
        super(message);
    }

    public GroupMemberNotFoundException(){};
}
