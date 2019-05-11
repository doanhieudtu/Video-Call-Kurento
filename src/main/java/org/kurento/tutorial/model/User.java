package org.kurento.tutorial.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="USER")
public class User {
	
	@Id
	@GeneratedValue
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "EMAIL", nullable = false)
	private String email;
	
	@Column(name = "PASSWORD", nullable = false)
	private String password;

	public String getEmail() {
		return email;
	}
	
	@Column(name="role")	
	private String role;
	
	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	
}
