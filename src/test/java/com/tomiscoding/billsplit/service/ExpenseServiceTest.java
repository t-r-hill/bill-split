package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.CurrencyConversionException;
import com.tomiscoding.billsplit.exceptions.ExpenseNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ExpenseService.class)
class ExpenseServiceTest {

    @MockBean
    ExpenseRepository expenseRepository;

    @MockBean
    CurrencyConversionService currencyConversionService;

    @Autowired
    ExpenseService expenseService;

    private User peppaPig()
    {
        return User.builder()
                .fullName("Peppa pig")
                .username("peppa@pigs.com")
                .authorities(Collections.singletonList(new Authority(Authority.Roles.ROLE_USER)))
                .build();
    }

    private SplitGroup piggies() {
        return SplitGroup.builder()
                .groupName("Piggies")
                .groupDescription("For the piggy family")
                .baseCurrency(Currency.GBP)
                .build();
    }

    private Expense expenseGood(Currency currency, String name){
        return Expense.builder()
                .name(name)
                .expenseDescription("For jumping in muddy puddles")
                .expenseDate(LocalDate.of(2023,4,20))
                .currencyAmount(BigDecimal.valueOf(10.50))
                .currency(currency)
                .user(peppaPig())
                .splitGroup(piggies())
                .build();
    }

    private Expense expenseBad() {
        return Expense.builder()
                .name("Bad expense")
                .expenseDescription("Shouldn't be persisted")
                .expenseDate(LocalDate.of(2023,4,20))
                .currencyAmount(BigDecimal.valueOf(10.5015))
                .currency(Currency.GBP)
                .user(peppaPig())
                .splitGroup(piggies())
                .build();
    }

    private Expense expenseSplit(){
        return Expense.builder()
                .id(1L)
                .name("Split wellies")
                .expenseDescription("For jumping in muddy puddles")
                .expenseDate(LocalDate.of(2023,4,20))
                .currencyAmount(BigDecimal.valueOf(10.50))
                .currency(Currency.GBP)
                .isSplit(true)
                .user(peppaPig())
                .splitGroup(piggies())
                .build();
    }

    List<Expense> expenses;

    @Test
    void saveExpenseSuccess() throws ValidationException, CurrencyConversionException {
        Expense expenseGood = expenseGood(Currency.USD,"Red wellies");
        BigDecimal USDGBPrate = BigDecimal.valueOf(1.2);
        Expense expenseGoodConverted = expenseGood(Currency.USD, "Red wellies");
        expenseGoodConverted.setCurrencyAmount(expenseGoodConverted.getCurrencyAmount().setScale(2, RoundingMode.HALF_EVEN));
        expenseGoodConverted.setAmount(expenseGoodConverted.getCurrencyAmount().divide(USDGBPrate, 2, RoundingMode.HALF_EVEN));

        when(expenseRepository.save(ArgumentMatchers.argThat(e -> e.getAmount().equals(expenseGoodConverted.getAmount())))).thenReturn(expenseGoodConverted);
        when(currencyConversionService.getCurrencyConversion(Currency.USD, Currency.GBP)).thenReturn(BigDecimal.valueOf(1.2));

        assertThat(expenseService.saveExpense(expenseGood)).isNotNull();
    }

    @Test
    void saveExpenseFailure() throws ValidationException, CurrencyConversionException {
        Expense expenseBad = expenseBad();

        when(expenseRepository.save(any())).thenReturn(expenseBad);
        when(currencyConversionService.getCurrencyConversion(Currency.USD, Currency.GBP)).thenReturn(BigDecimal.valueOf(1.2));

        assertThrows(ValidationException.class,
                () -> expenseService.saveExpense(expenseBad));
    }

    @Test
    void editExpenseFailure() throws ValidationException, CurrencyConversionException, ExpenseNotFoundException {
        Expense expenseSplit = expenseSplit();
        Expense expenseGood = expenseGood(Currency.GBP, "Returned expense");
        ExpenseService mockExpenseService = mock(ExpenseService.class);

        when(mockExpenseService.getExpense(any())).thenReturn(expenseSplit);
        when(mockExpenseService.saveExpense(any())).thenReturn(expenseGood);

        assertThrows(ValidationException.class,
                () -> expenseService.editExpense(expenseSplit.getId(), expenseSplit));
    }

    @Test
    void deleteExpenseFailure() throws ExpenseNotFoundException {
        Expense expenseSplit = expenseSplit();

        when(expenseService.getExpense(any())).thenReturn(expenseSplit);
    }

    @Test
    void deleteExpensesListSuccess() {
        expenses = new ArrayList<>();
    }

    @Test
    void setExpensesAsSplitByGroup() {
    }
}