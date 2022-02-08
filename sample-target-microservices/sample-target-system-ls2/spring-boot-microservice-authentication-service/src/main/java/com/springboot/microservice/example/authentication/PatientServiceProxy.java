package com.springboot.microservice.example.authentication;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "patient-service", url="localhost:8050")
public interface PatientServiceProxy {
	
	@GetMapping("/{username}/patient/id/{id}")
	public String getPatientMedHistByID(@PathVariable("username") String username,
			@PathVariable("id") Long id);
	
	@GetMapping("/{username}/patient/name/{name}")
	public String getPatientMedHistByName(@PathVariable("username") String username, 
			@PathVariable("name") String name);
	
	@GetMapping("/{username}/disease/{disease}")
	public String getAllPatientsWithDisease(
			@PathVariable("username") String username,
			@PathVariable("disease") String disease);
	
	@GetMapping("/{username}/patient")
	public String getAllPatients(@PathVariable("username") String username);
}
