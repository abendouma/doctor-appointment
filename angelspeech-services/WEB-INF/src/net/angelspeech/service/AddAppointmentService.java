package net.angelspeech.service;

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
import net.angelspeech.object.NotifyHelper;
import net.angelspeech.object.NotifyHelperService;
import net.angelspeech.object.PatientHelper;
import net.angelspeech.object.PhoneHelper;
import net.angelspeech.object.RESTfulServiceHelper;
import net.angelspeech.object.TimeHelper;
import net.angelspeech.service.dto.AppointmentServiceInfo;
import net.angelspeech.util.AngelspeedUtils;
import net.angelspeech.util.MessageResourcesManager;

import org.apache.log4j.Logger;

/**
 * @author Quang mailto:quangnguyen111@gmail.com
 */
@Path("/addappt")
public class AddAppointmentService extends WebServiceSupport {
	static private Logger logger = Logger.getLogger(AddAppointmentService.class);
	private static final String DURATION = "15";

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/xml")
	public AppointmentServiceInfo addAppointment(@FormParam("doctorId") String doctorId, 
										@FormParam("firstname") String firstname,
										@FormParam("lastname") String lastname,
										@FormParam("phone") String phone,
										@FormParam("email") String email,
										@FormParam("note") String note,
										@FormParam("date") String date,
										@FormParam("hour") String hour,
										@FormParam("minute") String minute,
										@FormParam("apptProfileId") String apptProfileId,
										@FormParam("restkey") String restkey) throws Exception {
		
		this.messageResources = new MessageResourcesManager(context.getRealPath("/"));
		System.out.println(this.messageResources);
		System.out.println(this.messageResources.getValue("error.login"));
		System.out.println(this.messageResources.getValue("error.appt.not.existed"));
		String useCustomizeAppt = "";
		String duration = DURATION;
		
		MessagesInlineService messages = new MessagesInlineService ();
		String patientId;
		PatientRecord patientRecord = new PatientRecord ();
		PatientRecord patientRecordNew = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		AppointmentInfo appointmentInfo = new AppointmentInfo ();

		if (doctorId == null) {
			logger.info("Logout on addApptSubmit caused by missing doctorId");
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.login"));
		}
		
		if (restkey == null) {
			logger.info("REST key is null");
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.login"));
		}
		
		if (!RESTfulServiceHelper.isRESTKeyValid(doctorId, restkey)) {
			logger.info("Invalid Rest key");
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.login"));
		}
		
		/*
		BUG FIX: store the epochDay and startSlot value into session so
		that add-appt.jsp can reload original form user input after
		struts page forward following data validation failure
		*/
		if (apptProfileId.length()==0) {
			apptProfileId = "0";
		}
		int epochDay = AngelspeedUtils.getStartDay (date);
		
		// if input date format is bad
		if (epochDay == -1){
			messages.addGenericMessage ("error.invalid.startDate");
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.invalid.startDate"), doctorId, apptProfileId);
		}
		
		int startSlot = AngelspeedUtils.getStartSlot (hour, minute, epochDay, doctorId);
		if (startSlot == -1){
			messages.addGenericMessage ("error.invalid.startMinute");
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.invalid.startMinute"), doctorId, apptProfileId);
		}
		
		if (messages.isEmptyDisplay () == false) {
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.message.failure"), doctorId, apptProfileId);
		}
		if (epochDay < TimeHelper.currentEpochDay ()) {
			messages.addGenericMessage ("error.failed.apptAdd");
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.failed.apptAdd"), doctorId, epochDay + "", startSlot + "", apptProfileId);
		}
		doctorRecord.readById (doctorId);
		phone = PhoneHelper.normalize(phone, false);
		
		// create patient record only if phone number is valid
		if (phone== null) {
			messages.addGenericMessage ("error.invalid.phone");
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.invalid.phone"), doctorId, apptProfileId);
		}
		// if patient is new, add new record and the optional data
		if (patientRecord.readByFilter (new String [][] {
			{"doctorId", doctorId},
			{"firstName", firstname},
			{"lastName", lastname},
			{"phone", phone}
		}) == false) {
			patientRecord.doctorId = doctorId;
			patientRecord.firstName = firstname;
			patientRecord.middleName = "";
			patientRecord.lastName = lastname;
			patientRecord.ssn = "";
			patientRecord.reminderType = ""; //aForm.getString ("reminderType");
			patientRecord.language = ""; //aForm.getString ("language");
			patientRecord.checkInNotes = ""; //aForm.getString ("checkInNotes");
			patientRecord.street = "";
			patientRecord.city = "";
			patientRecord.state = "";
			patientRecord.zip = "";
			patientRecord.phone = phone;
			patientRecord.phone2 = "";
			patientRecord.smsCarrier = "";
			patientRecord.email = email; //aForm.getString ("email");
			patientRecord.isActive = "1";
			patientRecord.isRestricted = "0";
			patientRecord.latestActivityDay = String.valueOf(epochDay);
			patientRecord.createdDay = String.valueOf(TimeHelper.currentEpochDay ());
			patientId = PatientHelper.multiCreate (patientRecord);
			PatientHelper.patientMatrixUpdate (messages, patientId);
			messages.addGenericMessage ("success.patientAdd");
			patientRecord.patientId = patientId;
//			NotifyHelperService.patientAdd (patientRecord, doctorRecord, messages);//temporary disable
		}
		/**
		*	if patient record is found successfully (not new patient)...
		*	only none-restricted patient can make new appt
		*/
		if (patientRecord.isRestricted.equalsIgnoreCase("1")){
			messages.addGenericMessage ("error.failed.isRestricted");
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.failed.isRestricted"), doctorId, apptProfileId);
		}
		//pending patient can only make one appt
		if(patientRecord.isRestricted.equals("2")){
			int apptTotal = PatientHelper.getPatientAppointmentCount (patientRecord.patientId);
			logger.info("the pending patient id " + patientRecord.patientId + " has "+ apptTotal+ " appt now");
			if (apptTotal != 0) {
				messages.addGenericMessage ("error.failed.isRestricted");
				return new AppointmentServiceInfo(201, this.messageResources.getValue("error.failed.isRestricted"), doctorId, apptProfileId);
			}
		}
		//Save or update the email on patient record if it was new or modified
		patientRecordNew.email = email;
		if (patientRecord.email.equalsIgnoreCase (patientRecordNew.email) == false) {
			patientRecord.email = patientRecordNew.email;
			PatientHelper.multiUpdate (patientRecord);
			messages.setPatientOne (doctorId, patientRecord.patientId);
			messages.addGenericMessage ("success.patientEdit");
			NotifyHelperService.patientEdit (patientRecord, doctorRecord, messages);
		}
		// update patient activity status on record
		patientRecord.isActive = "1";
		/**
		* if latestActivityDay is past, update the latestActivityDay
		* otherwise chose the appt day closest to today as latestActivityDay
		*/
		int activityDay = TimeHelper.getStringInt(patientRecord.latestActivityDay);
		int currentDay = TimeHelper.currentEpochDay ();
		if (( activityDay < currentDay)
			|| (activityDay > epochDay)){
			patientRecord.latestActivityDay = String.valueOf(epochDay);
			PatientHelper.multiUpdate (patientRecord);
			PatientHelper.patientMatrixUpdate (messages, patientRecord.patientId);
		}
		// created an appt
		appointmentInfo.doctorId = doctorId;
		appointmentInfo.patientId = patientRecord.patientId;
		appointmentInfo.requireSIU = doctorRecord.hasHL7Channels.equals ("1");
		if ((doctorRecord.hasGoogleSync.equals ("1"))||(doctorRecord.hasGoogleSync.equals ("2"))){
			appointmentInfo.requireGsync = AppointmentInfo.GSYNC_TO_GOOGLE;
		}
		appointmentInfo.requireSMS = (patientRecord.isSMSActive())? "1" : "0";
		appointmentInfo.fromPatientGui = false;
		appointmentInfo.notes = note;
		appointmentInfo.epochDay = epochDay;
		appointmentInfo.recurrentApptId = "0";
		appointmentInfo.rangeStart =
			Integer.parseInt (hour) * 3600 +
			Integer.parseInt (minute) * 60;
		if (setProfileAndRangeEnd (
			appointmentInfo,
			doctorRecord,
			messages, useCustomizeAppt, apptProfileId, minute, duration
		) == false) {
//			messages.save ();
//			request.setAttribute ("doctorId", doctorId);
//			request.setAttribute ("epochDay", new Integer (epochDay));
//			request.setAttribute ("startSlot", new Integer (startSlot));
//			request.setAttribute ("apptProfileId", apptProfileId);
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.failed.setProfileAndRankEnd"), doctorId, epochDay + "", startSlot + "", apptProfileId);
		}
		if (appointmentInfo.create (false) == false) {
			messages.addGenericMessage ("error.failed.apptAdd");
//			messages.save ();
//			request.setAttribute ("doctorId", doctorId);
//			request.setAttribute ("epochDay", new Integer (epochDay));
//			request.setAttribute ("startSlot", new Integer (startSlot));
//			request.setAttribute ("apptProfileId", apptProfileId);
//			return (mapping.findForward ("failure"));
			return new AppointmentServiceInfo(201, this.messageResources.getValue("error.failed.apptAdd"), doctorId, epochDay + "", startSlot + "", apptProfileId);
		}
		messages.setSchedule (doctorId, appointmentInfo.epochDay, null);
		messages.addGenericMessage ("success.apptAdd");
		NotifyHelperService.appointmentAdd (appointmentInfo, messages);
		/** 
		if customized appt is active (set to 1)
		Send email with question form link if apptProfile requires
		question form be filled by patient
		*/
		if (doctorRecord.hasCustomizeAppt.equals("1")){
			ApptProfileRecord apptProfileRecord = new ApptProfileRecord ();		
			apptProfileRecord.readById(apptProfileId);
			if (apptProfileRecord.hasQuestionForm.equals("1")){
				messages.addGenericMessage ("info.apptRequireQusetionForm");
				NotifyHelper.requestPatientQuestionForm(appointmentInfo, apptProfileRecord);
			}
		}

		return new AppointmentServiceInfo(200, this.messageResources.getValue("success.apptAdd"), doctorId, epochDay + "", startSlot + "", apptProfileId, appointmentInfo.patientId, appointmentInfo.apptId,appointmentInfo.notes);
	}
	
	private boolean setProfileAndRangeEnd(AppointmentInfo appointmentInfo,
			DoctorRecord doctorRecord, String useCustomizeAppt, String apptProfileId, String minute, String duration) throws Exception {
		if (useCustomizeAppt.equals("1")) {
			return (setCustomAppointment(appointmentInfo, doctorRecord, apptProfileId, minute));
		} else {
			return (setNonCustomAppointment(appointmentInfo, apptProfileId, duration));
		}
	}

	private boolean setCustomAppointment(AppointmentInfo appointmentInfo,
			DoctorRecord doctorRecord, String apptProfileId, String minute) throws Exception {
		ApptProfileRecord apptProfileRecord = new ApptProfileRecord();

		if (apptProfileId == null) {
			return (false);
		}
		if (apptProfileId.equals("0")) {
			return (false);
		}
		if (doctorRecord.hasCustomizeAppt.equals("0")) {
			return (false);
		}
		if (apptProfileRecord.readByFilter(new String[][] {
				{ "apptProfileId", apptProfileId },
				{ "doctorId", doctorRecord.doctorId } }) == false) {
//			messages.addGenericMessage("error.failed.apptAdd");
			return (false);
		}
		// Check if the start Time met the required restriction
		if (apptProfileRecord.startAtSlot.equals("0") == false) {
			logger.debug("checking start time restriction...");
			boolean isStartTimeOk = ApptSearchHelper.isApptStartTimeOK(
					minute, appointmentInfo.epochDay,
					apptProfileRecord, doctorRecord);
			// alert user wrong start time
			if (isStartTimeOk == false) {
				String requiredStartSlot = ApptSearchHelper
						.getRequiredStartSlot(apptProfileRecord.startAtSlot);
//				messages.addGenericMessage("error.failed.apptAdd");
//				messages.addDynamicMessage("The start time for this appointment must be the "
//						+ requiredStartSlot);
				return (false);
			}
		}
		appointmentInfo.apptProfileId = apptProfileId;
		appointmentInfo.rangeEnd = 0;
		return (true);
	}

	private boolean setNonCustomAppointment(AppointmentInfo appointmentInfo, String apptProfileId, 
			String duration) {
		int durationInt;

		if (duration == null) {
//			messages.addGenericMessage("error.missing.duration");
			return (false);
		}
		try {
			durationInt = Integer.parseInt(duration);
		} catch (NumberFormatException ex) {
//			messages.addGenericMessage("error.invalid.duration");
			return (false);
		}
		appointmentInfo.apptProfileId = "0";
		appointmentInfo.rangeEnd = appointmentInfo.rangeStart + durationInt
				* 60;
		return (true);
	}
	
	private boolean setProfileAndRangeEnd (
			AppointmentInfo appointmentInfo,
			DoctorRecord doctorRecord,
			MessagesInlineService messages, String useCustomizeAppt, String apptProfileId, String minute, String duration
			
		) throws Exception
		{
			if (useCustomizeAppt.equals ("1")) {
				return (setCustomAppointment (
					appointmentInfo,
					doctorRecord,
					messages, useCustomizeAppt, apptProfileId, minute, duration
				));
			} else {
				return (setNonCustomAppointment (
					appointmentInfo,
					messages, useCustomizeAppt, apptProfileId, minute, duration
				));
			}
		}

		private boolean setCustomAppointment (
			AppointmentInfo appointmentInfo,
			DoctorRecord doctorRecord,
			MessagesInlineService messages, String useCustomizeAppt, String apptProfileId, String minute, String duration
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

		private boolean setNonCustomAppointment (
			AppointmentInfo appointmentInfo,
			MessagesInlineService messages, String useCustomizeAppt, String apptProfileId, String minute, String duration
		)
		{
//			String durationString;
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
