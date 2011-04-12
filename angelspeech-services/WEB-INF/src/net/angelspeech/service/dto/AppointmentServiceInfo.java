package net.angelspeech.service.dto;

import javax.xml.bind.annotation.XmlRootElement;

import net.angelspeech.object.AppointmentInfo;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
@XmlRootElement
public class AppointmentServiceInfo {
	public int code;
	public String notice;
	
	public String doctorId;
	public String epochDay;
	public String startSlot;
	public String apptProfileId;
	public String patientId;
	public String notes;
	public String apptId;
	
	public AppointmentServiceInfo(int code, String notice) {
		this.code = code;
		this.notice = notice;
	}

	
	public AppointmentServiceInfo(int code, String notice, String doctorId,
			String epochDay, String startSlot, String apptProfileId) {
		this.code = code;
		this.notice = notice;
		this.doctorId = doctorId;
		this.epochDay = epochDay;
		this.startSlot = startSlot;
		this.apptProfileId = apptProfileId;
	}

	public AppointmentServiceInfo(int code, String notice, String doctorId,
			String apptProfileId) {
		this.code = code;
		this.notice = notice;
		this.doctorId = doctorId;
		this.apptProfileId = apptProfileId;
	}

	public AppointmentServiceInfo(int code, String notice, String doctorId,
			String epochDay, String startSlot, String apptProfileId,
			String patientId, String apptId, String notes) {
		this.code = code;
		this.notice = notice;
		this.doctorId = doctorId;
		this.epochDay = epochDay;
		this.startSlot = startSlot;
		this.apptProfileId = apptProfileId;
		this.patientId = patientId;
		this.notes = notes;
		this.apptId = apptId;
	}

	public AppointmentServiceInfo() {
	}
}
