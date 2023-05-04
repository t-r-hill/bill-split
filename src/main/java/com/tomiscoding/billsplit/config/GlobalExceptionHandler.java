package com.tomiscoding.billsplit.config;

import com.tomiscoding.billsplit.aspect.LogException;
import com.tomiscoding.billsplit.exceptions.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CurrencyConversionException.class)
    @LogException
    public String handleCurrencyConversionException(CurrencyConversionException currencyConversionException, Model model){
        model.addAttribute("pageName", "Error");
        model.addAttribute("message", "We couldn't complete your request, please try again. If you re-encounter this issue please contact support on tom@tomiscoding.com");
        model.addAttribute("detail", currencyConversionException.getMessage());
        model.addAttribute("redirectPath", "splitGroup");
        model.addAttribute("redirectText", "Return to groups");
        return "error";
    }

    @ExceptionHandler(DuplicateGroupMemberException.class)
    @LogException
    public String handleDuplicateGroupMemberException(DuplicateGroupMemberException duplicateGroupMemberException, Model model){
        model.addAttribute("pageName", "Already group member");
        model.addAttribute("message", "You can't be added to this group as you are already a part of it");
        model.addAttribute("detail", duplicateGroupMemberException.getMessage());
        model.addAttribute("redirectPath", "join");
        model.addAttribute("redirectText", "Join another group");
        return "error";
    }

    @ExceptionHandler(EmailSendException.class)
    @LogException
    public String handleEmailSendException(EmailSendException emailSendException, Model model){
        model.addAttribute("pageName", "Invite email error");
        model.addAttribute("message", "The invite email couldn't be sent, please try again");
        model.addAttribute("detail", emailSendException.getMessage());
        model.addAttribute("redirectPath", "splitGroup");
        model.addAttribute("redirectText", "Return to groups");
        return "error";
    }

    @ExceptionHandler(ExpenseNotFoundException.class)
    @LogException
    public String handleExpenseNotFoundException(ExpenseNotFoundException expenseNotFoundException, Model model){
        model.addAttribute("pageName", "404 - Not found");
        model.addAttribute("message", "An expense could not be found");
        model.addAttribute("detail", expenseNotFoundException.getMessage());
        model.addAttribute("redirectPath", "splitGroup");
        model.addAttribute("redirectText", "Return to groups");
        return "error";
    }

    @ExceptionHandler(GroupMemberNotFoundException.class)
    @LogException
    public String handleGroupMemberNotFoundException(GroupMemberNotFoundException groupMemberNotFoundException, Model model){
        model.addAttribute("pageName", "404 - Not found");
        model.addAttribute("message", "A group member could not be found");
        model.addAttribute("detail", groupMemberNotFoundException.getMessage());
        model.addAttribute("redirectPath", "splitGroup");
        model.addAttribute("redirectText", "Return to groups");
        return "error";
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    @LogException
    public String handlePaymentNotFoundException(PaymentNotFoundException paymentNotFoundException, Model model){
        model.addAttribute("pageName", "404 - Not found");
        model.addAttribute("message", "A payment could not be found");
        model.addAttribute("detail", paymentNotFoundException.getMessage());
        model.addAttribute("redirectPath", "splitGroup");
        model.addAttribute("redirectText", "Return to groups");
        return "error";
    }

    // Replace with try catch in handler
    @ExceptionHandler(SplitGroupListNotFoundException.class)
    @LogException
    public String handleSplitGroupListNotFoundException(SplitGroupListNotFoundException splitGroupListNotFoundException, Model model){
        model.addAttribute("pageName", "Expense Search");
        model.addAttribute("message", "You aren't a member of any groups");
        model.addAttribute("detail", "Click the button below and either create or join a group");
        model.addAttribute("redirectPath", "splitGroup");
        model.addAttribute("redirectText", "Return to groups");
        return "error";
    }

    @ExceptionHandler(SplitGroupNotFoundException.class)
    @LogException
    public String handleSplitGroupNotFoundException(SplitGroupNotFoundException splitGroupNotFoundException, Model model){
        model.addAttribute("pageName", "404 - Not found");
        model.addAttribute("message", "A group could not be found");
        model.addAttribute("detail", splitGroupNotFoundException.getMessage());
        model.addAttribute("redirectPath", "splitGroup");
        model.addAttribute("redirectText", "Return to groups");
        return "error";
    }

    @ExceptionHandler(ValidationException.class)
    @LogException
    public String handleValidationException(ValidationException validationException, Model model){
        model.addAttribute("pageName", "Input Validation Error");
        model.addAttribute("message", "There was an error with your input");
        model.addAttribute("detail", validationException.getMessage());
        model.addAttribute("redirectPath", "splitGroup");
        model.addAttribute("redirectText", "Return to groups");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @LogException
    public String handleException(Exception exception, Model model){
        model.addAttribute("pageName", "Error");
        model.addAttribute("message", "There was a server error");
        model.addAttribute("redirectPath", "");
        model.addAttribute("redirectText", "Return home");
        return "error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @LogException
    public String handleAccessDeniedException(AccessDeniedException exception, Model model){
        model.addAttribute("pageName", "Access denied");
        model.addAttribute("message", "You don't have permission to view this page");
        model.addAttribute("redirectPath", "");
        model.addAttribute("redirectText", "Return home");
        return "error";
    }

}
