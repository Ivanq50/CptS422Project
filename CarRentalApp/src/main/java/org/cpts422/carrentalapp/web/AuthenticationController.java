/*
Web controller for Registration, Login and Logout
*/

// Created by : Yevin
// Created on : Sep 22

// Last Updated by : Yevin
// Last Updated on : Sep 22

package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.MembershipType;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.service.AuthenticationService;
import org.cpts422.carrentalapp.web.datatransfers.LoginForm;
import org.cpts422.carrentalapp.web.datatransfers.RegistrationForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthenticationController
{

    private final AuthenticationService auth;
    private final AppUserRepository users;

    public AuthenticationController(AuthenticationService auth, AppUserRepository users)
    {
        this.auth = auth;
        this.users = users;
    }

    // ---------- Registration ----------
    @GetMapping("/register")
    public String registerPage(Model model)
    {
        model.addAttribute("form", new RegistrationForm());
        model.addAttribute("membershipTypes", MembershipType.values());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute("form") RegistrationForm form,
                             BindingResult errors,
                             Model model)
    {

        if (errors.hasErrors())
        {
            model.addAttribute("membershipTypes", MembershipType.values());
            return "register";
        }
        if (users.existsByUsername(form.getUsername()))
        {
            errors.rejectValue("username", "duplicate", "Username is already taken");
        }
        if (users.existsByDriversLicenseNumber(form.getDriversLicenseNumber()))
        {
            errors.rejectValue("driversLicenseNumber", "duplicate", "Driver’s license number already used");
        }
        if (errors.hasErrors())
        {
            model.addAttribute("membershipTypes", MembershipType.values());
            return "register";
        }

        AppUser u = new AppUser();
        u.setUsername(form.getUsername());
        u.setAge(form.getAge());
        u.setDriversLicenseNumber(form.getDriversLicenseNumber());
        u.setDriversLicenseExpiry(form.getDriversLicenseExpiry());
        u.setMembershipType(form.getMembershipType());

        auth.register(u, form.getPassword());
        return "redirect:/register/success";
    }

    @GetMapping("/register/success")
    public String registerSuccess()
    {
        return "register_successful";
    }

    // ---------- Login ----------
    @GetMapping("/login")
    public String loginPage(Model model)
    {
        model.addAttribute("form", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@Valid @ModelAttribute("form") LoginForm form,
                          BindingResult errors,
                          HttpSession session,
                          Model model)
    {

        if (errors.hasErrors())
        {
            return "login";
        }

        try
        {
            var user = auth.authenticate(form.getUsername(), form.getPassword());

            // Keep the session
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userId", user.getId());
            return "redirect:/";
        }
        catch (IllegalArgumentException ex)
        {
            errors.rejectValue("password", "invalid", ex.getMessage());
            return "login";
        }
    }

    // ---------- Logout ----------
    @PostMapping("/logout")
    public String logout(HttpSession session)
    {
        session.invalidate();
        return "redirect:/";
    }
}
