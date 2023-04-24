package com.tomiscoding.billsplit.controller;

import com.tomiscoding.billsplit.dto.ExpenseSearchFilter;
import com.tomiscoding.billsplit.exceptions.*;
import com.tomiscoding.billsplit.model.Currency;
import com.tomiscoding.billsplit.model.Expense;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import com.tomiscoding.billsplit.service.ExpenseService;
import com.tomiscoding.billsplit.service.GroupService;
import com.tomiscoding.billsplit.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/expense")
public class ExpenseController {


    private final ExpenseService expenseService;
    private final GroupService groupService;
    private final SearchService searchService;

    // Only accessed by expense user or group admin
    @PreAuthorize("hasPermission(#id, 'expense','all')")
    @GetMapping("/{id}/edit")
    public String showEditExpense(@PathVariable Long id, Model model) throws SplitGroupNotFoundException, ExpenseNotFoundException {
        Expense expense = expenseService.getExpense(id);
        List<Currency> currencies = List.of(Currency.values());
        model.addAttribute("currencies", currencies);
        model.addAttribute("expense", expense);
        return "expense-edit";
    }

    // Only accessed by expense user or group admin
    @PreAuthorize("hasPermission(#id, 'expense','all')")
    @GetMapping("/{id}/delete")
    public String deleteExpense(@PathVariable Long id) throws ValidationException, ExpenseNotFoundException {
        long splitGroupId = expenseService.getExpense(id).getSplitGroup().getId();
        expenseService.deleteExpense(id);
        return "redirect:/splitGroup/" + splitGroupId;
    }

    // Only accessed by expense user or group admin
    @PreAuthorize("hasPermission(#id, 'expense','all')")
    @PostMapping("/{id}")
    public String editExpense(@PathVariable Long id, @ModelAttribute Expense expense) throws ValidationException, ExpenseNotFoundException, CurrencyConversionException {
        expense = expenseService.editExpense(id, expense);
        return "redirect:/splitGroup/" + expense.getSplitGroup().getId();
    }

    // Only accessed by group member
    @PreAuthorize("hasPermission(#groupId,'splitGroup','user')")
    @GetMapping("/search")
    public String showExpenseSearch(@RequestParam(required = false, defaultValue = "0") Long groupId ,Model model, Authentication authentication) throws SplitGroupNotFoundException, SplitGroupListNotFoundException {
        User activeUser = (User) authentication.getPrincipal();
        ExpenseSearchFilter expenseSearchFilter = searchService.populateExpenseSearchOptions(activeUser);
        expenseSearchFilter.setUser(null);
        expenseSearchFilter.setCurrentPageNum(0);
        expenseSearchFilter.setNumPages(1);
        expenseSearchFilter.setIsSplit(true);

        SplitGroup splitGroup;

        if (groupId == 0){
            splitGroup = expenseSearchFilter.getSplitGroups().isEmpty() ? null : expenseSearchFilter.getSplitGroups().get(0);
        } else{
            splitGroup = groupService.getGroupById(groupId);
        }

        expenseSearchFilter.setSplitGroup(splitGroup);

        Page<Expense> expensesPage = expenseService.getExpenseBySplitGroupIdAndIsSplit(
                expenseSearchFilter.getSplitGroup().getId(),
                expenseSearchFilter.getIsSplit(),
                expenseSearchFilter.getCurrentPageNum());

        expenseSearchFilter.setNumPages(expensesPage.getTotalPages());
        List<Expense> expenses = expensesPage.getContent();

        model.addAttribute("filterOptions", expenseSearchFilter);
        model.addAttribute("expenses", expenses);

        return "expense-search";
    }

    // Only accessed by group member
    @PreAuthorize("hasPermission(#filterOptions.splitGroup.id,'splitGroup','user')")
    @PostMapping("/search")
    public String updateExpenseSearch(@ModelAttribute ExpenseSearchFilter filterOptions ,Model model, Authentication authentication) throws SplitGroupListNotFoundException {
        User activeUser = (User) authentication.getPrincipal();
        Integer pageNum = filterOptions.getSelectedPageNum() == null ? 0 : filterOptions.getSelectedPageNum();

        ExpenseSearchFilter expenseSearchFilter = searchService.populateExpenseSearchOptions(activeUser);
        expenseSearchFilter.setUser(filterOptions.getUser());
        expenseSearchFilter.setSplitGroup(filterOptions.getSplitGroup());
        expenseSearchFilter.setIsSplit(filterOptions.getIsSplit());
        expenseSearchFilter.setCurrentPageNum(pageNum);

        // Get search results and add to model

        Page<Expense> expensesPage;
        if (filterOptions.getUser() == null){
            expensesPage = expenseService.getExpenseBySplitGroupIdAndIsSplit(
                    expenseSearchFilter.getSplitGroup().getId(),
                    expenseSearchFilter.getIsSplit(),
                    expenseSearchFilter.getCurrentPageNum());
        } else {
            expensesPage = expenseService.getExpenseByUserIdAndSplitGroupIdAndIsSplit(
                    expenseSearchFilter.getUser().getId(),
                    expenseSearchFilter.getSplitGroup().getId(),
                    expenseSearchFilter.getIsSplit(),
                    expenseSearchFilter.getCurrentPageNum());
        }
        expenseSearchFilter.setNumPages(expensesPage.getTotalPages());
        List<Expense> expenses = expensesPage.getContent();

        model.addAttribute("filterOptions", expenseSearchFilter);
        model.addAttribute("expenses", expenses);

        return "expense-search";
    }
}
