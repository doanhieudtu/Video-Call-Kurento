package org.kurento.tutorial.groupcall;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
	@GetMapping(value = { "/call" })
    public String testThymeleafView(@RequestParam String room ) {
        return "th_login";
    }
}
