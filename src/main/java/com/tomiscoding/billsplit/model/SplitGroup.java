package com.tomiscoding.billsplit.model;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "split_group")
public class SplitGroup {

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String groupName;

    private String groupDescription;

    @Column(nullable = false)
    private String inviteCode;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Currency baseCurrency;

    @OneToMany(mappedBy = "splitGroup", cascade = CascadeType.ALL)
    private List<GroupMember> groupMembers;

    //    @OneToMany(mappedBy = "group")
//    private List<Expense> expense = new ArrayList<>();

}
