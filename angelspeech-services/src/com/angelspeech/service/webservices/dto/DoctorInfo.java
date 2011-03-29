package com.angelspeech.service.webservices.dto;

import javax.xml.bind.annotation.XmlRootElement;

import com.angelspeech.service.core.Doctor;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
@XmlRootElement
public class DoctorInfo {
	private static final String LOGIN_SUCCESS = "Login successful";
	private static final String WRONG_PASSWORD = "Wrong password";
	private static final String USERNAME_NOT_FOUND = "Username not found";
	private static final String SERVER_ERROR = "Server error";
	
	
	/**
	 * Error code
	 * 200: success
	 * 201: wrong password
	 * 202: wrong username (user not found)
	 * 
	 */
	public int errorCode;
	public String errorMessage;
	
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
		this.errorCode = 200;
		this.errorMessage = LOGIN_SUCCESS;
	}
	
	public DoctorInfo(int errorCode) {
		this.errorCode = errorCode;
		switch (errorCode) {
		case 201:
			this.errorMessage = WRONG_PASSWORD;
			break;

		case 202:
			this.errorMessage = USERNAME_NOT_FOUND;
			break;

		case 500:
			this.errorMessage = SERVER_ERROR;
			break;

		default:
			break;
		}
	}

	public DoctorInfo() {
	}
}
