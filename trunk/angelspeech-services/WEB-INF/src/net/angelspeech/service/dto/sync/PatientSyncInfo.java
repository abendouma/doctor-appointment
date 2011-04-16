package net.angelspeech.service.dto.sync;

/**
 * @author Quang
 *
 */
public class PatientSyncInfo {
	public int patientId;
	public String firstName;
	public String lastName;
	public String phone;
	public String email;
	
	public PatientSyncInfo() {
	}

	public PatientSyncInfo(int patientId, String firstName, String lastName,
			String phone, String email) {
		this.patientId = patientId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.email = email;
	}
	
}
