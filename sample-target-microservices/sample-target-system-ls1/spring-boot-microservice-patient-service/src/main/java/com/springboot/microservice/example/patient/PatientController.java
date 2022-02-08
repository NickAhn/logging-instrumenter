package com.springboot.microservice.example.patient;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PatientController {
	
	@Autowired
	private Environment env;
	
	@Autowired
	private PatientRepository repo;
	
	@GetMapping("/{username}/patient/id/{id}")
	public Patient getPatientMedHistByID(@PathVariable String username, 
			@PathVariable Long id) {
		Optional<Patient> patient = repo.findById(id);
		if (patient.isPresent()) {
			patient.get().setPort(Integer.parseInt(
					env.getRequiredProperty("local.server.port")));
			return patient.get();
		}
		return null; 
	}
	
	@GetMapping("/{username}/patient/name/{name}")
	public Patient getPatientMedHistByName(@PathVariable String username, 
			@PathVariable String name) {
		Patient patient = repo.findByName(name);
		if (patient != null) {
			patient.setPort(Integer.parseInt(
					env.getRequiredProperty("local.server.port")));
		}
		return patient; 
	}
	
	@GetMapping("/{username}/disease/{disease}")
	public List<Patient> getAllPatientsWithDisease(@PathVariable String username, 
			@PathVariable String disease) {
		List<Patient> patients = repo.findAllByDisease(disease);
		for (Patient patient : patients) {
			patient.setPort(Integer.parseInt(
					env.getRequiredProperty("local.server.port")));
		}
		return patients;
	}
	
	@GetMapping("/{username}/patient")
	public List<Patient> getAllPatients(@PathVariable String username) {
		List<Patient> patients = repo.findAll();
		for (Patient patient : patients) {
			patient.setPort(Integer.parseInt(
					env.getRequiredProperty("local.server.port")));
		}
		return patients;
	}
	
	

}
