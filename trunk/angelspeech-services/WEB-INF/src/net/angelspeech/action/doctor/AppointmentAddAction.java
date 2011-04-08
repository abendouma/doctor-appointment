package net.angelspeech.action.doctor;

import java.util.GregorianCalendar;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.MappingDispatchAction;
import org.apache.struts.validator.DynaValidatorForm;

import net.angelspeech.database.ApptProfileRecord;
import net.angelspeech.database.ScheduleDayRecord;
import net.angelspeech.database.DoctorRecord;
import net.angelspeech.database.PatientRecord;
import net.angelspeech.database.SqlQuery;

import net.angelspeech.object.AppointmentInfo;
import net.angelspeech.object.EmergencyInfo;
import net.angelspeech.object.HttpHelper;
import net.angelspeech.object.MessagesInline;
import net.angelspeech.object.NotifyHelper;
import net.angelspeech.object.PatientHelper;
import net.angelspeech.object.SessionDoctor;
import net.angelspeech.object.SuperuserHelper;
import net.angelspeech.object.ApptSearchHelper;

import net.angelspeech.object.SettingsHelper;
import net.angelspeech.object.TextExpansion;
import net.angelspeech.object.TimeHelper;
import net.angelspeech.object.EmailHelper;
import net.angelspeech.object.PhoneHelper;
import net.angelspeech.object.SqlHelper;
import net.angelspeech.object.BuildJavaScript;


import org.apache.log4j.Logger;

/**
 * This class implements the doctor page used to add a new appointment.
 */
public class AppointmentAddAction extends MappingDispatchAction
{
	static private Logger logger = Logger.getLogger(AppointmentAddAction.class);

	/**
	 * Displays the "Add appointment" page and prompts doctor for appointment data.
	 *
	 * @return		Return an ActionForward instance
	 * @param mapping	The ActionMapping used to select this instance
	 * @param form		The optional ActionForm bean for this request
	 * @param request	The HTTP request we are processing
	 * @param response	The HTTP response we are creating
	 */
	public ActionForward do_prompt (
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response
	) throws java.lang.Exception
	{
		String doctorId;
		Integer epochDay, startSlot, apptProfileId;

		HttpHelper.disableCache (response);
		if ((doctorId = HttpHelper.getString (request, "doctorId")) == null) {
			logger.info("Logout on addApptPrompt caused by missing doctorId");
			return (mapping.findForward ("doctorLogin"));
		}
		if ((epochDay = HttpHelper.getInteger (request, "epochDay")) == null) {
			logger.info("Logout on addApptPrompt caused by missing epochDay");
			logger.info("doctorId is "+ doctorId);
			return (mapping.findForward ("doctorLogin"));
		}
		if ((startSlot = HttpHelper.getInteger (request, "startSlot")) == null) {
			logger.info("Logout on addApptPrompt caused by missing startSlot");
			logger.info("doctorId is "+ doctorId +" epochDay is "+ epochDay);
			return (mapping.findForward ("doctorLogin"));
		}
		if ((apptProfileId = HttpHelper.getInteger (request, "apptProfileId")) == null) {
			logger.info("Logout on addApptPrompt caused by missing apptProfileId");
			logger.info("doctorId is "+ doctorId +" epochDay is "+ epochDay);
			return (mapping.findForward ("doctorLogin"));
		}
		if (SessionDoctor.checkAccess (request, doctorId) == false) {
			logger.info("Logout on addApptPrompt caused by checkAccess failure");
			logger.info("doctorId is "+ doctorId +" epochDay is "+ epochDay);
			return (mapping.findForward ("doctorLogin"));
		}
		request.setAttribute ("doctorId", doctorId);
		request.setAttribute ("epochDay", epochDay);
		request.setAttribute ("startSlot", startSlot);
		request.setAttribute ("apptProfileId", apptProfileId);

		/**
		BUG FIX: store the epochDay and startSlot value into session so
		that add-appt.jsp can reload original form user input after
		struts page forward following data validation failure
		*/
		HttpSession session = request.getSession();
		session.setAttribute("epochDay", epochDay);
		session.setAttribute("startSlot", startSlot);
		session.setAttribute ("apptProfileId", apptProfileId);
		DynaValidatorForm aForm = (DynaValidatorForm) form;
		// set default optional field reminderType to "both" (phone and email)
		aForm.set ("reminderType", "3");
		aForm.set ("apptProfileId", apptProfileId.toString());
		return (mapping.findForward ("success"));
	}

	/**
	 * Takes the user input from the "Add appointment" doctor page, checks the user input and
	 * if it is correct adds a new appointment.
	 *
	 * @return		Return an ActionForward instance
	 * @param mapping	The ActionMapping used to select this instance
	 * @param form		The optional ActionForm bean for this request
	 * @param request	The HTTP request we are processing
	 * @param response	The HTTP response we are creating
	 */
	public ActionForward do_submit (
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response
	) throws java.lang.Exception
	{
		String doctorId;
		DynaValidatorForm aForm;
		MessagesInline messages = new MessagesInline (request);
		String patientId;
		PatientRecord patientRecord = new PatientRecord ();
		PatientRecord patientRecordNew = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		AppointmentInfo appointmentInfo = new AppointmentInfo ();
		HttpHelper.disableCache (response);
		if ((doctorId = HttpHelper.getString (request, "doctorId")) == null) {
			logger.info("Logout on addApptSubmit caused by missing doctorId");
			return (mapping.findForward ("doctorLogin"));
		}
		if (SessionDoctor.checkAccess (request, doctorId) == false) {
			logger.info("Logout on addApptSubmit caused by checkAccess failure");
			logger.info("doctorId is "+ doctorId);
			return (mapping.findForward ("doctorLogin"));
		}
		/*
		BUG FIX: store the epochDay and startSlot value into session so
		that add-appt.jsp can reload original form user input after
		struts page forward following data validation failure		*/
		aForm = (DynaValidatorForm) form;
		String apptProfileId = aForm.getString("apptProfileId");
		if (apptProfileId.length()==0){
			apptProfileId = "0";
		}
		int epochDay = getStartDay (aForm);
		
		// if input date format is bad		
		if (epochDay == -1){
			messages.addGenericMessage ("error.invalid.startDate");
			messages.save ();
			request.setAttribute ("doctorId", doctorId);
			request.setAttribute ("apptProfileId", apptProfileId);
			return (mapping.findForward ("failure"));		
		}
		int startSlot = getStartSlot (aForm, epochDay, doctorId);
		if (startSlot == -1){
			messages.addGenericMessage ("error.invalid.startMinute");
			messages.save ();
			request.setAttribute ("doctorId", doctorId);
			request.setAttribute ("apptProfileId", apptProfileId);
			return (mapping.findForward ("failure"));
		}
		
		HttpSession session = request.getSession();
		session.setAttribute("epochDay", new Integer (epochDay));
		session.setAttribute("startSlot", new Integer (startSlot));
		session.setAttribute ("apptProfileId",  new Integer(apptProfileId));
		//logger.info("Store epochDay("+epochDay+") and startSlot("+startSlot+") into session.");
		messages.addActionMessages (aForm.validate (mapping, request));
		if (messages.isEmptyDisplay () == false) {
			messages.save ();
			request.setAttribute ("doctorId", doctorId);
			request.setAttribute ("apptProfileId", apptProfileId);
			return (mapping.findForward ("failure"));
		}
		if (epochDay < TimeHelper.currentEpochDay ()) {
			messages.addGenericMessage ("error.failed.apptAdd");
			messages.save ();
			request.setAttribute ("doctorId", doctorId);
			request.setAttribute ("epochDay", new Integer (epochDay));
			request.setAttribute ("startSlot", new Integer (startSlot));
			request.setAttribute ("apptProfileId", apptProfileId);
			return (mapping.findForward ("failure"));
		}
		doctorRecord.readById (doctorId);
		String phone = PhoneHelper.normalize(aForm.getString ("phone"), false);
		// create patient record only if phone number is valid
		if (phone== null) {
			messages.addGenericMessage ("error.invalid.phone");
			messages.save ();
			request.setAttribute ("doctorId", doctorId);
			request.setAttribute ("apptProfileId", apptProfileId);
			return (mapping.findForward ("failure"));
		}
		// if patient is new, add new record and the optional data
		if (patientRecord.readByFilter (new String [][] {
			{"doctorId", doctorId},
			{"firstName", aForm.getString ("firstName")},
			{"lastName", aForm.getString ("lastName")},
			{"phone", phone}
		}) == false) {
			patientRecord.doctorId = doctorId;
			patientRecord.firstName = aForm.getString ("firstName");
			patientRecord.middleName = "";
			patientRecord.lastName = aForm.getString ("lastName");
			patientRecord.ssn = aForm.getString ("fileId");
			patientRecord.reminderType = aForm.getString ("reminderType");
			patientRecord.language = aForm.getString ("language");
			patientRecord.checkInNotes = aForm.getString ("checkInNotes");
			patientRecord.street = "";
			patientRecord.city = "";
			patientRecord.state = "";
			patientRecord.zip = "";
			patientRecord.phone = phone;
			patientRecord.phone2 = "";
			patientRecord.smsCarrier = "";
			patientRecord.email = aForm.getString ("email");
			patientRecord.isActive = "1";
			patientRecord.isRestricted = "0";
			patientRecord.latestActivityDay = String.valueOf(epochDay);
			patientRecord.createdDay = String.valueOf(TimeHelper.currentEpochDay ());
			patientId = PatientHelper.multiCreate (patientRecord);
			PatientHelper.patientMatrixUpdate (messages, patientId, request);
			messages.addGenericMessage ("success.patientAdd");
			patientRecord.patientId = patientId;
			NotifyHelper.patientAdd (patientRecord, doctorRecord, messages);
		}
		/**
		*	if patient record is found successfully (not new patient)...
		*	only none-restricted patient can make new appt
		*/
		if (patientRecord.isRestricted.equalsIgnoreCase("1")){
			messages.addGenericMessage ("error.failed.isRestricted");
			messages.save ();
			return (mapping.findForward ("success"));
		}
		//pending patient can only make one appt
		if(patientRecord.isRestricted.equals("2")){
			int apptTotal = PatientHelper.getPatientAppointmentCount (patientRecord.patientId);
			logger.info("the pending patient id " + patientRecord.patientId + " has "+ apptTotal+ " appt now");
			if (apptTotal != 0) {
				messages.addGenericMessage ("error.failed.isRestricted");
				messages.save ();
				return (mapping.findForward ("success"));
			}
		}
		//Save or update the email on patient record if it was new or modified
		patientRecordNew.email = aForm.getString ("email");
		if (patientRecord.email.equalsIgnoreCase (patientRecordNew.email) == false) {
			patientRecord.email = patientRecordNew.email;
			PatientHelper.multiUpdate (patientRecord);
			messages.setPatientOne (doctorId, patientRecord.patientId);
			messages.addGenericMessage ("success.patientEdit");
			NotifyHelper.patientEdit (patientRecord, doctorRecord, messages);
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
			PatientHelper.patientMatrixUpdate (messages, patientRecord.patientId, request);
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
		appointmentInfo.notes = aForm.getString ("notes");
		appointmentInfo.epochDay = epochDay;
		appointmentInfo.rangeStart =
			Integer.parseInt (aForm.getString ("startHour")) * 3600 +
			Integer.parseInt (aForm.getString ("startMinute")) * 60;
		if (setProfileAndRangeEnd (
			appointmentInfo,
			doctorRecord,
			aForm,
			messages
		) == false) {
			messages.save ();
			request.setAttribute ("doctorId", doctorId);
			request.setAttribute ("epochDay", new Integer (epochDay));
			request.setAttribute ("startSlot", new Integer (startSlot));
			request.setAttribute ("apptProfileId", apptProfileId);
			return (mapping.findForward ("failure"));
		}
		if (appointmentInfo.create (false) == false) {
			messages.addGenericMessage ("error.failed.apptAdd");
			messages.save ();
			request.setAttribute ("doctorId", doctorId);
			request.setAttribute ("epochDay", new Integer (epochDay));
			request.setAttribute ("startSlot", new Integer (startSlot));
			request.setAttribute ("apptProfileId", apptProfileId);
			return (mapping.findForward ("failure"));
		}
		messages.setSchedule (doctorId, appointmentInfo.epochDay, null);
		messages.addGenericMessage ("success.apptAdd");
		NotifyHelper.appointmentAdd (appointmentInfo, messages);
		/** 
		if customized appt is active (set to 1)
		Send email with question form link if apptProfile requires
		question form be filled by patient
		*/
		if (doctorRecord.hasCustomizeAppt.equals("1")){
			apptProfileId = aForm.getString ("apptProfileId");
			ApptProfileRecord apptProfileRecord = new ApptProfileRecord ();		
			apptProfileRecord.readById(apptProfileId);
			if (apptProfileRecord.hasQuestionForm.equals("1")){
				messages.addGenericMessage ("info.apptRequireQusetionForm");
				NotifyHelper.requestPatientQuestionForm(appointmentInfo, apptProfileRecord);
			}
		}
		messages.save ();
		return (mapping.findForward ("success"));
	}

	private boolean setProfileAndRangeEnd (
		AppointmentInfo appointmentInfo,
		DoctorRecord doctorRecord,
		DynaValidatorForm form,
		MessagesInline messages
	) throws Exception
	{
		if (form.getString ("useCustomizeAppt").equals ("1")) {
			return (setCustomAppointment (
				appointmentInfo,
				doctorRecord,
				form,
				messages
			));
		} else {
			return (setNonCustomAppointment (
				appointmentInfo,
				form,
				messages
			));
		}
	}

	private boolean setCustomAppointment (
		AppointmentInfo appointmentInfo,
		DoctorRecord doctorRecord,
		DynaValidatorForm form,
		MessagesInline messages
	) throws Exception
	{
		String apptProfileId;
		ApptProfileRecord apptProfileRecord = new ApptProfileRecord ();

		if ((apptProfileId = form.getString ("apptProfileId")) == null) {
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
													form.getString ("startMinute"),
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
		DynaValidatorForm form,
		MessagesInline messages
	)
	{
		String durationString;
		int durationInt;

		if ((durationString = form.getString ("duration")) == null) {
			messages.addGenericMessage ("error.missing.duration");
			return (false);
		}
		try {
			durationInt = Integer.parseInt (durationString);
		} catch (NumberFormatException ex) {
			messages.addGenericMessage ("error.invalid.duration");
			return (false);
		}
		appointmentInfo.apptProfileId = "0";
		appointmentInfo.rangeEnd = appointmentInfo.rangeStart + durationInt * 60;
		return (true);
	}
	/**
	 * Search and activate appointment record of an inactive patient.
	 *
	 * @return		Return an ActionForward instance
	 * @param mapping	The ActionMapping used to select this instance
	 * @param form		The optional ActionForm bean for this request
	 * @param request	The HTTP request we are processing
	 * @param response	The HTTP response we are creating
	 */
	public ActionForward do_search_archive (
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response
	) throws java.lang.Exception
	{
		Integer epochDay, startSlot, apptProfileId;
		String doctorId, firstNameValue, lastNameValue, phoneValue, emailValue;
		MessagesInline messages = new MessagesInline (request);
		String [][] rows;

		HttpHelper.disableCache (response);
		if ((doctorId = SessionDoctor.getActiveDoctor (request)) == null) {
			logger.info("Logout on addApptSearchArchive caused by missing doctorId");
			return (mapping.findForward ("doctorLogin"));
		}
		if ((firstNameValue = HttpHelper.getString (request, "firstName")) == null) {
			firstNameValue = "";
		}
		if ((lastNameValue = HttpHelper.getString (request, "lastName")) == null) {
			lastNameValue = "";
		}
		if ((phoneValue = HttpHelper.getString (request, "phone")) == null) {
			phoneValue = "";
		}		if ((emailValue = HttpHelper.getString (request, "email")) == null) {
			emailValue = "";
		}

		rows = SqlQuery.query (
			"SELECT " +
				"patientId, " +
				"firstName, " +
				"lastName, " +
				"phone, " +
				"email " +
			"FROM patients " +
			"WHERE " +
				"(doctorId='" + SqlQuery.escape (doctorId) + "') AND " +
				SqlHelper.searchTerm ("firstName", firstNameValue) + " AND " +
				SqlHelper.searchTerm ("lastName", lastNameValue) + " AND " +
				SqlHelper.searchTerm ("phone", phoneValue) + " AND " +
				SqlHelper.searchTerm ("email", emailValue)
		);
		logger.debug("searching on..."+firstNameValue+" "+lastNameValue
					+ " "+phoneValue+" "+emailValue);
		logger.debug("matching total "+ String.valueOf(rows.length) + " rows...\n");
		// set params for next page (add-appt)
		request.setAttribute ("doctorId", doctorId);
		/**
		* if there is one match then activate the record
		* and return to make appt page
		*/
		if (rows.length == 1) {
			PatientRecord patientRecord = new PatientRecord ();
			patientRecord.readById (rows [0][0]);
			patientRecord.isActive = "1";
			PatientHelper.multiUpdate (patientRecord);
			PatientHelper.patientMatrixUpdate (messages, patientRecord.patientId, request);
			messages.addGenericMessage ("success.patientActivated");
			messages.save ();
			return (mapping.findForward ("success"));
		}else{
			messages.addGenericMessage ("error.failed.multipleMatches");
			messages.save();
			return (mapping.findForward ("failure"));
		}
	}
	/**
	 * Cancels an existing appointment.
	 *
	 * @return		Return an ActionForward instance
	 * @param mapping	The ActionMapping used to select this instance
	 * @param form		The optional ActionForm bean for this request
	 * @param request	The HTTP request we are processing
	 * @param response	The HTTP response we are creating
	 */
	public ActionForward do_cancel_appt (
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response
	) throws java.lang.Exception
	{
		String apptId;
		MessagesInline messages = new MessagesInline (request);
		AppointmentInfo appointmentInfo = new AppointmentInfo ();
		Integer apptProfileId;

		HttpHelper.disableCache (response);
		if ((apptId = HttpHelper.getString (request, "apptId")) == null) {
			logger.info("Logout on addApptCancelAppt caused by missing apptId");
			return (mapping.findForward ("doctorLogin"));
		}
		if ((apptProfileId = HttpHelper.getInteger (request, "apptProfileId")) == null) {
			logger.info("Logout on addApptCancelAppt caused by missing apptProfileId");
			logger.info("apptId is "+ apptId);
			return (mapping.findForward ("doctorLogin"));
		}
		if (appointmentInfo.read (apptId) == false) {
			logger.info("Logout on addApptCancelAppt caused by failure reading appointmentInfo record ");
			logger.info("apptId is "+ apptId);
			return (mapping.findForward ("doctorLogin"));
		}
		if (SessionDoctor.checkAccess (request, appointmentInfo.doctorId) == false) {
			logger.info("Logout on addApptCancelAppt caused by checkAccess failure ");
			logger.info("apptId is "+ apptId);
			return (mapping.findForward ("doctorLogin"));
		}
		if (appointmentInfo.epochDay < TimeHelper.currentEpochDay ()) {
			messages.addGenericMessage ("error.failed.apptCancel");
			messages.save ();
			request.setAttribute ("doctorId", appointmentInfo.doctorId);
			request.setAttribute ("apptProfileId", apptProfileId);
			return (mapping.findForward ("success"));
		}
		if (AppointmentInfo.destroy (apptId) == false) {
			messages.addGenericMessage ("error.failed.apptCancel");
			messages.save ();
			request.setAttribute ("doctorId", appointmentInfo.doctorId);
			request.setAttribute ("apptProfileId", apptProfileId);
			return (mapping.findForward ("success"));
		}
		messages.setSchedule (appointmentInfo.doctorId, appointmentInfo.epochDay, null);
		messages.addGenericMessage ("success.apptCancel");
		NotifyHelper.appointmentCancel (appointmentInfo, messages);
		messages.save ();
		request.setAttribute ("doctorId", appointmentInfo.doctorId);
		request.setAttribute ("apptProfileId", apptProfileId);
		return (mapping.findForward ("success"));
	}

	/**
	 * Cancels an existing emergency appointment.
	 *
	 * @return		Return an ActionForward instance
	 * @param mapping	The ActionMapping used to select this instance
	 * @param form		The optional ActionForm bean for this request
	 * @param request	The HTTP request we are processing
	 * @param response	The HTTP response we are creating
	 */
	public ActionForward do_cancel_emrg (
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response
	) throws java.lang.Exception
	{
		String emrgId;
		MessagesInline messages = new MessagesInline (request);
		EmergencyInfo emergencyInfo = new EmergencyInfo ();
		Integer apptProfileId;

		HttpHelper.disableCache (response);
		if ((emrgId = HttpHelper.getString (request, "emrgId")) == null) {
			logger.info("Logout on addApptCancelEmrg caused by missing emrgId ");
			return (mapping.findForward ("doctorLogin"));
		}
		if ((apptProfileId = HttpHelper.getInteger (request, "apptProfileId")) == null) {
			logger.info("Logout on addApptCancelEmrg caused by missing apptProfileId");
			logger.info("emrgId is "+ emrgId);
			return (mapping.findForward ("doctorLogin"));
		}
		if (emergencyInfo.read (emrgId) == false) {
			logger.info("Logout on addApptCancelEmrg caused by failure reading emergencyInfo record ");
			logger.info("emrgId is "+ emrgId);
			return (mapping.findForward ("doctorLogin"));
		}
		if (SessionDoctor.checkAccess (request, emergencyInfo.doctorId) == false) {
			logger.info("Logout on addApptCancelEmrg caused by checkAccess failure ");
			logger.info("emrgId is "+ emrgId);
			return (mapping.findForward ("doctorLogin"));
		}
		if (emergencyInfo.epochDay != TimeHelper.currentEpochDay ()) {
			messages.addGenericMessage ("error.failed.emrgCancel");
			messages.save ();
			request.setAttribute ("doctorId", emergencyInfo.doctorId);
			request.setAttribute ("apptProfileId", apptProfileId);
			return (mapping.findForward ("success"));
		}
		if (EmergencyInfo.destroy (emrgId) == false) {
			messages.addGenericMessage ("error.failed.emrgCancel");
			messages.save ();
			request.setAttribute ("doctorId", emergencyInfo.doctorId);
			request.setAttribute ("apptProfileId", apptProfileId);
			return (mapping.findForward ("success"));
		}
		messages.setSchedule (emergencyInfo.doctorId, emergencyInfo.epochDay, null);
		messages.addGenericMessage ("success.emrgCancel");
		messages.save ();
		request.setAttribute ("doctorId", emergencyInfo.doctorId);
		request.setAttribute ("apptProfileId", apptProfileId);
		return (mapping.findForward ("success"));
	}

	/**
	 * This method convert form input date to epochDay
	 */
	static private int getStartDay (DynaValidatorForm form)
	{
		String [] dateParts;
		try {
			dateParts = form.getString ("startDate").split ("/");
			return (TimeHelper.calendarToDays (new GregorianCalendar (
				Integer.parseInt (dateParts [2]) + 2000,
				Integer.parseInt (dateParts [0]) - 1,
				Integer.parseInt (dateParts [1])
			)));				
		} catch (Exception ex) {
			return (-1);
		}

	}
	/**
	 * This method convert form input to startSlot
	 */
	static private int getStartSlot (DynaValidatorForm form, int epochDay, String doctorId)
	{		
		int startSlot=0;		
		try {
			ScheduleDayRecord epochDayRecord = new ScheduleDayRecord ();
			if ((epochDayRecord.readByFilter (new String [][] {
				{"doctorId", doctorId},
				{"epochDay", String.valueOf (epochDay)}
				}))==true){
					// calculate startSlot in int
					int startHour =	(new Integer(form.getString ("startHour"))).intValue();
					int startMinutes = (new Integer (form.getString ("startMinute"))).intValue();
					int rangeStart = startHour * 3600 +	startMinutes * 60;
					int i_slotSize = (new Integer(epochDayRecord.slotSize)).intValue();
					startSlot = rangeStart / i_slotSize;
			}
			return startSlot;
		} catch (Exception ex) {
			return (-1);
		}
	}
}
