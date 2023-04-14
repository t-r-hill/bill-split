package com.tomiscoding.billsplit.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "split_group_id",
    referencedColumnName = "id")
    private SplitGroup splitGroup;

    @Column(nullable = false)
    private boolean isAdmin;
}
