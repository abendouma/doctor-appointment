package com.angelspeech.service.webservices;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.hibernate.Session;

import com.angelspeech.service.core.Doctor;
import com.angelspeech.service.dao.DAOFactory;
import com.angelspeech.service.dao.DoctorDAO;
import com.angelspeech.service.hibernate.HibernateDAOFactory;
import com.angelspeech.service.webservices.dto.DoctorInfo;
import com.angelspeed.service.util.HibernateUtil;

/**
 * @author Quang 
 * mailto:quangnguyen111@gmail.com
 */
@Path("/login")
public class LoginService {
	
	@POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/xml")
    public DoctorInfo doctorLogin(@FormParam("username") String username, @FormParam("password") String password) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		DAOFactory fac = DAOFactory.instance(HibernateDAOFactory.class);
		DoctorDAO doctorDAO = fac.getDoctorDAO();
		Doctor doctor = doctorDAO.authorize(username, password);
		
		boolean isExisted = doctorDAO.isExisted(username);
		session.getTransaction().commit();

		if(doctor != null) {
			return new DoctorInfo(doctor);
		}
		else if(isExisted) {
			return new DoctorInfo(201);//wrong password
		}
		else {
			return new DoctorInfo(202); //User not found
		}
	}
}
