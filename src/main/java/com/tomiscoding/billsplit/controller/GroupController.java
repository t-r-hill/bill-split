package com.tomiscoding.billsplit.controller;

import com.tomiscoding.billsplit.exceptions.SplitGroupNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.Currency;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import com.tomiscoding.billsplit.service.GroupService;
import com.tomiscoding.billsplit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class GroupController {

    @Autowired
    GroupService groupService;

    @Autowired
    UserService userService;

    @GetMapping("/splitGroup")
    public String showGroupsOfUser(Authentication authentication, Model model){
        User user = (User) authentication.getPrincipal();
        List<SplitGroup> splitGroups = groupService.getGroupsByUser(user);
        model.addAttribute("splitGroups", splitGroups);
        return "splitGroups";
    }

    @GetMapping("/splitGroup/{id}")
    public String showGroupWithUsersAndExpenses(@PathVariable Long id, Model model) throws SplitGroupNotFoundException {
        SplitGroup splitGroup = groupService.getGroupById(id);
        List<User> users = userService.getUsersBySplitGroup(splitGroup);
        model.addAttribute("splitGroup", splitGroup);
        model.addAttribute("users", users);
        return "splitGroup";
    }

    @GetMapping("/splitGroup/new")
    public String showCreateGroupPage(Model model){
        SplitGroup splitGroup = new SplitGroup();
        List<Currency> currencies = List.of(Currency.values());
        model.addAttribute("splitGroup", splitGroup);
        model.addAttribute("currencies", currencies);
        return "splitGroups-new";
    }

    @GetMapping("/splitGroup/{id}/user/new")
    public String showAddGroupMemberPage(@PathVariable Long id, Model model) throws SplitGroupNotFoundException {
        SplitGroup splitGroup = groupService.getGroupById(id);
        model.addAttribute("splitGroup", splitGroup);
        return "add-group-member";
    }

    @PostMapping("/splitGroup")
    public String createGroup(@ModelAttribute SplitGroup splitGroup, Authentication authentication) throws ValidationException {
        User user = (User) authentication.getPrincipal();
        groupService.createGroup(splitGroup, user);
        return "redirect:/splitGroup";
    }
}
