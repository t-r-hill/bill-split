package com.tomiscoding.billsplit.model;

public enum PaymentStatus {

    NOT_PAID("Not paid"),
    PAID_PENDING("Paid - pending"),
    PAID_CONFIRMED("Paid - confirmed");

    private final String fieldDescription;

    PaymentStatus(String value){
        this.fieldDescription = value;
    }

    public String getFieldDescription(){
        return fieldDescription;
    }

}
