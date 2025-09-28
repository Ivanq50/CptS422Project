package org.cpts422.carrentalapp.web;

import jakarta.servlet.http.HttpSession;
import org.cpts422.carrentalapp.model.AppUser;
import org.cpts422.carrentalapp.model.Vehicle;
import org.cpts422.carrentalapp.repo.AppUserRepository;
import org.cpts422.carrentalapp.repo.VehicleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class VehicleController {

    private final VehicleRepository vehicles;
    private final AppUserRepository users;

    public VehicleController(VehicleRepository vehicles, AppUserRepository users) {
        this.vehicles = vehicles;
        this.users = users;
    }

    @GetMapping("/vehicles")
    public String listVehicles(Model model,
                               @RequestParam(name = "available", required = false) Boolean available,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (session.getAttribute("userId") == null) {
            ra.addFlashAttribute("msg", "Please sign in to browse vehicles.");
            return "redirect:/login";
        }

        Long uid = (Long) session.getAttribute("userId");
        AppUser currentUser = users.findById(uid).orElse(null);
        model.addAttribute("currentUser", currentUser);

        List<Vehicle> list = (available == null)
                ? vehicles.findAll()
                : vehicles.findByAvailable(available);
        model.addAttribute("vehicles", list);
        return "vehicles";
    }
}
