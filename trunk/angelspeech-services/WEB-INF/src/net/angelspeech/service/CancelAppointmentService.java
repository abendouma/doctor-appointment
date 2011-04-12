package net.angelspeech.service;

import javax.servlet.ServletContext;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.angelspeech.object.AppointmentInfo;
import net.angelspeech.object.MessagesInlineService;
import net.angelspeech.object.NotifyHelperService;
import net.angelspeech.object.RESTfulServiceHelper;
import net.angelspeech.object.TimeHelper;
import net.angelspeech.service.dto.AppointmentServiceInfo;
import net.angelspeech.service.dto.CancelApptResultInfo;
import net.angelspeech.util.MessageResourcesManager;

import org.apache.log4j.Logger;

/**
 * @author Quang mailto:quangnguyen111@gmail.com
 */
@Path("/cancelAppt")
public class CancelAppointmentService extends WebServiceSupport {
	
	static private Logger logger = Logger.getLogger(CancelAppointmentService.class);
	
	@POST
	@Produces("application/xml")
	public CancelApptResultInfo cancelAppointment(@FormParam("doctorId") String doctorId,
			@FormParam("apptProfileId") String apptProfileId,
			@FormParam("apptId") String apptId,
			@FormParam("restkey") String restkey) throws Exception {
		
		this.messageResources = new MessageResourcesManager(context.getRealPath("/"));

		MessagesInlineService messages = new MessagesInlineService();
		AppointmentInfo appointmentInfo = new AppointmentInfo();
		
		if (apptId == null) {
			logger.info("Logout on addApptCancelAppt caused by missing apptId");
			return new CancelApptResultInfo(201, this.messageResources.getValue("error.appId.is.null"),
					appointmentInfo.doctorId, apptProfileId.toString());
		}
		if (apptProfileId == null) {
			logger.info("Logout on addApptCancelAppt caused by missing apptProfileId");
			logger.info("apptId is " + apptId);
			return new CancelApptResultInfo(201, this.messageResources.getValue("error.appId.is.null"),
					appointmentInfo.doctorId, apptProfileId.toString());
		}
		if (appointmentInfo.read(apptId) == false) {
			logger.info("Logout on addApptCancelAppt caused by failure reading appointmentInfo record ");
			logger.info("apptId is " + apptId);
			return new CancelApptResultInfo(201,
					this.messageResources.getValue("error.appt.not.existed"),
					appointmentInfo.doctorId, apptProfileId.toString());
		}
		if (doctorId == null || doctorId.equals("")) {
			logger.info("Logout on addApptCancelAppt caused by checkAccess failure ");
			logger.info("apptId is " + apptId);
			return new CancelApptResultInfo(201, this.messageResources.getValue("error.login"),
					appointmentInfo.doctorId, apptProfileId.toString());
		}
		if (restkey == null) {
			logger.info("REST key is null");
			return new CancelApptResultInfo(201, this.messageResources.getValue("error.login"),
					appointmentInfo.doctorId, apptProfileId.toString());
		}
		
		if (!RESTfulServiceHelper.isRESTKeyValid(doctorId, restkey)) {
			logger.info("Invalid Rest key");
			return new CancelApptResultInfo(201, this.messageResources.getValue("error.login"),
					appointmentInfo.doctorId, apptProfileId.toString());
		}
		if (appointmentInfo.epochDay < TimeHelper.currentEpochDay()) {
			messages.addGenericMessage("error.failed.apptCancel");
			return new CancelApptResultInfo(201,
					this.messageResources.getValue("error.failed.apptCancel"),
					appointmentInfo.doctorId, apptProfileId.toString());
		}
		if (AppointmentInfo.destroy(apptId) == false) {
			messages.addGenericMessage("error.failed.apptCancel");
			return new CancelApptResultInfo(201,
					this.messageResources.getValue("error.failed.apptCancel"),
					appointmentInfo.doctorId, apptProfileId.toString());
		}
		messages.setSchedule(appointmentInfo.doctorId,
				appointmentInfo.epochDay, null);
		messages.addGenericMessage("success.apptCancel");
		NotifyHelperService.appointmentCancel(appointmentInfo, messages);
		return new CancelApptResultInfo(200, this.messageResources.getValue("success.apptCancel"),
				doctorId, apptProfileId);
	}
}
