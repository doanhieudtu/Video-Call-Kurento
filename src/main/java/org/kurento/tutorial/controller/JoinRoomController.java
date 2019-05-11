package org.kurento.tutorial.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes({"email"})
public class JoinRoomController {
	
	@GetMapping("join-call")
	public String JoinRoom(@RequestParam String room,Model model) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			  String username = ((UserDetails)principal).getUsername();
			  System.out.println("user:"+username);
			  model.addAttribute("email", username);
		}
		return "th_join-call";
	}
	
}
