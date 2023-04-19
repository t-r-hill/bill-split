package com.tomiscoding.billsplit.config;

import com.tomiscoding.billsplit.exceptions.ExpenseNotFoundException;
import com.tomiscoding.billsplit.exceptions.PaymentNotFoundException;
import com.tomiscoding.billsplit.exceptions.SplitGroupNotFoundException;
import com.tomiscoding.billsplit.model.Expense;
import com.tomiscoding.billsplit.model.Payment;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import com.tomiscoding.billsplit.repository.GroupMemberRepository;
import com.tomiscoding.billsplit.service.ExpenseService;
import com.tomiscoding.billsplit.service.GroupService;
import com.tomiscoding.billsplit.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    ExpenseService expenseService;
    
    @Autowired
    GroupMemberRepository groupMemberRepository;

    @Autowired
    GroupService groupService;

    @Autowired
    PaymentService paymentService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        User activeUser = (User) authentication.getPrincipal();

        if (targetType.equalsIgnoreCase("expense")){
            try {
                Expense expense = expenseService.getExpense(Long.getLong(targetId.toString()));
                return activeUser.getId() == expense.getUser().getId() ||
                        groupMemberRepository.existsByUserIdAndSplitGroupIdAndIsAdmin(activeUser.getId(), expense.getSplitGroup().getId(), true);
            } catch (ExpenseNotFoundException e) {
                return true;
            }
        } else if (targetType.equalsIgnoreCase("splitGroup")) {
            try {
                SplitGroup splitGroup = groupService.getGroupById(Long.getLong(targetId.toString()));
                if (permission.equals("user")) {
                    return groupMemberRepository.existsByUserAndSplitGroup(activeUser, splitGroup);
                } else if (permission.equals("admin")) {
                    return groupMemberRepository.existsByUserIdAndSplitGroupIdAndIsAdmin(activeUser.getId(), splitGroup.getId(), true);
                } else {
                    return false;
                }
            } catch (SplitGroupNotFoundException e) {
                return true;
            }
        } else if (targetType.equalsIgnoreCase("payment")){
            try {
                Payment payment = paymentService.getPaymentById(Long.getLong(targetId.toString()));
                if (permission.equals("pending")){
                    return activeUser.equals(payment.getFromUser());
                } else if (permission.equals("confirmed")) {
                    return activeUser.equals(payment.getToUser());
                } else {
                    return false;
                }
            } catch (PaymentNotFoundException e) {
                return true;
            }
        }
        return false;
    }
}
