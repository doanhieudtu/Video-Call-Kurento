package org.kurento.tutorial.service;

import java.util.List;

import org.kurento.tutorial.model.User;

public interface  UserService {
	 User save(User user) ;
	 List<User> findAll();
	 void delete(User uder);
	 User findByEmail(String email);
	 User findByTokenAccess(String token);
}
