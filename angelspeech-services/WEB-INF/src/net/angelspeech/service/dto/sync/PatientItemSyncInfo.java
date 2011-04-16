package net.angelspeech.service.dto.sync;

import java.util.List;

/**
 * @author Quang
 *
 */
public class PatientItemSyncInfo {
	public int doctorId;
	public List<PatientSyncInfo> patients;
	
	public PatientItemSyncInfo() {
	}

	public PatientItemSyncInfo(int doctorId, List<PatientSyncInfo> patients) {
		this.doctorId = doctorId;
		this.patients = patients;
	}
}
