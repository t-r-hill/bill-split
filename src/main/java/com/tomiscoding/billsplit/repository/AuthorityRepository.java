package com.tomiscoding.billsplit.repository;

import com.tomiscoding.billsplit.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    public Authority findByRole(Authority.Roles role);
}
