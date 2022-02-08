package com.springboot.microservice.example.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationServiceController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AuthorizationServiceProxy proxy;
	
	@GetMapping("/authorization/access/{username}/to/{service}")
	public boolean checkAccess(@PathVariable("username") String username, 
			@PathVariable("service") String service) {
		boolean resp = proxy.haveAccess(username, service);
		logger.info("Response from authorization-service: " + resp);
		return resp;
	}
	
	@GetMapping("/authorization/btg/for/{username}")
	public boolean brkGlass(@PathVariable("username") String username) {
		boolean resp = proxy.breakTheGlass(username);
		logger.info("Response from authorization-service: " + resp);
		return resp;
	}
	
	@GetMapping("/authorization/mtg/for/{username}")
	public boolean mndGlass(@PathVariable("username") String username) {
		boolean resp = proxy.mendTheGlass(username);
		logger.info("Response from authorization-service: " + resp);
		return resp;
	}
	
	@GetMapping("/authorization/btg/users")
	public String retrieveBTGUsers() {
		String resp = proxy.getBTGUsers();
		logger.info("Response from patient-service: " + resp);
		return resp;
	}
	
	@GetMapping("/authorization/btg/check/{username}")
	public boolean checkIfBTGUser(@PathVariable String username) {
		boolean resp = proxy.isBTGUser(username);
		logger.info("Response from authorization-service: " + resp);
		return resp;
	}
}
