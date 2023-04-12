package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.ExpenseNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.Expense;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.repository.ExpenseRepository;
import org.aspectj.weaver.tools.MatchingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    ExpenseRepository expenseRepository;

    public Expense saveExpense(Expense expense) throws ValidationException {
        validateExpense(expense);
        expense.setAmount(expense.getAmount().setScale(2, MathContext.DECIMAL64.getRoundingMode()));
        return expenseRepository.save(expense);
    }

    public Expense getExpense(Long id) throws ExpenseNotFoundException {
        return expenseRepository.findById(id).orElseThrow(
                () -> new ExpenseNotFoundException("Could not find an expense with id: " + id)
        );
    }

    public Expense editExpense(Long id, Expense expense) throws ExpenseNotFoundException, ValidationException {
        Expense expense1 = getExpense(id);
        if (expense1.isSplit()){
            throw new ValidationException(expense1.getName().toString() + " has already been split so cannot be edited");
        }
        expense1.setExpenseDate(expense.getExpenseDate());
        expense1.setExpenseDescription(expense.getExpenseDescription());
        expense1.setAmount(expense.getAmount());
        expense1.setCurrency(expense.getCurrency());
        expense1.setName(expense.getName());
        return expenseRepository.save(expense1);
    }

    public void deleteExpense(Long id) throws ExpenseNotFoundException, ValidationException {
        Expense expense = getExpense(id);
        if (expense.isSplit()){
            throw new ValidationException(expense.getName().toString() + " has already been split so cannot be deleted");
        }
        expenseRepository.delete(expense);
    }

    public List<Expense> getExpenseByGroup(SplitGroup splitGroup){
        return expenseRepository.getExpenseBySplitGroup(splitGroup);
    }

    private void validateExpense(Expense expense) throws ValidationException {
        if (expense.getName() == null || expense.getName().isBlank()){
            throw new ValidationException("Name must not be blank");
        } else if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) == -1){
            throw new ValidationException("Amount must be a positive value");
        } else if (expense.getAmount().scale() > 2){
            throw new ValidationException("Amount must have no more than two decimal places");
        } else if (expense.getCurrency() == null) {
            throw new ValidationException("A currency must be selected");
        } else if (expense.getUser() == null) {
            throw new ValidationException("The expense must be assigned to a user");
        } else if (expense.getSplitGroup() ==  null) {
            throw new ValidationException("The expense must be assigned to a group");
        } else if (expense.getExpenseDate() == null) {
            throw new ValidationException("The expense must be have a date");
        }
    }
}
