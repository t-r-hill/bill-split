package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.DuplicateGroupMemberException;
import com.tomiscoding.billsplit.exceptions.GroupMemberNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.Expense;
import com.tomiscoding.billsplit.model.GroupMember;
import com.tomiscoding.billsplit.model.Payment;
import com.tomiscoding.billsplit.model.PaymentStatus;
import com.tomiscoding.billsplit.repository.GroupMemberRepository;
import com.tomiscoding.billsplit.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseService expenseService;
    private final PaymentRepository paymentRepository;

    /**
     *
     * @param groupMember to be persisted
     * @return GroupMember which has been saved
     * @throws ValidationException if group member fields not correctly populated
     * @throws DuplicateGroupMemberException if group member already exists
     */
    public GroupMember createGroupMember(GroupMember groupMember) throws ValidationException, DuplicateGroupMemberException {
        validateGroupMember(groupMember);
        if (groupMemberRepository.existsByUserAndSplitGroup(groupMember.getUser(), groupMember.getSplitGroup())){
            throw new DuplicateGroupMemberException(groupMember.getUser().toString() + " is already a member of group: " + groupMember.getSplitGroup());
        }
        return groupMemberRepository.save(groupMember);
    }

    public GroupMember updateGroupMember(GroupMember groupMember) throws ValidationException {
        validateGroupMember(groupMember);
        return groupMemberRepository.save(groupMember);
    }

    public GroupMember makeGroupMemberAdmin(GroupMember groupMember) throws ValidationException {
        groupMember.setAdmin(true);
        return updateGroupMember(groupMember);
    }

    /**
     * Removes a group member as admin ensuring that at least one admin is left in the group
     * @param groupMember the group member to be removed as admin
     * @param splitGroupId the group to be removed as admin from
     * @throws ValidationException if there is less than one admin left in the group
     */
    public void removeGroupMemberAdmin(GroupMember groupMember, Long splitGroupId) throws ValidationException {
        List<GroupMember> adminGroupMembers = groupMemberRepository.getGroupMembersBySplitGroupIdAndIsAdmin(splitGroupId, true);
        if (adminGroupMembers.size() > 1){
            groupMember.setAdmin(false);
            updateGroupMember(groupMember);
        } else {
            throw new ValidationException("There must be at least one admin in group with id: " + splitGroupId);
        }
    }

    /**
     * Deletes a group member ensuring that the user has no outstanding payments within the group and removes any expenses
     * which have not yet been split
     * @param groupMember the group member to be deleted
     * @param splitGroupId the group to be removed from
     * @throws ValidationException from removeGroupMemberAdmin() method call
     */
    @Transactional
    public void deleteGroupMember(GroupMember groupMember, Long splitGroupId) throws ValidationException {
        List<Expense> expenses = expenseService.getExpenseByUserIdAndSplitGroupId(groupMember.getUser().getId(), splitGroupId);
        assertGroupMemberHasNoOutstandingPayments(groupMember);
        expenseService.deleteExpensesList(expenses);
        removeGroupMemberAdmin(groupMember, splitGroupId);
        groupMemberRepository.delete(groupMember);
    }

    public GroupMember getGroupMemberByGroupIdAndUserId(Long groupId, Long userId) throws GroupMemberNotFoundException {
        return groupMemberRepository.getByUserIdAndSplitGroupId(userId, groupId).orElseThrow(
                () -> new GroupMemberNotFoundException("There is no group member with group id = " + groupId + " user id = " + userId)
        );
    }

    /**
     * Helper method to check whether a group member has any outstanding payments
     * @param groupMember
     * @throws ValidationException if group member has outstanding payments
     */
    private void assertGroupMemberHasNoOutstandingPayments(GroupMember groupMember) throws ValidationException {
        List<Payment> payments = paymentRepository.getPaymentsBySplitGroupAndUser(groupMember.getSplitGroup(), groupMember.getUser());
        List<Payment> outstandingPayments = payments.stream()
                .filter(p -> !p.getPaymentStatus().equals(PaymentStatus.PAID_CONFIRMED))
                .collect(Collectors.toList());
        if (!outstandingPayments.isEmpty()){
            throw new ValidationException("Cannot remove group remember while they still have outstanding payments");
        }
    }

    private void validateGroupMember(GroupMember groupMember) throws ValidationException {
        if (groupMember.getSplitGroup() == null){
            throw new ValidationException("A group must be selected");
        } else if (groupMember.getUser() == null) {
            throw new ValidationException("A user must be selected");
        }
    }
}
