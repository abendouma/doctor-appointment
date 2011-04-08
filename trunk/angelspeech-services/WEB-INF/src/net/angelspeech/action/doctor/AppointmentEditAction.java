package net.angelspeech.action.doctor;

import java.util.GregorianCalendar;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.MappingDispatchAction;
import org.apache.struts.validator.DynaValidatorForm;

import net.angelspeech.database.ApptProfileRecord;
import net.angelspeech.database.DoctorRecord;
import net.angelspeech.database.PatientRecord;
import net.angelspeech.database.SqlQuery;

import net.angelspeech.object.AppointmentInfo;
import net.angelspeech.object.HttpHelper;
import net.angelspeech.object.MessagesInline;
import net.angelspeech.object.NotifyHelper;
import net.angelspeech.object.PatientHelper;
import net.angelspeech.object.SessionDoctor;
import net.angelspeech.object.SuperuserHelper;
import net.angelspeech.object.ApptSearchHelper;

import net.angelspeech.object.SettingsHelper;
import net.angelspeech.object.TextExpansion;
import net.angelspeech.object.CallRecordLog;
import net.angelspeech.object.TimeHelper;
import net.angelspeech.object.EmailHelper;
import net.angelspeech.object.MiscHelper;

import org.apache.log4j.Logger;

/**
 * This class implements the doctor page used to edit an existing appointment.
 */
public class AppointmentEditAction extends MappingDispatchAction
{
	static private Logger logger = Logger.getLogger (AppointmentEditAction.class);

	/**
	 * Displays the "Edit appointment" page and prompts doctor for appointment data.
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
		String apptId;
		AppointmentInfo appointmentInfo = new AppointmentInfo ();
		MessagesInline messages = new MessagesInline (request);

		HttpHelper.disableCache (response);
		if ((apptId = HttpHelper.getString (request, "apptId")) == null) {
			return (mapping.findForward ("doctorLogin"));
		}
		if (appointmentInfo.read (apptId) == false) {
			return (mapping.findForward ("doctorLogin"));
		}
		if (SessionDoctor.checkAccess (request, appointmentInfo.doctorId) == false) {
			return (mapping.findForward ("doctorLogin"));
		}

		//If appt is pending payment disable reschedule page
		if (appointmentInfo.apptStatus.equalsIgnoreCase("1")){
			messages.addGenericMessage ("error.failed.prepaidPending");
			messages.save ();
			return (mapping.findForward ("pending"));
		}else{
			messages.setSchedule (appointmentInfo.doctorId, appointmentInfo.epochDay, null);
			messages.save ();
			request.setAttribute ("doctorId", appointmentInfo.doctorId);
			request.setAttribute ("apptId", apptId);
			request.setAttribute ("askConfirm", "false");
			return (mapping.findForward ("success"));
		}
	}

	/**
	 * Takes the user input from the "Edit appointment" doctor page, checks the user input and
	 * if it is correct modifies the appointment.
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
		String apptId;
		DynaValidatorForm aForm;
		DoctorRecord doctorRecord = new DoctorRecord ();
		AppointmentInfo appointmentInfo = new AppointmentInfo ();
		MessagesInline messages = new MessagesInline (request);
		int oldDay, oldRangeStart, newDay, currentDay;

		HttpHelper.disableCache (response);
		if ((apptId = HttpHelper.getString (request, "apptId")) == null) {
			return (mapping.findForward ("doctorLogin"));
		}
		if (appointmentInfo.read (apptId) == false) {
			return (mapping.findForward ("doctorLogin"));
		}
		if (SessionDoctor.checkAccess (request, appointmentInfo.doctorId) == false) {
			return (mapping.findForward ("doctorLogin"));
		}
		aForm = (DynaValidatorForm) form;
		messages.addActionMessages (aForm.validate (mapping, request));

		//Set attribute before forwarding in different points
		request.setAttribute ("askConfirm", "false");
		request.setAttribute ("doctorId", appointmentInfo.doctorId);
		request.setAttribute ("apptId", apptId);

		if (messages.isEmptyDisplay () == false) {
			messages.save ();
			//request.setAttribute ("doctorId", appointmentInfo.doctorId);
			//request.setAttribute ("apptId", apptId);
			return (mapping.findForward ("failure"));
		}
		oldDay = appointmentInfo.epochDay;
		oldRangeStart = appointmentInfo.rangeStart;
		newDay = getStartDay (aForm);
		// if date is in bad format
		if (newDay == -1){
			messages.addGenericMessage ("error.invalid.startDate");
			messages.save ();
			//request.setAttribute ("doctorId", appointmentInfo.doctorId);
			//request.setAttribute ("apptId", apptId);
			return (mapping.findForward ("failure"));
		}
		currentDay = TimeHelper.currentEpochDay ();
		if ((oldDay < currentDay) || (newDay < currentDay)) {
			messages.addGenericMessage ("error.failed.apptEdit");
			messages.save ();
			//request.setAttribute ("doctorId", appointmentInfo.doctorId);
			//request.setAttribute ("apptId", apptId);
			return (mapping.findForward ("failure"));
		}
		// update appt record
		doctorRecord.readById (appointmentInfo.doctorId);

		//save input data from reschedule page.
		appointmentInfo.notes = aForm.getString ("notes");
		appointmentInfo.epochDay = newDay;
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
			//request.setAttribute ("doctorId", appointmentInfo.doctorId);
			//request.setAttribute ("apptId", apptId);
			return (mapping.findForward ("failure"));
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
			messages.save ();
			//request.setAttribute ("doctorId", appointmentInfo.doctorId);
			//request.setAttribute ("apptId", apptId);
			return (mapping.findForward ("failure"));
		}
		messages.setSchedule (appointmentInfo.doctorId, oldDay, null);
		if (oldDay != newDay) {
			messages.setSchedule (appointmentInfo.doctorId, newDay, null);
		}
		messages.addGenericMessage ("success.apptEdit");
		if (isApptTimeChanged) {
			NotifyHelper.appointmentEdit (oldDay, oldRangeStart, appointmentInfo, messages);
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

	/**
	* This method set non customized appt object.
	*/
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
	 * Cancels an existing appointment.
	 *
	 * @return		Return an ActionForward instance
	 * @param mapping	The ActionMapping used to select this instance
	 * @param form		The optional ActionForm bean for this request
	 * @param request	The HTTP request we are processing
	 * @param response	The HTTP response we are creating
	 */
	public ActionForward do_cancel (
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response
	) throws java.lang.Exception
	{
		String apptId, notifyByPhone;
		AppointmentInfo appointmentInfo = new AppointmentInfo ();
		MessagesInline messages = new MessagesInline (request);

		HttpHelper.disableCache (response);
		if ((apptId = HttpHelper.getString (request, "apptId")) == null) {
			return (mapping.findForward ("doctorLogin"));
		}
		if ((notifyByPhone = HttpHelper.getString (request, "notifyByPhone")) == null) {
			return (mapping.findForward ("doctorLogin"));
		}
		if (appointmentInfo.read (apptId) == false) {
			return (mapping.findForward ("doctorLogin"));
		}
		if (SessionDoctor.checkAccess (request, appointmentInfo.doctorId) == false) {
			return (mapping.findForward ("doctorLogin"));
		}
		request.setAttribute ("askConfirm", "false");
		request.setAttribute ("doctorId", appointmentInfo.doctorId);
		if (appointmentInfo.epochDay < TimeHelper.currentEpochDay ()) {
			messages.addGenericMessage ("error.failed.apptCancel");
			messages.save ();
			return (mapping.findForward ("success"));
		}
		/**
		*Search recurrent appts associate to this apptId
		*and display warning with recurrent appt dates
		* recurrentApptId = 0: NO recurrent appt
		* recurrentApptId = 1: first appt of a list of recurrent appt
		* recurrentApptId = int. recurrent appt link
		*/
		String recurrentApptId = appointmentInfo.recurrentApptId;
		// if no recurrent appt is involved just cancel appt as requested
		if (recurrentApptId.equalsIgnoreCase("0")){
			if (AppointmentInfo.destroy (apptId) == false) {
				messages.addGenericMessage ("error.failed.apptCancel");
				messages.save ();
				return (mapping.findForward ("success"));
			}
			messages.setSchedule (appointmentInfo.doctorId, appointmentInfo.epochDay, null);
			messages.addGenericMessage ("success.apptCancel");
			if (notifyByPhone.equalsIgnoreCase("true")){
				NotifyHelper.appointmentCancel (appointmentInfo, messages);
			}else{
				NotifyHelper.appointmentCancelNoCall (appointmentInfo, messages);
			}

			messages.save ();
			return (mapping.findForward ("success"));
		}
		// if recurrent appt is involved
		if (recurrentApptId.equalsIgnoreCase("1")) {
			recurrentApptId = appointmentInfo.apptId;
		}
		String[] cancelRecurrentApptIds
				= getAndDisplayRecurrentApptsToCancel(appointmentInfo.patientId, recurrentApptId, messages);
		/**
		* If only one recurrent appt is found
		* then cancel the appt without more confirmation
		* Otherwise if more than one recurrent appts were found
		* Forward to confirmation page to ask if single or all appts
		* are to be cancelled
		*/

		if (cancelRecurrentApptIds.length == 1){
			if (AppointmentInfo.destroy (apptId) == false) {
				messages.addGenericMessage ("error.failed.apptCancel");
				messages.save ();
				return (mapping.findForward ("success"));
			}
			messages.setSchedule (appointmentInfo.doctorId, appointmentInfo.epochDay, null);
			messages.addGenericMessage ("success.apptCancel");
			if (notifyByPhone.equalsIgnoreCase("true")){
				NotifyHelper.appointmentCancel (appointmentInfo, messages);
			}else{
				NotifyHelper.appointmentCancelNoCall (appointmentInfo, messages);
			}

			messages.save ();
			return (mapping.findForward ("success"));
		}else{
			messages.save ();
			request.setAttribute ("askConfirm", "true");
			request.getSession (true).setAttribute ("cancelRecurrentApptIds", cancelRecurrentApptIds);
			request.getSession (true).setAttribute ("apptId", apptId);
			request.getSession (true).setAttribute ("notifyByPhone", notifyByPhone);
			return (mapping.findForward ("recurrent"));
		}
	}

	/**
	 * Cancels an recurrent appointment in future date from a given starting apptId.
	 * This method is redirected from AppointmentDailyAction();
	 * @return		Return an ActionForward instance
	 * @param mapping	The ActionMapping used to select this instance
	 * @param form		The optional ActionForm bean for this request
	 * @param request	The HTTP request we are processing
	 * @param response	The HTTP response we are creating
	 */
	public ActionForward confirm_cancel_recurrent_appt (
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response
	) throws java.lang.Exception
	{
		String doctorId, apptId, notifyByPhone;
		Integer epochDay;
		MessagesInline messages = new MessagesInline (request);

		HttpHelper.disableCache (response);
		logger.debug("confirm_cancel_recurrent_appt called...");
		if ((doctorId = HttpHelper.getString (request, "doctorId")) == null) {
			logger.info("Logout caused by missing doctorId");
			return (mapping.findForward ("doctorLogin"));
		}
		if (SessionDoctor.checkAccess (request, doctorId) == false) {
			return (mapping.findForward ("doctorLogin"));
		}
		//Retrieve appt info from session
		String [] cancelRecurrentApptIds = (String []) request.getSession (true).getAttribute ("cancelRecurrentApptIds");
		apptId = (String) request.getSession (true).getAttribute ("apptId");
		notifyByPhone = (String) request.getSession (true).getAttribute ("notifyByPhone");

		logger.debug("apptId...notifyByPhone are... "+ apptId + ".."+ notifyByPhone);
		if (apptId == null) {
			return (mapping.findForward ("doctorLogin"));
		}
		if (notifyByPhone == null) {
			return (mapping.findForward ("doctorLogin"));
		}
		//Set request attributes before forwarding at different points
		request.setAttribute ("askConfirm", "false");
		request.setAttribute ("doctorId", doctorId);
		//define array of appt object for cancellation
		int totalAppts = cancelRecurrentApptIds.length;
		AppointmentInfo [] appointmentInfo = new AppointmentInfo [totalAppts];
		//Read all to-be-cancelled appt records
		for (int i = 0; i < totalAppts; ++i) {
			appointmentInfo[i] = new AppointmentInfo();
			if (appointmentInfo[i].read (cancelRecurrentApptIds[i]) == false) {
				messages.addGenericMessage ("error.failed.recurrentApptCancel");
				messages.save ();
				return (mapping.findForward ("success"));
			}
		}
		//validate record once more
		for (int i = 0; i < totalAppts; ++i) {
			if (doctorId.equals (appointmentInfo[i].doctorId) == false) {
				messages.addGenericMessage ("error.failed.recurrentApptCancel");
				messages.save ();
				return (mapping.findForward ("success"));
			}
		}
		//Delete all appt records and push update to client
		for (int i = 0; i < totalAppts; ++i) {
			if (AppointmentInfo.destroy (cancelRecurrentApptIds[i]) == false) {
				messages.addGenericMessage ("error.failed.recurrentApptCancel");
				messages.save ();
				return (mapping.findForward ("success"));
			}
			messages.setSchedule (doctorId, appointmentInfo[i].epochDay, null);
		}
		messages.addDynamicMessage ("Total of " + String.valueOf (totalAppts)+ " recurrent appointments have been cancelled!");
		if (notifyByPhone.equalsIgnoreCase("true")){
			NotifyHelper.appointmentCancel (appointmentInfo[0], messages);
		}else{
			NotifyHelper.recurrentAppointmentCancel (appointmentInfo, messages);
		}
		messages.save ();
		return (mapping.findForward ("success"));
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
	public ActionForward confirm_cancel_single_appt (
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response
	) throws java.lang.Exception
	{
		String doctorId, apptId, notifyByPhone;
		Integer epochDay;
		MessagesInline messages = new MessagesInline (request);
		AppointmentInfo appointmentInfo = new AppointmentInfo ();

		HttpHelper.disableCache (response);
		logger.debug("confirm_cancel_single_appt called...");
		if ((doctorId = HttpHelper.getString (request, "doctorId")) == null) {
			logger.info("Logout caused by missing doctorId");
			return (mapping.findForward ("doctorLogin"));
		}
		if (SessionDoctor.checkAccess (request, doctorId) == false) {
			return (mapping.findForward ("doctorLogin"));
		}
		//Retrieve appt info from session
		apptId = (String) request.getSession (true).getAttribute ("apptId");
		notifyByPhone = (String) request.getSession (true).getAttribute ("notifyByPhone");

		logger.debug("apptId...notifyByPhone are... "+ apptId + ".."+ notifyByPhone);

		if (apptId == null) {
			return (mapping.findForward ("doctorLogin"));
		}
		if (notifyByPhone == null) {
			return (mapping.findForward ("doctorLogin"));
		}

		//Set request attributes before forwarding at different points
		request.setAttribute ("askConfirm", "false");
		request.setAttribute ("doctorId", doctorId);

		if (appointmentInfo.read (apptId) == false) {
			messages.addGenericMessage ("error.failed.apptCancel");
			messages.save ();
			return (mapping.findForward ("success"));
		}
		if (doctorId.equals (appointmentInfo.doctorId) == false) {
			messages.addGenericMessage ("error.failed.apptCancel");
			messages.save ();
			return (mapping.findForward ("success"));
		}
		if (AppointmentInfo.destroy (apptId) == false) {
			messages.addGenericMessage ("error.failed.apptCancel");
			messages.save ();
			return (mapping.findForward ("success"));
		}
		messages.setSchedule (doctorId, appointmentInfo.epochDay, null);
		messages.addGenericMessage ("success.apptCancel");
		if (notifyByPhone.equalsIgnoreCase("true")){
			NotifyHelper.appointmentCancel (appointmentInfo, messages);
		}else{
			NotifyHelper.appointmentCancelNoCall (appointmentInfo, messages);
		}
		messages.save ();
		return (mapping.findForward ("success"));
	}
	/**
	 * add log record for an no-show appointment.
	 *
	 * @return		Return an ActionForward instance
	 * @param mapping	The ActionMapping used to select this instance
	 * @param form		The optional ActionForm bean for this request
	 * @param request	The HTTP request we are processing
	 * @param response	The HTTP response we are creating
	 */
	public ActionForward do_noshow (
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response
	) throws java.lang.Exception
	{
		String apptId, notifyByPhone;
		AppointmentInfo appointmentInfo = new AppointmentInfo ();
		MessagesInline messages = new MessagesInline (request);

		HttpHelper.disableCache (response);
		if ((apptId = HttpHelper.getString (request, "apptId")) == null) {
			return (mapping.findForward ("doctorLogin"));
		}
		if (appointmentInfo.read (apptId) == false) {
			return (mapping.findForward ("doctorLogin"));
		}
		if (SessionDoctor.checkAccess (request, appointmentInfo.doctorId) == false) {
			return (mapping.findForward ("doctorLogin"));
		}
		request.setAttribute ("askConfirm", "false");
		if (appointmentInfo.epochDay < TimeHelper.currentEpochDay ()) {
			messages.addGenericMessage ("error.failed.apptEdit");
			messages.save ();
			return (mapping.findForward ("success"));
		}
		//create a no-show call record for this appt;
		// save the call log info.
		int callStartTime=TimeHelper.currentEpochSecond ();
		CallRecordLog callRecordLog = new CallRecordLog();
		String logResult= callRecordLog.logCallRecord(
								appointmentInfo.doctorId,					//doctorId
								"noShow",									//callType
								String.valueOf(callStartTime),				//callStartTime
								"none",										//patientPhone
								String.valueOf(appointmentInfo.patientId),	//patientId
								"none",										//transferredToPhone
								String.valueOf(appointmentInfo.epochDay),	//apptEpochDay
								String.valueOf(appointmentInfo.rangeStart),	//apptStartSecond
								"none",										//errorEventLog
								"success"									//callResult
							);
		logger.debug("Call record logging for cancel appt no call is ..."+ logResult+"\n");
		messages.addGenericMessage ("success.apptEdit");
		messages.save ();
		return (mapping.findForward ("success"));
	}

	static private int getStartDay (DynaValidatorForm form)
	{
		String [] dateParts;
		try {
			dateParts = form.getString ("startDate").split ("/");
			return (TimeHelper.calendarToDays (new GregorianCalendar (
				Integer.parseInt (dateParts [2]) + 2000,
				Integer.parseInt (dateParts [0]) - 1,
				Integer.parseInt (dateParts [1])
			)));		} catch (Exception ex) {
			return (-1);
		}
	}

	/**
	 * This method search for future days that has recurrent appts for
	 * cancellation and display these dates in confirmation message
	 * cancelApptId is the original appt user clicked to cancel
	 * recurrentApptId is the recurrentApptId that all recurrent appts shares
	 */
	 static private String[] getAndDisplayRecurrentApptsToCancel(
	 	String patientId,
	 	String recurrentApptId,
	 	MessagesInline messages
	 ) throws Exception
	{
		//Search for affected recurrent appts that share same scheduleApptId and patientId
		String [][] rows;
		rows = SqlQuery.query (
			"SELECT scheduleApptId, epochDay " +
			"FROM scheduleAppt INNER JOIN scheduleDays USING (scheduleDayId) " +
			"WHERE " +
				"patientId='" + SqlQuery.escape (patientId) + "' AND " +
				"recurrentApptId='" + SqlQuery.escape (recurrentApptId) + "' AND " +
				"epochDay >= '" +
					SqlQuery.escape (String.valueOf (TimeHelper.currentEpochDay ())) +
				"'"
		);
		logger.debug("displayRecurrentApptsToCancel() has find..."+ rows.length + " future recurrent appts to cancel");

		int recurrentApptEpochDay;
		boolean firstRecurrentApptIncluded = false;
		AppointmentInfo firstAppointmentInfo = new AppointmentInfo();
		// if first recurrrent appt (still exist)id = 1, should be included if still valid
		if (firstAppointmentInfo.read(recurrentApptId)){
			if (firstAppointmentInfo.epochDay >= TimeHelper.currentEpochDay ()) {
				firstRecurrentApptIncluded = true;
			}
		}
		int totalAppts = rows.length;
		if (firstRecurrentApptIncluded){
			totalAppts = rows.length+1;
		}
		/**
		* Insert warning message only if there is more than ONE
		* recurrent appts found
		*/
		String [] recurrentApptIds = new String [totalAppts];
		if (totalAppts > 1){
			//Warning message heading
			messages.addDynamicMessage ("WARNING: This is a recurrent appointment!\n"
			+"Press OK to cancel all recurrent appointments on dates below.\n"
			+"Press CANCEL to cancel this one appointment only.\n\n" );
			if (firstRecurrentApptIncluded){
				messages.addDynamicMessage (TimeHelper.epochDayToString (firstAppointmentInfo.epochDay));
			}
		}

		for (int i = 0; i < rows.length; ++i) {
			recurrentApptIds [i] = rows [i][0];
			logger.info("find recurrent apptId.."+ recurrentApptIds [i]);
			recurrentApptEpochDay = MiscHelper.getStringInt(rows [i][1]);
			if (totalAppts > 1){
				messages.addDynamicMessage (TimeHelper.epochDayToString (recurrentApptEpochDay));
			}
		}
		if (firstRecurrentApptIncluded){
			recurrentApptIds [rows.length] = recurrentApptId;
		}

		logger.debug("displayRecurrentApptsToCancel() has returned..."+ recurrentApptIds.length + " total recurrent appts to cancel");
		return recurrentApptIds;
	} // end of getAnddisplayRecurrentApptsToCancel()


}