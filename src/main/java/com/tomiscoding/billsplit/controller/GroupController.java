package com.tomiscoding.billsplit.controller;

import com.tomiscoding.billsplit.exceptions.*;
import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/splitGroup")
public class GroupController {

    @Autowired
    GroupService groupService;

    @Autowired
    UserService userService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    GroupMemberService groupMemberService;

    @Autowired
    MailerSendService mailerSendService;

    @Autowired
    ExpenseService expenseService;

    @GetMapping
    public String showGroupsOfUser(Authentication authentication, Model model){
        User user = (User) authentication.getPrincipal();
        List<SplitGroup> splitGroups = groupService.getGroupsByUser(user);
        model.addAttribute("splitGroups", splitGroups);
        return "splitGroups";
    }

    // Only allow group members to access
    @PreAuthorize("hasPermission(#id,'splitGroup','user')")
    @GetMapping("/{id}")
    public String showGroupOverview(@PathVariable Long id, Authentication authentication, Model model) throws SplitGroupNotFoundException {
        User user = (User) authentication.getPrincipal();
        SplitGroup splitGroup = groupService.getGroupWithExpensesMembersPaymentsById(id);
        List<User> users = userService.getUsersBySplitGroup(splitGroup);
        List<Payment> payments = paymentService.getPaymentsBySplitGroupAndUser(splitGroup, user);
        model.addAttribute("payments", payments);
        model.addAttribute("splitGroup", splitGroup);
        model.addAttribute("users", users);
        return "splitGroup";
    }

    // Only allow group admin to access
    @PreAuthorize("hasPermission(#id,'splitGroup','admin')")
    @GetMapping("/{id}/admin")
    public String showGroupAdminOverview(@PathVariable Long id, Model model) throws SplitGroupNotFoundException {
        SplitGroup splitGroup = groupService.getGroupWithExpensesMembersPaymentsById(id);
        List<User> users = userService.getUsersBySplitGroup(splitGroup);
        model.addAttribute("splitGroup", splitGroup);
        model.addAttribute("users", users);
        return "splitGroup-admin";
    }

    @GetMapping("/new")
    public String showCreateGroupPage(Model model){
        SplitGroup splitGroup = new SplitGroup();
        List<Currency> currencies = List.of(Currency.values());
        model.addAttribute("splitGroup", splitGroup);
        model.addAttribute("currencies", currencies);
        return "splitGroups-new";
    }

    // Only allow group admin to access
    @PreAuthorize("hasPermission(#id,'splitGroup','admin')")
    @GetMapping("/{id}/invite")
    public String showAddGroupMemberPage(@PathVariable Long id, Model model) throws SplitGroupNotFoundException {
        SplitGroup splitGroup = groupService.getGroupById(id);
        model.addAttribute("splitGroup", splitGroup);
        return "add-group-member";
    }

    // Only allow group admin to access
    @PreAuthorize("hasPermission(#id,'splitGroup','admin')")
    @PostMapping("/{id}/invite")
    public String sendInviteEmail(@PathVariable Long id,
                                  @RequestParam String emailAddress,
                                  Authentication authentication,
                                  Model model) throws SplitGroupNotFoundException, EmailSendException {
        User user = (User) authentication.getPrincipal();
        SplitGroup splitGroup = groupService.getGroupById(id);
        mailerSendService.sendInviteEmail(emailAddress, splitGroup, user);
        return "redirect:/splitGroup/" + id;
    }

    @PostMapping
    public String createGroup(@ModelAttribute SplitGroup splitGroup, Authentication authentication) throws ValidationException {
        User user = (User) authentication.getPrincipal();
        groupService.createGroup(splitGroup, user);
        return "redirect:/splitGroup";
    }

    // Only allow group admin to access
    @PreAuthorize("hasPermission(#id,'splitGroup','admin')")
    @GetMapping("/{id}/calculate")
    public String calculatePayments(@PathVariable Long id, Model model) throws SplitGroupNotFoundException {
        SplitGroup splitGroup = groupService.getGroupById(id);
        paymentService.calculateAndSavePayments(splitGroup);
        return "redirect:/splitGroup/" + id;
    }

    @GetMapping("/join/{inviteCode}")
    public String collectInviteLink(@PathVariable String inviteCode, HttpServletResponse response){
        Cookie cookie = new Cookie("inviteCode", inviteCode);
        cookie.setMaxAge(600);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/overview";
    }

    @GetMapping("/join")
    public String showJoinGroup(){
        return "join-group";
    }

    @PostMapping("/join")
    public String addUserToGroup(@RequestParam String inviteCode, Authentication authentication){
        if (!inviteCode.isBlank()){
            User user = (User) authentication.getPrincipal();
            try {
                groupService.addUserToGroupByInviteCode(user, inviteCode);
            } catch (ValidationException | SplitGroupNotFoundException | DuplicateGroupMemberException e) {
                // log exception here;
            }
        }
        return "redirect:/splitGroup";
    }

    // Only allow group admin to access
    @PreAuthorize("hasPermission(#groupId,'splitGroup','admin')")
    @GetMapping("/{groupId}/user/{userId}")
    public String changeGroupMember(@RequestParam String action,
                                         @PathVariable Long groupId,
                                         @PathVariable Long userId) throws GroupMemberNotFoundException, ValidationException {
        GroupMember groupMember = groupMemberService.getGroupMemberByGroupIdAndUserId(groupId, userId);
        switch (action) {
            case "add_admin":
                groupMemberService.makeGroupMemberAdmin(groupMember);
                break;
            case "remove_admin":
                groupMemberService.removeGroupMemberAdmin(groupMember, groupId);
                break;
            case "remove":
                groupMemberService.deleteGroupMember(groupMember, groupId);
                break;
        }
        return "redirect:/splitGroup/" + groupId + "/admin";
    }

    // Only allow group member to access
    @PreAuthorize("hasPermission(#splitGroupId,'splitGroup','user')")
    @GetMapping("/{splitGroupId}/expense/new")
    public String showCreateExpense(@PathVariable Long splitGroupId, Model model) throws SplitGroupNotFoundException {
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

    //Only allow group member to access
    @PreAuthorize("hasPermission(#splitGroupId,'splitGroup','user')")
    @PostMapping("/{splitGroupId}/expense/new")
    public String createNewExpense(@ModelAttribute Expense expense,
                                   @PathVariable Long splitGroupId,
                                   Authentication authentication) throws ValidationException, CurrencyConversionException {
        User user = (User) authentication.getPrincipal();
        expense.setUser(user);
        expenseService.saveExpense(expense);
        return "redirect:/splitGroup/" + splitGroupId;
    }
}
