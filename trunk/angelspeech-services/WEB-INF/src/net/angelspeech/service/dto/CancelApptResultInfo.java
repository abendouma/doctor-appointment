package net.angelspeech.service.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
@XmlRootElement
public class CancelApptResultInfo {
	public int code;
	public String notice;
	public String doctorId;
	public String apptProfileId;
	
	public CancelApptResultInfo(int code, String notice, String doctorId, String apptProfileId) {
		this.code = code;
		this.notice = notice;
		this.doctorId = doctorId;
		this.apptProfileId = apptProfileId;
	}

	public CancelApptResultInfo() {
	}
}
