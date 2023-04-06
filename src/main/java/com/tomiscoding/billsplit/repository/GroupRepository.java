package com.tomiscoding.billsplit.repository;

import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<SplitGroup, Long> {

    public Optional<SplitGroup> findByInviteCode(String code);

    public List<SplitGroup> getByGroupMembers_User(User user);
}
