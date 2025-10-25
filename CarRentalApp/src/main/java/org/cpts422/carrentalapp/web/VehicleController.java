package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.service.AccountService;
import org.cpts422.carrentalapp.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VehicleController {

    private final VehicleService vehicles;
    private final AccountService accounts;

    public VehicleController(VehicleService vehicles, AccountService accounts) {
        this.vehicles = vehicles; this.accounts = accounts;
    }

    @GetMapping("/vehicles")
    public String list(@RequestParam(required = false) Boolean available, Model model, HttpSession session) {
        Long uid = (Long) session.getAttribute("userId");
        if (uid != null) {
            AppUser currentUser = accounts.getById(uid);
            model.addAttribute("currentUser", currentUser);
        }
        model.addAttribute("vehicles", vehicles.list(available));
        return "vehicles";
    }
}
