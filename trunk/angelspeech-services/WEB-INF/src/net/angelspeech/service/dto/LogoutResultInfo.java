package net.angelspeech.service.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
@XmlRootElement
public class LogoutResultInfo {
	public int code;
	public String notice;
	
	public LogoutResultInfo(int code, String notice) {
		this.code = code;
		this.notice = notice;
	}

	public LogoutResultInfo() {
	}

}
