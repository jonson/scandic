package com.dajodi.scandic.user;

public class UsernamePassword {

	private final String username;
	private final String password;
	
	public UsernamePassword(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
