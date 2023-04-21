package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.CurrencyConversionException;
import com.tomiscoding.billsplit.exceptions.ExpenseNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.Currency;
import com.tomiscoding.billsplit.model.Expense;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import com.tomiscoding.billsplit.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.tools.MatchingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CurrencyConversionService currencyConversionService;

    public Expense saveExpense(Expense expense) throws ValidationException, CurrencyConversionException {
        validateExpense(expense);
        expense.setCurrencyAmount(expense.getCurrencyAmount().setScale(2, RoundingMode.HALF_EVEN));
        BigDecimal convertedAmount = convertExpenseAmount(expense);
        expense.setAmount(convertedAmount);
        return expenseRepository.save(expense);
    }

    public Expense getExpense(Long id) throws ExpenseNotFoundException {
        return expenseRepository.findById(id).orElseThrow(
                () -> new ExpenseNotFoundException("Could not find an expense with id: " + id)
        );
    }

    public Expense editExpense(Long id, Expense expense) throws ExpenseNotFoundException, ValidationException, CurrencyConversionException {
        Expense expense1 = getExpense(id);
        if (expense1.isSplit()){
            throw new ValidationException(expense1.getName().toString() + " has already been split so cannot be edited");
        }
        expense1.setExpenseDate(expense.getExpenseDate());
        expense1.setExpenseDescription(expense.getExpenseDescription());
        expense1.setCurrencyAmount(expense.getCurrencyAmount());
        expense1.setCurrency(expense.getCurrency());
        expense1.setName(expense.getName());
        return saveExpense(expense);
    }

    public void deleteExpense(Long id) throws ExpenseNotFoundException, ValidationException {
        Expense expense = getExpense(id);
        if (expense.isSplit()){
            throw new ValidationException(expense.getName().toString() + " has already been split so cannot be deleted");
        }
        expenseRepository.delete(expense);
    }

    public void deleteExpensesList(List<Expense> expenses){
        expenses = expenses.stream()
                .filter(e -> !e.isSplit())
                .collect(Collectors.toList());

        expenseRepository.deleteAllInBatch(expenses);
    }

    public List<Expense> setExpensesAsSplitByGroup(SplitGroup splitGroup){
        List<Expense> expenses = splitGroup.getExpenses();
        expenses.forEach(e -> e.setSplit(true));
        return expenseRepository.saveAll(expenses);
    }

    public List<Expense> getExpenseByUserIdAndSplitGroupId(Long userId, Long splitGroupId){
        return expenseRepository.getExpensesByUserIdAndSplitGroupId(userId, splitGroupId);
    }

    public Page<Expense> getExpenseByUserIdAndSplitGroupIdAndIsSplit(Long userId, Long splitGroupId, Boolean isSplit, Integer pageNum){
        PageRequest pageRequest = PageRequest.of(pageNum, 3).withSort(Sort.Direction.DESC,"expenseDate");
        return expenseRepository.getExpensesByUserIdAndSplitGroupIdAndIsSplit(userId, splitGroupId, isSplit, pageRequest);
    }

    public Page<Expense> getExpenseBySplitGroupIdAndIsSplit(Long splitGroupId, Boolean isSplit, Integer pageNum){
        PageRequest pageRequest = PageRequest.of(pageNum, 3).withSort(Sort.Direction.DESC,"expenseDate");
        return expenseRepository.getExpensesBySplitGroupIdAndIsSplit(splitGroupId, isSplit, pageRequest);
    }

    private BigDecimal convertExpenseAmount(Expense expense) throws CurrencyConversionException {
        Currency fromCurrency = expense.getCurrency();
        Currency toCurrency = expense.getSplitGroup().getBaseCurrency();

        BigDecimal convertedAmount;
        if (fromCurrency.equals(toCurrency)){
            convertedAmount = expense.getCurrencyAmount();
        } else {
            BigDecimal conversionRate = currencyConversionService.getCurrencyConversion(fromCurrency, toCurrency);
            convertedAmount = expense.getCurrencyAmount().divide(conversionRate,2,RoundingMode.HALF_EVEN);
        }
        return convertedAmount;
    }

    private void validateExpense(Expense expense) throws ValidationException {
        if (expense.getName() == null || expense.getName().isBlank()){
            throw new ValidationException("Name must not be blank");
        } else if (expense.getCurrencyAmount() == null || expense.getCurrencyAmount().compareTo(BigDecimal.ZERO) < 0){
            throw new ValidationException("Amount must be a positive value");
        } else if (expense.getCurrencyAmount().scale() > 2){
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
