/*
controller to answer browser requests
*/

// Created by : Yevin
// Created on : Sep 22

// Last Updated by : Yevin
// Last Updated on : Sep 22

package org.cpts422.carrentalapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController
{
    @GetMapping("/")
    public String home(Model model)
    {
        model.addAttribute("title", "CarRentalApp");
        model.addAttribute("message", "It works");
        return "home";
    }
}
