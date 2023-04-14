package com.tomiscoding.billsplit.controller;

import com.tomiscoding.billsplit.exceptions.DuplicateGroupMemberException;
import com.tomiscoding.billsplit.exceptions.GroupMemberNotFoundException;
import com.tomiscoding.billsplit.exceptions.SplitGroupNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.*;
import com.tomiscoding.billsplit.service.GroupMemberService;
import com.tomiscoding.billsplit.service.GroupService;
import com.tomiscoding.billsplit.service.PaymentService;
import com.tomiscoding.billsplit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
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

    @GetMapping
    public String showGroupsOfUser(Authentication authentication, Model model){
        User user = (User) authentication.getPrincipal();
        List<SplitGroup> splitGroups = groupService.getGroupsWithGroupMembersByUser(user);
        model.addAttribute("splitGroups", splitGroups);
        return "splitGroups";
    }

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

    @GetMapping("/{id}/user/new")
    public String showAddGroupMemberPage(@PathVariable Long id, Model model) throws SplitGroupNotFoundException {
        SplitGroup splitGroup = groupService.getGroupById(id);
        model.addAttribute("splitGroup", splitGroup);
        return "add-group-member";
    }

    @PostMapping
    public String createGroup(@ModelAttribute SplitGroup splitGroup, Authentication authentication) throws ValidationException {
        User user = (User) authentication.getPrincipal();
        groupService.createGroup(splitGroup, user);
        return "redirect:/splitGroup";
    }

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
}
