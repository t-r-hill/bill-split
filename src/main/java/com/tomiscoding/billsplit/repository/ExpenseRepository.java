package com.tomiscoding.billsplit.repository;

import com.tomiscoding.billsplit.model.Expense;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    public List<Expense> getExpenseByUser(User user);

    public List<Expense> getExpenseBySplitGroup(SplitGroup splitGroup);
}
