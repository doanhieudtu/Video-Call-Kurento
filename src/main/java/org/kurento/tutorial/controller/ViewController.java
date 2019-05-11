package org.kurento.tutorial.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class ViewController {

	@GetMapping("/login")
    public String viewLogin() {
        return "th_login";
    }
}
