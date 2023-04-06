package com.tomiscoding.billsplit.controller;

import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.Currency;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import com.tomiscoding.billsplit.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class GroupController {

    @Autowired
    GroupService groupService;

    @GetMapping("/splitGroup")
    public String showGroupsOfUser(Authentication authentication, Model model){
        User user = (User) authentication.getPrincipal();
        List<SplitGroup> splitGroups = groupService.getGroupsByUser(user);
        model.addAttribute("splitGroups", splitGroups);
        return "splitGroups";
    }

    @GetMapping("/splitGroup/new")
    public String showCreateGroupPage(Model model){
        SplitGroup splitGroup = new SplitGroup();
        List<Currency> currencies = List.of(Currency.values());
        model.addAttribute("splitGroup", splitGroup);
        model.addAttribute("currencies", currencies);
        return "splitGroups-new";
    }

    @PostMapping("/splitGroup")
    public String createGroup(@ModelAttribute SplitGroup splitGroup, Authentication authentication) throws ValidationException {
        User user = (User) authentication.getPrincipal();
        groupService.createGroup(splitGroup, user);
        return "redirect:/splitGroup";
    }
}
