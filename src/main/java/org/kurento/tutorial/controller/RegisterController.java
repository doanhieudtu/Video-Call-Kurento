package org.kurento.tutorial.controller;

import javax.validation.Valid;

import org.kurento.tutorial.dao.UserDao;
import org.kurento.tutorial.model.User;
import org.kurento.tutorial.model.UserForm;
import org.kurento.tutorial.utils.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class RegisterController{
	
	@Autowired
	UserValidator userValidator;
	
	@InitBinder
	protected void initBinder(WebDataBinder dataBinder) {
	     dataBinder.setValidator(userValidator);
	}
	
	@Autowired
	UserDao userDao;
	
	@RequestMapping(value="/register",method=RequestMethod.POST)
    public String saveRegister(@ModelAttribute @Valid UserForm userForm,BindingResult result) {
		if(result.hasErrors()) {
			   return "th_index";
		}
		try { 
			BCryptPasswordEncoder bCryptPasswordEncoder= new BCryptPasswordEncoder();
            User user= new User();
            user.setEmail(userForm.getEmail());
            user.setRole("ROLE_USER");
            user.setPassword(bCryptPasswordEncoder.encode(userForm.getPassword()));
        	userDao.save(user);
        }
        catch (Exception e) {
        	return "th_index";
        }
        return "th_login";
	}
}
