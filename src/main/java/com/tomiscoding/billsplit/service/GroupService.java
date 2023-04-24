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

/**
 * Handles CRUD operations for SplitGroup objects, as well as handling joining groups by invite code and
 * generating the overview DTOs for displaying split groups to users.
 */
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberService groupMemberService;

    /**
     * Creates a splitGroup and in the process, generates an invitation code for the group using generateCode() and
     * creates a group member and sets them as admin. Will revert if group member cannot be added
     * @param splitGroup the group to be created
     * @param user the user to be added as group admin
     * @return the saved splitGroup with group member admin
     * @throws ValidationException
     */
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

    /**
     *
     * @param id the id of the group to load
     * @return A splitGroup with expenses, members and payments loaded by Hibernate
     * @throws SplitGroupNotFoundException
     */
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

    /**
     * Creates and saves a group member and thus adding a user to a group.
     * @param user the user to be added to a group
     * @param inviteCode the invite code for the group to be joined
     * @throws SplitGroupNotFoundException
     * @throws ValidationException
     * @throws DuplicateGroupMemberException
     */
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

    /**
     * Filters users, payments and expenses and calculates expense and payment balances and totals to be displyed
     * to the user in a group overview
     * @param splitGroupId the group to generate a GroupOverview for
     * @param userId the user to generate the GroupOverview for
     * @return GroupOverview
     * @throws SplitGroupNotFoundException
     */
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

    /**
     * Filters users, payments and expenses and calculates expense and payment balances and totals to be displayed
     * to the user in a group overview
     * @param splitGroupId the group to generate a GroupOverview for
     * @return GroupOverview
     * @throws SplitGroupNotFoundException
     */
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
