package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountController {

    private final AccountService accounts;

    public AccountController(AccountService accounts) { this.accounts = accounts; }

    @GetMapping("/account")
    public String account(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        AppUser user = accounts.getById(userId);
        model.addAttribute("user", user);
        return "account";
    }

    @PostMapping("/account/add-funds")
    public String addFunds(HttpSession session,
                           @RequestParam double amount,
                           RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        try {
            accounts.addFunds(userId, amount);
            ra.addFlashAttribute("msg", "Funds added.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/account";
    }
}