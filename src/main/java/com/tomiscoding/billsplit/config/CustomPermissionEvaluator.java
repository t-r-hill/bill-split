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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {


    private final ExpenseService expenseService;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupService groupService;
    private final PaymentService paymentService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        User activeUser = (User) authentication.getPrincipal();

        if (targetType.equalsIgnoreCase("expense")){
            try {
                Expense expense = expenseService.getExpense(Long.parseLong(targetId.toString()));
                return activeUser.getId() == expense.getUser().getId() ||
                        groupMemberRepository.existsByUserIdAndSplitGroupIdAndIsAdmin(activeUser.getId(), expense.getSplitGroup().getId(), true);
            } catch (ExpenseNotFoundException e) {
                return true;
            }
        } else if (targetType.equalsIgnoreCase("splitGroup")) {
            try {
                SplitGroup splitGroup = groupService.getGroupById(Long.parseLong(targetId.toString()));
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
                Payment payment = paymentService.getPaymentById(Long.parseLong(targetId.toString()));
                if (permission.equals("PAID_PENDING")){
                    return activeUser.getId() == payment.getFromUser().getId();
                } else if (permission.equals("PAID_CONFIRMED")) {
                    return activeUser.getId() == payment.getToUser().getId();
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
