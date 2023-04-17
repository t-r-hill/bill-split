package com.tomiscoding.billsplit.repository;

import com.tomiscoding.billsplit.model.Expense;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> getExpenseByUser(User user);

    List<Expense> getExpenseBySplitGroup(SplitGroup splitGroup);

    List<Expense> getExpensesByUserIdAndSplitGroupId(Long userId, Long splitGroupId);

    Page<Expense> getExpensesByUserIdAndSplitGroupIdAndIsSplit(Long userId, Long splitGroupId, Boolean isSplit, Pageable pageable);

    Page<Expense> getExpensesBySplitGroupIdAndIsSplit(Long splitGroupId, Boolean isSplit, Pageable pageable);
}
