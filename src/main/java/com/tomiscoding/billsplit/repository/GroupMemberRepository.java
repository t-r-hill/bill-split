package com.tomiscoding.billsplit.repository;

import com.tomiscoding.billsplit.model.GroupMember;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByUserAndSplitGroup(User user, SplitGroup splitGroup);

    Optional<GroupMember> getByUserIdAndSplitGroupId(Long userId, Long splitGroupId);

    List<GroupMember> getGroupMembersBySplitGroupIdAndIsAdmin(Long splitGroupId, Boolean isAdmin);
}
