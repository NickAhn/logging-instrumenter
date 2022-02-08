package com.springboot.microservice.example.authentication;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PatientServiceController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private PatientServiceProxy proxy;
	
	@GetMapping("/{username}/medical-history/id/{id}")
	public String retrievePatientByID(@PathVariable("username") String username,
			@PathVariable("id") Long id) {
		String resp = proxy.getPatientMedHistByID(username, id);
		logger.info("Response from patient-service: " + resp);
		return resp;
	}
	
	@GetMapping("/{username}/medical-history/name/{name}")
	public String retrievePatientByName(@PathVariable("username") String username,
			@PathVariable("name") String name) {
		String resp = proxy.getPatientMedHistByName(username, name);
		logger.info("Response from patient-service: " + resp);
		return resp;
	}
	
	@GetMapping("/{username}/medical-history/disease/{disease}")
	public String retrieveAllPatientsWithDisease(
			@PathVariable("username") String username,
			@PathVariable("disease") String disease) {
		String resp = proxy.getAllPatientsWithDisease(username, disease);
		logger.info("Response from patient-service: " + resp);
		return resp;
	}
	
	@GetMapping("/{username}/medical-history/patients")
	public String retrieveAllPatients(
			@PathVariable("username") String username) {
		String resp = proxy.getAllPatients(username);
		logger.info("Response from patient-service: " + resp);
		return resp;
	}
	
	

}
