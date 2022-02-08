package com.springboot.microservice.example.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("username")
public class AuthenticationController {

		@Autowired
		AuthenticationService authenticationService;
		
		@Autowired
		AuthorizationServiceController authorizationController;
		
		@RequestMapping(value = "/auth", method = RequestMethod.GET)
		public String showAuthenticationPage(ModelMap modelMap) {
			return "auth";
		}
		
		@RequestMapping(value = "/auth", method = RequestMethod.POST)
		public String showAuthenticationPage(ModelMap modelMap,
				@RequestParam String username, @RequestParam String password) {
			if (authenticationService.authenticate(username, password)) {
				modelMap.put("username", username);
				modelMap.put("password", password);
				modelMap.put("access", 
						authorizationController.checkAccess(
								username, "patient-service"));
				modelMap.put("btg", authorizationController.checkIfBTGUser(username));
				return "portal";
			}
			modelMap.put("error", "Invalid username and/or password.");
			return "auth";
		}
		
		
}
