package com.angelspeech.service.hibernate;

import com.angelspeech.service.dao.DAOFactory;
import com.angelspeech.service.dao.DoctorDAO;
import com.angelspeech.service.dao.hibernate.DoctorDAOHibernate;



/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
public class HibernateDAOFactory extends DAOFactory{

	@Override
	public DoctorDAO getDoctorDAO() {
		return new DoctorDAOHibernate();
	}

//	@Override
//	public DoctorDAO getDoctorDAO() {
//		return new DoctorDAOHibernate();
//	}
}