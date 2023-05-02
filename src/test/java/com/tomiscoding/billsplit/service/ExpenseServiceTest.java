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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

        when(expenseRepository.findById(ArgumentMatchers.eq(1L))).thenReturn(Optional.ofNullable(expenseSplit));
        when(expenseRepository.save(ArgumentMatchers.argThat(e -> e.getAmount().equals(expenseSplit.getAmount())))).thenReturn(expenseSplit);
        when(currencyConversionService.getCurrencyConversion(Currency.USD, Currency.GBP)).thenReturn(BigDecimal.valueOf(1.2));

        assertThrows(ValidationException.class,
                () -> expenseService.editExpense(expenseSplit.getId(), expenseSplit));
    }

    @Test
    void deleteExpenseFailure() throws ExpenseNotFoundException {
        Expense expenseSplit = expenseSplit();

        when(expenseRepository.findById(ArgumentMatchers.eq(1L))).thenReturn(Optional.ofNullable(expenseSplit));

        assertThrows(ValidationException.class,
                () -> expenseService.editExpense(expenseSplit.getId(), expenseSplit));
    }

    @Test
    void deleteExpensesListSuccess() {
//        List<Expense> expenses = new ArrayList<>();
//        expenses.add(expenseGood(Currency.GBP, "Red wellies"));
//        expenses.add(expenseGood(Currency.GBP, "Blue wellies"));
//        expenses.add(expenseGood(Currency.GBP, "Green wellies"));
//        expenses.add(expenseSplit());
//
////        when(expenseRepository.deleteAllInBatch(any(ArrayList.class))).then(doNothing());
//        //This works without mocking method but I can't test that expenses.size() == 3
//        expenseService.deleteExpensesList(expenses);
//        assertThat(expenses.size() == 3).isTrue();

    }

    @Test
    void setExpensesAsSplitByGroup() {
        SplitGroup piggies = piggies();
        List<Expense> piggyExpenses = new ArrayList<>();
        piggyExpenses.add(expenseGood(Currency.GBP, "Red wellies"));
        piggyExpenses.add(expenseGood(Currency.GBP, "Blue wellies"));
        piggyExpenses.add(expenseGood(Currency.GBP, "Green wellies"));
        piggyExpenses.add(expenseSplit());

        List<Expense> unSplitExpenses = piggyExpenses.subList(0,3);
        unSplitExpenses.forEach(e -> e.setSplit(true));

//        when(expenseRepository.saveAll(ArgumentMatchers.argThat(l ->
//                int ))).thenReturn(unSplitExpenses);
//
//        assertThat(expenseService.setExpensesAsSplitByGroup(piggies)).hasSize(3);

    }
}