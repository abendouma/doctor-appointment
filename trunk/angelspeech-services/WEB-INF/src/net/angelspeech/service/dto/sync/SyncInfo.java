package net.angelspeech.service.dto.sync;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Quang
 *
 */
@XmlRootElement
public class SyncInfo {
	public int errorCode;
	
	public String errorMessage;
	
	public DoctorSyncInfo doctor;
	
	public List<LocationSyncInfo> locationSyncInfo;
	
	public List<AppointmentProfileSyncInfo> appointmentProfileSyncInfo;

	public ScheduleSyncInfo scheduleSyncInfo;

	public PatientItemSyncInfo patientItems;
	
	public SyncInfo(int errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
	
	public SyncInfo() {
	}
}
