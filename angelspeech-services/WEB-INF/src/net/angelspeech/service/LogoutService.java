package net.angelspeech.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.angelspeech.object.RESTfulServiceHelper;
import net.angelspeech.service.dto.LogoutResultInfo;
import net.angelspeech.util.MessageResourcesManager;

import org.apache.log4j.Logger;

/**
 * @author Quang mailto:quangnguyen111@gmail.com
 */
@Path("/logout")
public class LogoutService extends WebServiceSupport {
	static private Logger logger = Logger.getLogger(LogoutService.class);
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/xml")
	public LogoutResultInfo doctorLogout(@FormParam("doctorId") String doctorId, @FormParam("restkey") String restkey) throws Exception {
		this.messageResources = new MessageResourcesManager(context.getRealPath("/"));
		
		if(restkey != null && RESTfulServiceHelper.isRESTKeyValid(doctorId, restkey)) {
			logger.info("Clean-up REST key");
			RESTfulServiceHelper.updateRESTkey(doctorId, "");
		}
		
		logger.info("Logout success");
		return new LogoutResultInfo(200, this.messageResources.getValue("success.logout"));
	}
}
