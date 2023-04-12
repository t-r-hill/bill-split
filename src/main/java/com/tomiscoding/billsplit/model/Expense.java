package com.tomiscoding.billsplit.model;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String name;

    private String expenseDescription;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Currency currency;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate expenseDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean isSplit = false;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "split_group_id")
    private SplitGroup splitGroup;
}
