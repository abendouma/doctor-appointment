package net.angelspeech.service.dto;

import javax.xml.bind.annotation.XmlRootElement;

import net.angelspeech.database.DoctorRecord;

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
	
	public String doctorId;
	public String username;
	public String password;

	public String firstName;
	public String lastName;
	public String email;

	public DoctorInfo(DoctorRecord doctor) {
		this.doctorId = doctor.doctorId;
		this.username = doctor.username;
		this.firstName = doctor.firstName;
		this.lastName = doctor.lastName;
		this.email = doctor.email;
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
