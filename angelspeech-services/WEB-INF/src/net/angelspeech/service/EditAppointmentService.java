package net.angelspeech.service;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import net.angelspeech.database.ApptProfileRecord;
import net.angelspeech.database.DoctorRecord;
import net.angelspeech.database.PatientRecord;
import net.angelspeech.object.AppointmentInfo;
import net.angelspeech.object.ApptSearchHelper;
import net.angelspeech.object.MessagesInlineService;
import net.angelspeech.object.NotifyHelperService;
import net.angelspeech.object.RESTfulServiceHelper;
import net.angelspeech.object.TimeHelper;
import net.angelspeech.service.dto.AppointmentServiceInfo;
import net.angelspeech.util.AngelspeedUtils;
import net.angelspeech.util.MessageResourcesManager;

import org.apache.log4j.Logger;

/**
 * @author Quang mailto:quangnguyen111@gmail.com
 */
@Path("/editappt")
public class EditAppointmentService extends WebServiceSupport {
	static private Logger logger = Logger.getLogger(EditAppointmentService.class);
	private static final String DURATION = "15";

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/xml")
	public AppointmentServiceInfo editAppointment(@FormParam("doctorId") String doctorId, 
										@FormParam("note") String note,
										@FormParam("date") String date,
										@FormParam("hour") String hour,
										@FormParam("minute") String minute,
										@FormParam("apptProfileId") String apptProfileId,
										@FormParam("apptId") String apptId,
										@FormParam("notifyByPhone") String notifyByPhone,
										@FormParam("useCustomizeAppt") String useCustomizeAppt,
										@FormParam("restkey") String restkey) throws Exception {
		
		String duration = DURATION;
		
		this.messageResources = new MessageResourcesManager(context.getRealPath("/"));
		
//		DynaValidatorForm aForm;
		DoctorRecord doctorRecord = new DoctorRecord ();
		AppointmentInfo appointmentInfo = new AppointmentInfo ();
		MessagesInlineService messages = new MessagesInlineService();
		int oldDay, oldRangeStart, newDay, currentDay;

		
		if (restkey == null) {
			logger.info("REST key is null");
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.login"));
		}
		
		if (!RESTfulServiceHelper.isRESTKeyValid(doctorId, restkey)) {
			logger.info("Invalid Rest key");
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.login"));
		}

//		HttpHelper.disableCache (response);
		if (apptId == null) {
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.appId.is.null"));
		}
		if (appointmentInfo.read (apptId) == false) {
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.appt.not.existed"));
		}
		
//		aForm = (DynaValidatorForm) form;
//		messages.addActionMessages (aForm.validate (mapping, request));

		//Set attribute before forwarding in different points
//		request.setAttribute ("askConfirm", "false");
//		request.setAttribute ("doctorId", appointmentInfo.doctorId);
//		request.setAttribute ("apptId", apptId);

		if (messages.isEmptyDisplay () == false) {
//			messages.save ();
			//request.setAttribute ("doctorId", appointmentInfo.doctorId);
			//request.setAttribute ("apptId", apptId);
//			return (mapping.findForward ("failure"));
			return new AppointmentServiceInfo(201, "failure: message not display");
		}
		oldDay = appointmentInfo.epochDay;
		oldRangeStart = appointmentInfo.rangeStart;
		newDay = AngelspeedUtils.getStartDay (date);
		// if date is in bad format
		if (newDay == -1){
			messages.addGenericMessage ("error.invalid.startDate");
//			messages.save ();
			//request.setAttribute ("doctorId", appointmentInfo.doctorId);
			//request.setAttribute ("apptId", apptId);
//			return (mapping.findForward ("failure"));
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.invalid.startDate"));
		}
		currentDay = TimeHelper.currentEpochDay ();
		if ((oldDay < currentDay) || (newDay < currentDay)) {
			messages.addGenericMessage ("error.failed.apptEdit");
//			messages.save ();
			//request.setAttribute ("doctorId", appointmentInfo.doctorId);
			//request.setAttribute ("apptId", apptId);
//			return (mapping.findForward ("failure"));
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.failed.apptEdit"));
		}
		// update appt record
		doctorRecord.readById (appointmentInfo.doctorId);

		//save input data from reschedule page.
		appointmentInfo.notes = note;
		appointmentInfo.epochDay = newDay;
		appointmentInfo.rangeStart =
			Integer.parseInt (hour) * 3600 +
			Integer.parseInt (minute) * 60;
		
		if (setProfileAndRangeEnd (
			appointmentInfo,
			doctorRecord,
			messages, useCustomizeAppt, apptProfileId, minute, duration
		) == false) {
//			messages.save ();
			//request.setAttribute ("doctorId", appointmentInfo.doctorId);
			//request.setAttribute ("apptId", apptId);
//			return (mapping.findForward ("failure"));
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.failed.apptEdit"));
		}
		boolean isApptTimeChanged = (oldDay != newDay)||(oldRangeStart != appointmentInfo.rangeStart);
		if (isApptTimeChanged) {
			appointmentInfo.remSentPhone = false;
			appointmentInfo.remSentEmail = false;
			appointmentInfo.requireSIU = doctorRecord.hasHL7Channels.equals ("1");
			if ((doctorRecord.hasGoogleSync.equals ("1"))||(doctorRecord.hasGoogleSync.equals ("2"))){
				appointmentInfo.requireGsync = AppointmentInfo.GSYNC_TO_GOOGLE;
			}
			PatientRecord patientRecord = new PatientRecord ();
			patientRecord.readById (appointmentInfo.patientId);
			appointmentInfo.requireSMS = (patientRecord.isSMSActive())? "1" : "0";
		}
		if (appointmentInfo.update (false) == false) {
			messages.addGenericMessage ("error.failed.apptEdit");
//			messages.save ();
			//request.setAttribute ("doctorId", appointmentInfo.doctorId);
			//request.setAttribute ("apptId", apptId);
//			return (mapping.findForward ("failure"));
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.failed.apptEdit"));
		}
		messages.setSchedule (appointmentInfo.doctorId, oldDay, null);
		if (oldDay != newDay) {
			messages.setSchedule (appointmentInfo.doctorId, newDay, null);
		}
		messages.addGenericMessage ("success.apptEdit");
		if (isApptTimeChanged) {
			NotifyHelperService.appointmentEdit (oldDay, oldRangeStart, appointmentInfo, messages);
		}
//		messages.save ();
//		return (mapping.findForward ("success"));

		return new AppointmentServiceInfo(200, this.messageResources.getValue("success.appointmentProfileEdit"));//, doctorId, newDay + "", apptProfileId, appointmentInfo.patientId, appointmentInfo.apptId,appointmentInfo.notes);
	}

	private boolean setProfileAndRangeEnd(AppointmentInfo appointmentInfo,
			DoctorRecord doctorRecord, MessagesInlineService messages, String useCustomizeAppt, 
			String apptProfileId, String minute, String duration) throws Exception {
		if (useCustomizeAppt.equals("1")) {
			return (setCustomAppointment(appointmentInfo, doctorRecord,
					messages, useCustomizeAppt, apptProfileId, minute));
		} else {
			return (setNonCustomAppointment(appointmentInfo, messages, useCustomizeAppt, apptProfileId, duration));
		}
	}
	private boolean setCustomAppointment (
			AppointmentInfo appointmentInfo,
			DoctorRecord doctorRecord,
			MessagesInlineService messages,
			String useCustomizeAppt, String apptProfileId, String minute
		) throws Exception
		{
			ApptProfileRecord apptProfileRecord = new ApptProfileRecord ();

			if (apptProfileId == null) {
				messages.addGenericMessage ("error.missing.apptProfileId");
				return (false);
			}
			if (apptProfileId.equals ("0")) {
				messages.addGenericMessage ("error.invalid.apptProfileId");
				return (false);
			}
			if (doctorRecord.hasCustomizeAppt.equals ("0")) {
				messages.addGenericMessage ("error.failed.apptAdd");
				return (false);
			}
			if (apptProfileRecord.readByFilter (new String [][] {
				{"apptProfileId", apptProfileId},
				{"doctorId", doctorRecord.doctorId}
			}) == false) {
				messages.addGenericMessage ("error.failed.apptAdd");
				return (false);
			}
			//Check if the start Time met the required restriction
			if (apptProfileRecord.startAtSlot.equals("0")== false){
				logger.debug("checking start time restriction...");
				boolean isStartTimeOk = ApptSearchHelper.isApptStartTimeOK (
														minute,
														appointmentInfo.epochDay,
														apptProfileRecord,
														doctorRecord
													);
			    // alert user wrong start time
				if (isStartTimeOk == false) {
					String requiredStartSlot = ApptSearchHelper.getRequiredStartSlot(apptProfileRecord.startAtSlot);
					messages.addGenericMessage ("error.failed.apptAdd");
					messages.addDynamicMessage ("The start time for this appointment must be the "+ requiredStartSlot);
					return (false);
				}
			}
			appointmentInfo.apptProfileId = apptProfileId;
			appointmentInfo.rangeEnd = 0;
			return (true);
		}

		/**
		* This method set non customized appt object.
		*/
		private boolean setNonCustomAppointment (
			AppointmentInfo appointmentInfo,
			MessagesInlineService messages,
			String useCustomizeAppt, String apptProfileId, String duration
		)
		{
			int durationInt;

			if (duration == null) {
				messages.addGenericMessage ("error.missing.duration");
				return (false);
			}
			try {
				durationInt = Integer.parseInt (duration);
			} catch (NumberFormatException ex) {
				messages.addGenericMessage ("error.invalid.duration");
				return (false);
			}
			appointmentInfo.apptProfileId = "0";
			appointmentInfo.rangeEnd = appointmentInfo.rangeStart + durationInt * 60;
			return (true);
		}
	
}
