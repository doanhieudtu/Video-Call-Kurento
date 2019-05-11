package org.kurento.tutorial.dao;

import org.kurento.tutorial.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends CrudRepository<User, Long>{
	
	@Query("SELECT p FROM User p  WHERE p.email = :email")
	public User findUserByEmail(@Param("email") String email);
}
