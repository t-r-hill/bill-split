package com.tomiscoding.billsplit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.stream.Stream;


public enum Currency {

    GBP,
    USD,
    EUR;

}
