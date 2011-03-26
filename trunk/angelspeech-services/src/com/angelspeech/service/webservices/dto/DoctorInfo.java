package com.angelspeech.service.webservices.dto;

import javax.xml.bind.annotation.XmlRootElement;

import com.angelspeech.service.core.Doctor;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
@XmlRootElement
public class DoctorInfo {
	
	public Long doctorId;
	public String username;
	public String password;

	public String firstName;
	public String lastName;
	public String email;

	public DoctorInfo(Doctor doctor) {
		this.doctorId = doctor.getDoctorId();
		this.username = doctor.getUsername();
		this.firstName = doctor.getFirstName();
		this.lastName = doctor.getLastName();
		this.email = doctor.getEmail();
	}
	
	public DoctorInfo() {
	}
}
