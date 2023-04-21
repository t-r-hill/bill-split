package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.dto.GroupOverview;
import com.tomiscoding.billsplit.exceptions.DuplicateGroupMemberException;
import com.tomiscoding.billsplit.exceptions.SplitGroupNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberService groupMemberService;

    @Transactional
    public SplitGroup createGroup(SplitGroup splitGroup, User user) throws ValidationException{

        validateGroupProperties(splitGroup);

        while (true){
            String code = generateCode();
            if (groupRepository.findByInviteCode(code).isEmpty()){
                splitGroup.setInviteCode(code);
                break;
            }
        }

        GroupMember groupMember = GroupMember.builder()
                .user(user)
                .splitGroup(splitGroup)
                .isAdmin(true)
                .build();

        splitGroup.setGroupMembers(Collections.singletonList(groupMember));

        return groupRepository.save(splitGroup);
    }

    public SplitGroup getGroupById(Long id) throws SplitGroupNotFoundException {
        return groupRepository.findById(id).orElseThrow(
                () -> new SplitGroupNotFoundException("Could not find group with id: " + id)
        );
    }

    public SplitGroup getGroupWithExpensesMembersPaymentsById(Long id) throws SplitGroupNotFoundException {
        SplitGroup group = groupRepository.getSplitGroupWithExpensesById(id).orElseThrow(
                () -> new SplitGroupNotFoundException("Could not find group with id: " + id)
        );

        group = groupRepository.getSplitGroupWithGroupMembersById(id).orElseThrow(
                () -> new SplitGroupNotFoundException("Could not find group with id: " + id)
        );

        group = groupRepository.getSplitGroupWithPaymentsById(id).orElseThrow(
                () -> new SplitGroupNotFoundException("Could not find group with id: " + id)
        );

        return group;
    }

    public SplitGroup getGroupWithExpensesMembersById(Long id) throws SplitGroupNotFoundException {
        SplitGroup group = groupRepository.getSplitGroupWithExpensesById(id).orElseThrow(
                () -> new SplitGroupNotFoundException("Could not find group with id: " + id)
        );

        group = groupRepository.getSplitGroupWithGroupMembersById(id).orElseThrow(
                () -> new SplitGroupNotFoundException("Could not find group with id: " + id)
        );

        return group;
    }

    public void addUserToGroupByInviteCode(User user, String inviteCode) throws SplitGroupNotFoundException, ValidationException, DuplicateGroupMemberException {
        SplitGroup splitGroup = groupRepository.findByInviteCode(inviteCode).orElseThrow(
                () -> new SplitGroupNotFoundException("Could not find group with invite code: " + inviteCode)
        );

        GroupMember groupMember = GroupMember.builder()
                .user(user)
                .splitGroup(splitGroup)
                .isAdmin(false)
                .build();

        groupMemberService.createGroupMember(groupMember);
    }

    public List<SplitGroup> getGroupsByUser(User user){
        return groupRepository.getByGroupMembers_User(user);
    }

    public List<SplitGroup> getGroupsWithGroupMembersByUser(User user){
        return groupRepository.getSplitGroupWithGroupMembersByGroupMembers_User(user);
    }

    public GroupOverview generateGroupOverview(Long splitGroupId, Long userId) throws SplitGroupNotFoundException {
        SplitGroup splitGroup = getGroupWithExpensesMembersPaymentsById(splitGroupId);

        List<User> users = splitGroup.getGroupMembers().stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toList());

        List<Payment> payments = splitGroup.getPayments().stream()
                .filter(p -> p.getToUser().getId() == userId
                        || p .getFromUser().getId() == userId)
                .filter(p -> !p.getPaymentStatus().equals(PaymentStatus.PAID_CONFIRMED))
                .collect(Collectors.toList());

        List<Expense> expenses = splitGroup.getExpenses().stream()
                .sorted((e1,e2) -> e2.getExpenseDate().compareTo(e1.getExpenseDate()))
                .limit(10)
                .collect(Collectors.toList());

        return GroupOverview.builder()
                .userId(userId)
                .currentGroupExpenses(splitGroup.getExpensesNotSplitTotal())
                .currentUserExpenses(splitGroup.getExpensesNotSplitTotalByUserId(userId))
                .currentUserBalance(splitGroup.getOutstandingBalanceByUserId(userId))
                .totalGroupExpenses(splitGroup.getExpensesTotal())
                .totalUserExpenses(splitGroup.getExpensesTotalByUserId(userId))
                .confirmedUserPayments(splitGroup.getConfirmedPaymentsTotalForUserId(userId))
                .notConfirmedUserPayments(splitGroup.getNotConfirmedPaymentsTotalForUserId(userId))
                .userPayments(payments)
                .recentExpenses(expenses)
                .groupMemberUsers(users)
                .build();
    }

    public GroupOverview generateAdminGroupOverview(Long splitGroupId) throws SplitGroupNotFoundException {
        SplitGroup splitGroup = getGroupWithExpensesMembersPaymentsById(splitGroupId);

        List<Payment> payments = splitGroup.getPayments().stream()
                .filter(p -> !p.getPaymentStatus().equals(PaymentStatus.PAID_CONFIRMED))
                .collect(Collectors.toList());

        List<Expense> expenses = splitGroup.getExpenses().stream()
                .filter(e -> !e.isSplit())
                .sorted((e1,e2) -> e2.getExpenseDate().compareTo(e1.getExpenseDate()))
                .limit(10)
                .collect(Collectors.toList());

        return GroupOverview.builder()
                .currentGroupExpenses(splitGroup.getExpensesNotSplitTotal())
                .totalGroupExpenses(splitGroup.getExpensesTotal())
                .userPayments(payments)
                .recentExpenses(expenses)
                .build();
    }

    // Helper function to generate a random code to externally identify the group
    private String generateCode(){
        String chars = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ1234567890";
        int ub = chars.length() - 1;
        Random random = new Random();
        StringBuilder builder = new StringBuilder(8);
        for (int i = 0; i < 8; i++){
            builder.append(chars.charAt(random.nextInt(ub)));
        }
        return builder.toString();
    }

    private void validateGroupProperties(SplitGroup splitGroup) throws ValidationException {
        if (splitGroup.getGroupName() == null || splitGroup.getGroupName().isBlank()){
            throw new ValidationException("Name must not be blank");
        } else if (splitGroup.getBaseCurrency() == null) {
            throw new ValidationException("A base currency must be selected");
        }
    }
}
