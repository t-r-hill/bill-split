package com.tomiscoding.billsplit.repository;

import com.tomiscoding.billsplit.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
}
