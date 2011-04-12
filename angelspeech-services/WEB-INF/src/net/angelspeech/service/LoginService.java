package net.angelspeech.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;

import net.angelspeech.database.DoctorRecord;
import net.angelspeech.object.CryptoHelper;
import net.angelspeech.object.RESTfulServiceHelper;
import net.angelspeech.service.dto.DoctorInfo;
import net.angelspeech.util.MessageResourcesManager;

/**
 * @author Quang mailto:quangnguyen111@gmail.com
 */
@Path("/login")
public class LoginService extends WebServiceSupport {
	static private Logger logger = Logger.getLogger(LoginService.class);
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/xml")
	public DoctorInfo doctorLogin(@FormParam("username") String username, 
									@FormParam("password") String password) {
		this.messageResources = new MessageResourcesManager(context.getRealPath("/"));
		String encryptedPassword = "";
		try {
			encryptedPassword = CryptoHelper.encrypt(password);
		} catch (Exception e) {
			encryptedPassword = "";
		}
		
		DoctorInfo result;

		try {
			
			DoctorRecord doctor = new DoctorRecord();
			
			if(doctor.readByFilter(new String[][] {
					{ "username", username },
					{ "password", encryptedPassword } })) {
				
				RESTfulServiceHelper.updateRESTkey(doctor.doctorId, 
													RESTfulServiceHelper.create32DigitKey());
				doctor.readById(doctor.doctorId);
				
				logger.info("Login success");
				result = new DoctorInfo(doctor, messageResources.getValue("success.login"));
			}
			else if(doctor.readByFilter(new String[][] {
					{ "username", username } })) {
				logger.info("Login failed");
				result = new DoctorInfo(201, messageResources.getValue("error.failed.login"));
			}
			
			else {
				logger.info("User not found");
				result = new DoctorInfo(202, messageResources.getValue("error.failed.loginPatientRedirect")); 
			}
		} catch (Exception e) {
			logger.info("System error" + e.toString());
			result = new DoctorInfo(500, messageResources.getValue("error.system"));
		}

		return result;
	}
}
