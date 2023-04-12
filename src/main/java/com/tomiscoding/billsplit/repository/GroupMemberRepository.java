package com.tomiscoding.billsplit.repository;

import com.tomiscoding.billsplit.model.GroupMember;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    public boolean existsByUserAndSplitGroup(User user, SplitGroup splitGroup);
}
