package com.springboot.microservice.example.authentication;

import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
	public boolean authenticate(String username, String password) {
		if ((username.equals("admin")) && (password.equals("pass")))
			return true;
		if ((username.equals("dr.max")) && (password.equals("pass")))
			return true;
		if ((username.equals("user")) && (password.equals("pass")))
			return true;
		return false;
	}
}
