/*
Web controller for Registration
*/

// Created by : Yevin
// Created on : Sep 22

// Last Updated by : Yevin
// Last Updated on : Sep 22

package org.cpts422.carrentalapp.web;

import jakarta.validation.Valid;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.service.AuthenticationService;
import org.cpts422.carrentalapp.web.datatransfers.RegistrationForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthenticationController {
    private final AuthenticationService auth;

    public AuthenticationController(AuthenticationService auth) { this.auth = auth; }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute("form") RegistrationForm form,
                             BindingResult errors,
                             Model model) {
        if (errors.hasErrors()) return "register";
        try {
            AppUser u = new AppUser();
            u.setUsername(form.username);
            u.setAge(form.age);
            u.setDriversLicenseNumber(form.driversLicenseNumber);
            u.setDriversLicenseExpiry(form.driversLicenseExpiry);
            u.setMembershipType(form.membershipType);

            auth.register(u, form.password);

            model.addAttribute("flash", "Account created. You can sign in now.");
            // keep user on a confirmation page or redirect to /login later
            return "register_success";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }
}
