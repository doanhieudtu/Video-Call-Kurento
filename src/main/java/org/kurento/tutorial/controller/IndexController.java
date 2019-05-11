package org.kurento.tutorial.controller;

import org.kurento.tutorial.model.UserForm;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class IndexController {

	
	@RequestMapping(value ="/index", method=RequestMethod.GET )
    public String viewRegister(UserForm userForm) {
        return "th_index";
    }
	@RequestMapping(value ="/work", method=RequestMethod.GET)
    public String viewWork( Model model) {

		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			  String username = ((UserDetails)principal).getUsername();
			  System.out.println("user:"+username);
			  model.addAttribute("email", username);
		}
        return "th_get_link";
    }
}
	