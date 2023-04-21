package com.tomiscoding.billsplit.controller;

import com.tomiscoding.billsplit.exceptions.DuplicateGroupMemberException;
import com.tomiscoding.billsplit.exceptions.SplitGroupNotFoundException;
import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.User;
import com.tomiscoding.billsplit.service.GroupService;
import com.tomiscoding.billsplit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GroupService groupService;

//    @GetMapping("/login")
//    public String showLoginPage(Model model){
//        User user = User.builder().build();
//        model.addAttribute("user", user);
//        return "login";
//    }

    @GetMapping("/login")
    public String showLoginErrorPage(@RequestParam(required = false, defaultValue = "false") Boolean error, Model model){
        User user = User.builder().build();
        model.addAttribute("user", user);
        if (error){
            model.addAttribute("errorMessage", "error");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model){
        User user = User.builder().build();
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/register")
    public String createUserAccount(@ModelAttribute User user, Model model){
        try{
            userService.createNewUser(user);
            return "register-success";
        } catch (ValidationException e) {
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/overview")
    public String showOverviewPage(Authentication authentication, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String inviteCode = "";
        if (cookies != null){
            inviteCode = Arrays.stream(cookies)
                    .filter(c -> c.getName().equals("inviteCode"))
                    .min(Comparator.comparingInt(Cookie::getMaxAge))
                    .orElse(new Cookie("inviteCode", ""))
                    .getValue();
        }
        if (!inviteCode.isBlank()){
            User user = (User) authentication.getPrincipal();
            try {
                groupService.addUserToGroupByInviteCode(user, inviteCode);
            } catch (ValidationException | SplitGroupNotFoundException | DuplicateGroupMemberException e) {
                // log exception here;
            }
        }
        return "overview";
    }

    @GetMapping("/")
    public String showHomePage(){
        return "home";
    }

}
