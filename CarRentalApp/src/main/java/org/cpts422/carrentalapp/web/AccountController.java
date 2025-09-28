// Created by : Yevin
// Created on : Sep 28

// Last Updated by : Yevin
// Last Updated on : Sep 28

package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountController
{

    private final AppUserRepository users;

    public AccountController(AppUserRepository users)
    {
        this.users = users;
    }

    @GetMapping("/account")
    public String account(HttpSession session, Model model)
    {
        Long userId = (Long) session.getAttribute("userId");
        AppUser user = users.findById(userId).orElseThrow();
        model.addAttribute("user", user);
        return "account";
    }

    @PostMapping("/account/add-funds")
    public String addFunds(HttpSession session, @RequestParam double amount, RedirectAttributes ra)
    {
        if (amount <= 0) { ra.addFlashAttribute("msg", "Enter a positive amount."); return "redirect:/account"; }

        Long userId = (Long) session.getAttribute("userId");
        AppUser user = users.findById(userId).orElseThrow();
        user.setWalletBalance(Math.round((user.getWalletBalance() + amount) * 100.0) / 100.0);
        users.save(user);
        ra.addFlashAttribute("msg", "Funds added.");
        return "redirect:/account";
    }
}
