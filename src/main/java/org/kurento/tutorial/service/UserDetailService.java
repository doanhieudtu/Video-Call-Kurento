package org.kurento.tutorial.service;

import java.util.Arrays;

import org.kurento.tutorial.dao.UserDao;
import org.kurento.tutorial.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailService implements UserDetailsService {

	@Autowired
	UserDao userDao;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User activeUserInfo = userDao.findUserByEmail(email);
		GrantedAuthority authority = new SimpleGrantedAuthority(activeUserInfo.getRole());
		UserDetails userDetails = (UserDetails)new org.springframework.security.core.userdetails.User(activeUserInfo.getEmail(),
				activeUserInfo.getPassword(), Arrays.asList(authority));
		return userDetails;
	}
	
	
	
}
