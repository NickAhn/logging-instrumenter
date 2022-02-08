package com.springboot.microservice.example.authorization;


import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationController {
	
	List<String> btgUsers = new ArrayList<String>();
	
	@GetMapping("/access/{username}/to/{service}")
	public boolean haveAccess(@PathVariable String username, 
			@PathVariable String service) {
		if (username.equals("admin")) 
			return true;
		if (username.equals("dr.max") && service.equals("patient-service")) 
			return true;
		return false;
	}
	
	@GetMapping("/btg/for/{username}")
	public boolean breakTheGlass(@PathVariable String username) {
		if (!btgUsers.contains(username))
			btgUsers.add(username);
		return true;
	}
	
	@GetMapping("/mtg/for/{username}")
	public boolean mendTheGlass(@PathVariable String username) {
		btgUsers.remove(username);
		return true;
	}
	
	@GetMapping("/btg/users")
	public List<String> getBTGUsers() {
		return btgUsers;
	}
	
	@GetMapping("/btg/check/{username}")
	public boolean isBTGUser(@PathVariable String username) {
		return (btgUsers.contains(username));
	}

}
