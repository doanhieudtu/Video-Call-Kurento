

package org.kurento.tutorial.utils;


import org.apache.commons.validator.routines.EmailValidator;
import org.kurento.tutorial.dao.UserDao;
import org.kurento.tutorial.model.User;
import org.kurento.tutorial.model.UserForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {
	
	private EmailValidator emailValidator= EmailValidator.getInstance();
	
	@Autowired
	UserDao userDao;

	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return clazz== UserForm.class;
	}

	@Override
	public void validate(Object target, Errors errors) {
		UserForm userForm= (UserForm) target;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors,"email", "NotEmpty.UserForm.email");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty.UserForm.password");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "passwordConfirm", "NotEmpty.UserForm.confirmPassword");
		
		if(!errors.hasErrors())
		{
			if(!this.emailValidator.isValid(userForm.getEmail())) {
				errors.rejectValue("email", "Pattern.UserForm.email");
			}
			else {
				User user= null;
				try {
					user= userDao.findUserByEmail(userForm.getEmail());
				} catch (Exception e) {
					// TODO: handle exception
				}
				if(user!=null) {
					errors.rejectValue("email","Duplicate.UserForm.email");
				}
			}
		}
		if (!errors.hasErrors()) {
			if (!userForm.getPasswordConfirm().equals(userForm.getPassword()))
				errors.rejectValue("passwordConfirm", "Match.userForm.confirmPassword");
		}
	}
}