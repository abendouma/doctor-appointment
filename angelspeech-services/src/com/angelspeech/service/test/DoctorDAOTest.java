package com.angelspeech.service.test;

import java.util.List;

import org.hibernate.Session;

import com.angelspeech.service.core.Doctor;
import com.angelspeech.service.dao.DAOFactory;
import com.angelspeech.service.dao.DoctorDAO;
import com.angelspeech.service.hibernate.HibernateDAOFactory;
import com.angelspeech.service.util.HibernateUtil;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
public class DoctorDAOTest {
	public static void main(String[] args) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		DAOFactory fac = DAOFactory.instance(HibernateDAOFactory.class);
		DoctorDAO doctorDAO = fac.getDoctorDAO();
		
		List<Doctor> allDoctor = doctorDAO.findAll();
		
		System.out.println(allDoctor.size());
		
		String findingUsername = "dr.andrew@angelspeech.com";
		String findingPassword = "123456";
		
		Doctor doctor = doctorDAO.authorize(findingUsername, findingPassword);
		
		System.out.println(doctor.getUsername());
		
		session.getTransaction().commit();
	}
}
