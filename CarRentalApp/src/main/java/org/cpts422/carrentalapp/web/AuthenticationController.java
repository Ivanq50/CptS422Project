package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.MembershipType;
import org.cpts422.carrentalapp.service.AuthenticationService;
import org.cpts422.carrentalapp.service.error.DriversLicenseTakenException;
import org.cpts422.carrentalapp.service.error.DuplicateUsernameException;
import org.cpts422.carrentalapp.web.datatransfers.LoginForm;
import org.cpts422.carrentalapp.web.datatransfers.RegistrationForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthenticationController {

    private final AuthenticationService auth;

    public AuthenticationController(AuthenticationService auth) { this.auth = auth; }

    // Always have dropdown options available
    @ModelAttribute("membershipTypes")
    public MembershipType[] membershipTypes() { return MembershipType.values(); }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("form", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("form") LoginForm form,
                        BindingResult errors, HttpSession session) {
        if (errors.hasErrors()) return "login";
        try {
            AppUser user = auth.authenticate(form.getUsername(), form.getPassword());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("userId", user.getId());
            return "redirect:/";
        } catch (IllegalArgumentException ex) {
            errors.rejectValue("password", "invalid", ex.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegistrationForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form,
                           BindingResult errors) {
        if (errors.hasErrors()) return "register";
        try {
            auth.register(form);
            return "register_successful";
        } catch (DuplicateUsernameException ex) {
            errors.rejectValue("username", "duplicate", ex.getMessage());
            return "register";
        } catch (DriversLicenseTakenException ex) {
            errors.rejectValue("driversLicenseNumber", "duplicate", ex.getMessage());
            return "register";
        } catch (RuntimeException ex) {
            errors.reject("registration", ex.getMessage() == null ? "Registration failed." : ex.getMessage());
            return "register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}