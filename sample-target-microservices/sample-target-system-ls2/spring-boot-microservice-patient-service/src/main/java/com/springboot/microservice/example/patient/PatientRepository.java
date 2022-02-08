package com.springboot.microservice.example.patient;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
	public Patient findByName(String name);
	public List<Patient> findAllByDisease(String disease);
}
