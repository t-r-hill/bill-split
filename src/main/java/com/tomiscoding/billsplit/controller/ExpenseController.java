package com.tomiscoding.billsplit.controller;

import com.tomiscoding.billsplit.dto.ExpenseSearchFilter;
import com.tomiscoding.billsplit.exceptions.ExpenseNotFoundException;
import com.tomiscoding.billsplit.exceptions.SplitGroupNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.Currency;
import com.tomiscoding.billsplit.model.Expense;
import com.tomiscoding.billsplit.model.User;
import com.tomiscoding.billsplit.service.ExpenseService;
import com.tomiscoding.billsplit.service.GroupService;
import com.tomiscoding.billsplit.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/expense")
public class ExpenseController {

    @Autowired
    ExpenseService expenseService;

    @Autowired
    GroupService groupService;

    @Autowired
    SearchService searchService;

    @GetMapping("/new")
    public String showCreateExpense(@RequestParam Long splitGroupId, Model model) throws SplitGroupNotFoundException {
        Expense expense = new Expense();
        if (splitGroupId != null){
            expense.setSplitGroup(groupService.getGroupById(splitGroupId));
        }
        expense.setExpenseDate(LocalDate.now());
        List<Currency> currencies = List.of(Currency.values());
        model.addAttribute("currencies", currencies);
        model.addAttribute("expense", expense);
        return "expense-new";
    }

    @GetMapping("/{id}/edit")
    public String showEditExpense(@PathVariable Long id, Model model) throws SplitGroupNotFoundException, ExpenseNotFoundException {
        Expense expense = expenseService.getExpense(id);
        List<Currency> currencies = List.of(Currency.values());
        model.addAttribute("currencies", currencies);
        model.addAttribute("expense", expense);
        return "expense-edit";
    }

    @GetMapping("/{id}/delete")
    public String deleteExpense(@PathVariable Long id) throws ValidationException, ExpenseNotFoundException {
        long splitGroupId = expenseService.getExpense(id).getSplitGroup().getId();
        expenseService.deleteExpense(id);
        return "redirect:/splitGroup/" + splitGroupId;
    }

    @PostMapping("/{id}")
    public String editExpense(@PathVariable Long id, @ModelAttribute Expense expense) throws ValidationException, ExpenseNotFoundException {
        expense = expenseService.editExpense(id, expense);
        return "redirect:/splitGroup/" + expense.getSplitGroup().getId();
    }

    @PostMapping
    public String createNewExpense(@ModelAttribute Expense expense, Authentication authentication) throws ValidationException {
        User user = (User) authentication.getPrincipal();
        expense.setUser(user);
        expense = expenseService.saveExpense(expense);
        return "redirect:/splitGroup/" + expense.getSplitGroup().getId();
    }

    @GetMapping("/search")
    public String showExpenseSearch(Model model, Authentication authentication){
        User activeUser = (User) authentication.getPrincipal();
        ExpenseSearchFilter expenseSearchFilter = searchService.populateExpenseSearchOptions(activeUser);
        model.addAttribute("filterOptions", expenseSearchFilter);
        return "expense-search";
    }

    @PostMapping("/search")
    public String updateExpenseSearch(@ModelAttribute ExpenseSearchFilter filterOptions ,Model model, Authentication authentication){
        User activeUser = (User) authentication.getPrincipal();

        // Maybe need to dynamically update the filterOptions so that selected values are pre-populated and options are responsively updated
        ExpenseSearchFilter expenseSearchFilter = searchService.populateExpenseSearchOptions(activeUser);
        expenseSearchFilter.setUser(filterOptions.getUser());
        expenseSearchFilter.setSplitGroup(filterOptions.getSplitGroup());
        expenseSearchFilter.setIsSplit(filterOptions.getIsSplit());
        model.addAttribute("filterOptions", expenseSearchFilter);

        // Get search results and add to model
        List<Expense> expenses;
        if (filterOptions.getUser() == null){
            expenses = expenseService.getExpenseBySplitGroupAndIsSplit(
                    filterOptions.getSplitGroup(),
                    filterOptions.getIsSplit());
        } else {
            expenses = expenseService.getExpenseByUserAndSplitGroupAndIsSplit(
                    filterOptions.getUser(),
                    filterOptions.getSplitGroup(),
                    filterOptions.getIsSplit());
        }

        model.addAttribute("expenses", expenses);

        return "expense-search";
    }
}
