package com.tomiscoding.billsplit.repository;

import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<SplitGroup, Long> {

    Optional<SplitGroup> findByInviteCode(String code);

    List<SplitGroup> getByGroupMembers_User(User user);

    @EntityGraph(attributePaths = {"groupMembers"})
    List<SplitGroup> getSplitGroupWithGroupMembersByGroupMembers_User(User user);

    @EntityGraph(attributePaths = {"expenses"})
    Optional<SplitGroup> getSplitGroupWithExpensesById(Long id);

    @EntityGraph(attributePaths = {"groupMembers"})
    Optional<SplitGroup> getSplitGroupWithGroupMembersById(Long id);

    @EntityGraph(attributePaths = {"payments"})
    Optional<SplitGroup> getSplitGroupWithPaymentsById(Long id);

}
