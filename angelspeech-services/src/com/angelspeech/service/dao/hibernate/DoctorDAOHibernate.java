package com.angelspeech.service.dao.hibernate;

import java.util.List;

import net.angelspeech.object.CryptoHelper;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.angelspeech.service.core.Doctor;
import com.angelspeech.service.dao.DoctorDAO;
import com.angelspeech.service.hibernate.GenericHibernateDAO;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
public class DoctorDAOHibernate extends GenericHibernateDAO<Doctor, Long> implements DoctorDAO {
	public Doctor authorize(String username, String password) {
		String encryptedPassword = "";
		try {
			encryptedPassword = CryptoHelper.encrypt(password);
		} catch (Exception e) {
			encryptedPassword = "";
		}
		Criteria crit = getSession().createCriteria(Doctor.class);
		crit.add(Restrictions.eq("username", username));
		crit.add(Restrictions.eq("password", encryptedPassword));
		List<Doctor> returnList = crit.list();
		if(returnList.size() == 1) return returnList.get(0);
		else return null;
	}
}
