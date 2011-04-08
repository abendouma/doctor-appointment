package net.angelspeech.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import net.angelspeech.database.DoctorRecord;
import net.angelspeech.object.CryptoHelper;
import net.angelspeech.service.dto.DoctorInfo;

/**
 * @author Quang mailto:quangnguyen111@gmail.com
 */
@Path("/login")
public class LoginService {

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/xml")
	public DoctorInfo doctorLogin(@FormParam("username") String username, @FormParam("password") String password) {
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
				result = new DoctorInfo(doctor);
			}
			else if(doctor.readByFilter(new String[][] {
					{ "username", username } })) {
				result = new DoctorInfo(201);
			}
			
			else {
				result = new DoctorInfo(202); // User not found
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			result = new DoctorInfo(500); // Server error
		}

		return result;
	}
}
