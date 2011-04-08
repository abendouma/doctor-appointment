package net.angelspeech.service.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
@XmlRootElement
public class ResultNoticeInfo {
	public String notice;
	
	public ResultNoticeInfo(String notice) {
		this.notice = notice;
	}
	
	public ResultNoticeInfo() {
	}
}
