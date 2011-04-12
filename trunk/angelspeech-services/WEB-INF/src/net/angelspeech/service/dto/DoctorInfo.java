package net.angelspeech.service.dto;

import javax.xml.bind.annotation.XmlRootElement;

import net.angelspeech.database.DoctorRecord;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
@XmlRootElement
public class DoctorInfo {
	
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
	public String RESTKey;

	public DoctorInfo(DoctorRecord doctor, String errorMessage) {
		this.doctorId = doctor.doctorId;
		this.username = doctor.username;
		this.firstName = doctor.firstName;
		this.lastName = doctor.lastName;
		this.email = doctor.email;
		this.errorCode = 200;
		this.errorMessage = errorMessage;
		this.RESTKey = doctor.RESTkey;
	}
	
	public DoctorInfo(int errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public DoctorInfo() {
	}
}
