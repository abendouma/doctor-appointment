package com.angelspeech.service.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
@Entity
@Table(name="doctors")
public class Doctor {
	@Id
    @GeneratedValue
    @Column(name="doctorId")
	public Long doctorId;
	
    @Column(name="username")
	public String username;

    @Column(name="password")
	public String password;
	
    @Column(name="firstName")
    public String firstName;

    @Column(name="lastName")
	public String lastName;
    
    @Column(name="email")
	public String email;
    
	public Doctor() {
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Long doctorId) {
		this.doctorId = doctorId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
