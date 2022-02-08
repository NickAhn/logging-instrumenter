package com.springboot.microservice.example.patient;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class Patient {
	
	@Id
	private Long id;
	
	private String name;
	
	private String disease;
	
	private int port;

	// For JPA
	public Patient() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Patient(Long id, String name, String disease) {
		super();
		this.id = id;
		this.name = name;
		this.disease = disease;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDisease() {
		return disease;
	}

	@Override
	public String toString() {
		return "Patient [id=" + id + ", name=" + name + 
				", disease=" + disease + ", port=" + port + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((disease == null) ? 0 : disease.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Patient other = (Patient) obj;
		if (disease == null) {
			if (other.disease != null)
				return false;
		} else if (!disease.equals(other.disease))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

}
