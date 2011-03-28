package com.angelspeech.service.dao;

import com.angelspeech.service.core.Doctor;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
public interface DoctorDAO extends GenericDAO<Doctor, Long> {
	public Doctor authorize(String username, String password);
	public boolean isExisted(String username);
}
