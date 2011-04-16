package net.angelspeech.service;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.angelspeech.object.PeriodicUpdate;
import net.angelspeech.object.RESTfulServiceHelper;
import net.angelspeech.service.dto.sync.SyncInfo;
import net.angelspeech.util.MessageResourcesManager;

import org.apache.log4j.Logger;

/**
 * @author Quang mailto:quangnguyen111@gmail.com
 */
@Path("/periodicSync")
public class PeriodicSyncService extends WebServiceSupport {
	
	static private Logger logger = Logger.getLogger(PeriodicSyncService.class);
	
	@POST
	@Produces("application/xml")
	public SyncInfo cancelAppointment(@FormParam("doctorId") String doctorId,
			@FormParam("restkey") String restkey) throws Exception {
		
		this.messageResources = new MessageResourcesManager(context.getRealPath("/"));

		if (doctorId == null || doctorId.equals("")) {
			logger.info("doctorId is null");
			return new SyncInfo(201, this.messageResources.getValue("error.periodic.sync"));
		}
		if (restkey == null) {
			logger.info("REST key is null");
			return new SyncInfo(201, this.messageResources.getValue("error.periodic.sync"));
		}
		
		if (!RESTfulServiceHelper.isRESTKeyValid(doctorId, restkey)) {
			logger.info("Invalid Rest key");
			return new SyncInfo(201, this.messageResources.getValue("error.periodic.sync"));
		}
		
		SyncInfo syncInfo = PeriodicUpdate.iphoneSynch(doctorId);
		if(syncInfo == null) {
			logger.info("System error when periodic sync");
			return new SyncInfo(201, this.messageResources.getValue("error.periodic.sync"));
		}
		else {
			syncInfo.errorCode = 200;
			syncInfo.errorMessage = this.messageResources.getValue("success.periodic.sync");
			return syncInfo;
		}
	}
}
