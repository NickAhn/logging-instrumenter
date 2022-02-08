package com.springboot.microservice.example.authentication;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "authorization-service", url="localhost:8060")
public interface AuthorizationServiceProxy {
	
	@GetMapping("/access/{username}/to/{service}")
	public boolean haveAccess(@PathVariable String username, 
			@PathVariable String service);
	
	@GetMapping("/btg/for/{username}")
	public boolean breakTheGlass(@PathVariable String username);
	
	@GetMapping("/mtg/for/{username}")
	public boolean mendTheGlass(@PathVariable String username);
	
	//we may not need this one! seems inappropriate!
	@GetMapping("/btg/users")
	public String getBTGUsers();
	
	@GetMapping("/btg/check/{username}")
	public boolean isBTGUser(@PathVariable String username);

}
