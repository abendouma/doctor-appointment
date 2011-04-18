package net.angelspeech.object;

import java.io.File;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import net.angelspeech.database.DoctorRecord;
import net.angelspeech.database.MyNetworkData;
import net.angelspeech.database.Patient2Record;
import net.angelspeech.database.PatientRecord;
import net.angelspeech.database.ScheduleApptRecord;
import net.angelspeech.database.SqlQuery;

import org.apache.log4j.Logger;


/**
 * This class contains helper functions for managing patients
 */

public class PatientHelper
{
	static private Logger logger = Logger.getLogger(PatientHelper.class);


	/**
	 * This method creates a given patient account for all sibling doctor accounts if the
	 * doctor is controlled by a superuser, or just for the specified doctor if the doctor
	 * is not controlled by a superuser.
	 *
	 * @return		Return the patient ID for the patient created for the
	 *			original doctor ID specified in the patient record.
	 * @param patientRecord	The patient record that must be duplicated for all sibling
	 *			doctors.
	 */
	static public String multiCreate (PatientRecord patientRecord) throws Exception
	{
		String result, doctorId;
		String [] doctors;
		int i;
		String patientId;
		DoctorRecord doctorRecord = new DoctorRecord();
		Patient2Record patient2Record = new Patient2Record();
		
		result = null;
		doctorId = patientRecord.doctorId;
		doctors = SuperuserHelper.getSiblingControlledDoctors (doctorId);
		logger.debug ("SuperuserHelper find total..."+ doctors.length + " Sibling Controlled doctors ");
		for (i = 0; i < doctors.length; ++i) {
			patientRecord.doctorId = doctors [i];
			patientId = patientRecord.create ();
			//if the doctor has HL7 service add patient2 record for HL7 service
			doctorRecord.readById(doctors [i]);
			if (doctorRecord.hasHL7Channels.equals("0")== false){
				patient2Record.patientId = patientId;
				patient2Record.externalPatientId = " ";
				patient2Record.create ();
			}
			//return the patientId associated to the original doctorId
			if (patientRecord.doctorId.equals (doctorId)) {
				result = patientId;
			}
			logger.debug ("Multi create patient Record..."+ patientId);
		}
		return (result);
	}

	/**
	 * This method destroys a given patient account for all sibling doctor accounts if the
	 * doctor is controlled by a superuser, or just for the specified doctor if the doctor
	 * is not controlled by a superuser.
	 *
	 * @param patientId	The patient ID for the destroyed patient account.
	 */
	static public void multiDestroy (String patientId) throws Exception
	{
		PatientRecord patientOriginal = new PatientRecord ();
		PatientRecord patientCurrent = new PatientRecord ();
		String [] doctors;
		int i;

		patientOriginal.readById (patientId);
		doctors = SuperuserHelper.getSiblingControlledDoctors (patientOriginal.doctorId);
		logger.debug ("SuperuserHelper find total..."+ doctors.length + " Sibling Controlled doctors ");
		for (i = 0; i < doctors.length; ++i) {
			if (patientCurrent.readByFilter (new String [][] {
				{"doctorId", doctors [i]},
				{"firstName", patientOriginal.firstName},
				{"middleName", patientOriginal.middleName},
				{"lastName", patientOriginal.lastName}
			})) {
				singleDestroy (patientCurrent.patientId);
			}
			logger.debug ("Multi destroy patient Record..."+ patientCurrent.patientId);
		}
	}
	/**
	* Delete record associated with patientId from all database table
	*/
	static public void singleDestroy (String patientId) throws Exception
	{
		MyNetworkData myNetworkData;
		File recording;
		PatientRecord pRec = createPatientDeleteLog (patientId);
		SqlQuery.query (
			"DELETE " +
			"FROM patients " +
			"WHERE patientId='" + SqlQuery.escape (patientId) + "'"
		);
		SqlQuery.query (
			"DELETE " +
			"FROM scheduleAppt " +
			"WHERE patientId='" + SqlQuery.escape (patientId) + "'"
		);
		SqlQuery.query (
			"DELETE " +
			"FROM scheduleCallBack " +
			"WHERE patientId='" + SqlQuery.escape (patientId) + "'"
		);
		SqlQuery.query (
			"DELETE " +
			"FROM callRecord " +
			"WHERE patientId='" + SqlQuery.escape (patientId) + "'"
		);
		SqlQuery.query (
			"DELETE " +
			"FROM patients2 " +
			"WHERE patientId='" + SqlQuery.escape (patientId) + "'"
		);
		SqlQuery.query (
			"DELETE " +
			"FROM scheduleEmrg " +
			"WHERE patientId='" + SqlQuery.escape (patientId) + "'"
		);
		// will not look for recording if record is Google
		if (!pRec.isGooglePatient()){
			myNetworkData = MyNetworkData.getInstance ();
			recording = new File (
				myNetworkData.PatientRecordingFileDir ,
				patientId + "_Name.wav"
			);
			if (recording.exists ()) {
				recording.delete ();
			}
		}
	}

	/**
	* create a log for patient record deletion
	* Return patient's "FirstName LastName"
	*/

	private static PatientRecord createPatientDeleteLog (String patientId) throws Exception
	{
		logger.info("save callRecord for deleted patient..." + patientId);
		PatientRecord patientRecord = new PatientRecord ();
		patientRecord.readById(patientId);
		if (patientRecord.middleName.length()==0){
			patientRecord.middleName = "NA";
		}
		if (patientRecord.phone.length()==0){
			patientRecord.phone = "NA";
		}
		if (patientRecord.phone2.length()==0){
			patientRecord.phone2 = "NA";
		}
		if (patientRecord.email.length()==0){
			patientRecord.email = "NA";
		}

		int callStartTime=TimeHelper.currentEpochSecond ();
		//Save required patient record info into error event log
		String errorEventLog =  "patientId: "+ "%%"+ patientRecord.patientId + "%%" +
								"firstName: " + "%%" + patientRecord.firstName + "%%" +
								"middleName: " + "%%" + patientRecord.middleName + "%%" +
								"lastName: "+ "%%" + patientRecord.lastName + "%%" +
								"phone: " + "%%" + patientRecord.phone + "%%" +
								"phone2: " + "%%" + patientRecord.phone2 + "%%" +
								"email: " + "%%"+ patientRecord.email + "%%";

		CallRecordLog callRecordLog = new CallRecordLog();
		String logResult= callRecordLog.logCallRecord(
								patientRecord.doctorId,						//doctorId
								"deletePatient",							//callType
								String.valueOf(callStartTime),				//callStartTime
								patientRecord.phone,						//patientPhone
								"0",										//patientId
								"none",										//transferredToPhone
								"0",										//apptEpochDay
								"0",										//apptStartSecond
								errorEventLog,								//errorEventLog
								"deleted"									//callResult
							);

		String patientName= patientRecord.firstName + " " + patientRecord.lastName;
		logger.info("Call record logging for delete patient record for..."+ patientName + " with result "+logResult+"\n");
		return patientRecord;
	}

	/**
	 * This method updates a given patient account for all sibling doctor accounts if the
	 * doctor is controlled by a superuser, or just for the specified doctor if the doctor
	 * is not controlled by a superuser.
	 *
	 * @param patientId	The record for the updated patient account.
	 */
	static public void multiUpdate (PatientRecord patientRecord) throws Exception
	{
		String [] patients;
		PatientRecord cloneRecord;
		int i;

		patients = SuperuserHelper.getSiblingControlledPatients (patientRecord.patientId);
		logger.debug ("SuperuserHelper find total..."+ patients.length + " Sibling Controlled patient records ");
		cloneRecord = (PatientRecord) patientRecord.clone ();
		cloneRecord.patientId = null;
		cloneRecord.doctorId = null;
		for (i = 0; i < patients.length; ++i) {
			logger.debug("Patient id:"+patients[i]);
			if(patients[i] != null) {
				cloneRecord.writeById (patients [i]);
				logger.debug ("Multi update patient Record..."+ patients [i]);
			}
		}
	}

	/**
	 * This method checks if at least one of the the patients from a group is allowed to add an appointment
	 * through the patient GUI. The appointment count for a given patient also includes it appointments for its
	 * sibling patient accounts.
	 *
	 * @return		Return true iff there is at least one patient that is allowed to add another appointment
	 * @param patients	An array of checked patient IDs
	 * @param maxAllowed The max total of allowed patient self-service appt.
	 */
	static public boolean checkPatientAppointments (String [] patients, int maxAllowed) throws Exception
	{
		int i;

		for (i = 0; i < patients.length; ++i) {
			if(patients[i] != null) {
				if (checkPatientSiblings (patients [i], maxAllowed)) {
					return (true);
				}
			}
		}
		return (false);
	}

	static private boolean checkPatientSiblings (String patientId, int maxAllowed) throws Exception
	{
		String [] patients;
		int count, i;

		patients = SuperuserHelper.getSiblingControlledPatients (patientId);
		count = 0;
		for (i = 0; i < patients.length; ++i) {
			if(patients[i] != null) {
				count += getPatientWebAppointmentCount (patients [i]);
			}
		}
		// configurable maxAllowed appts allowed by patient self-service scheduling
		return (count < maxAllowed);
	}
	/*
	* This method count total appts for a patient made by patient self service
	*
	*/
	static public int getPatientWebAppointmentCount (String patientId) throws Exception
	{
		String [][] rows;

		rows = SqlQuery.query (
			"SELECT count(*) " +
			"FROM scheduleDays INNER JOIN scheduleAppt USING (scheduleDayId) " +
			"WHERE " +
				"patientId='" + SqlQuery.escape (patientId) + "' AND " +
				"fromPatientGui='1' AND " +
				"epochDay >= '" +
					SqlQuery.escape (String.valueOf (TimeHelper.currentEpochDay ())) +
				"'"
		);
		return (Integer.parseInt (rows [0][0]));
	}

	/*
	* This method get the pending patient apptId
	* if patient did not make appt then null is returned
	*/
	static public String getPendingPatientApptId (String patientId) throws Exception
	{
		String [][] rows;

		rows = SqlQuery.query (
			"SELECT scheduleApptId " +
			"FROM scheduleAppt " +
			"WHERE " +
				"patientId='" + SqlQuery.escape (patientId) + "'"
		);
		if (rows.length == 0){
			return null;
		}else{
			return (rows [0][0]);
		}
	}
//	/*
//	* This method fileter/match one patientId from an allowed list
//	* using a few matching parametres, This method could be used
//	* for security check (double check) of a existing patientId against
//	* other paremeters.
//	*/
//	static public String getPatientId (
//		String doctorId,
//		String [] allowedPatients,
//		DynaValidatorForm form
//	) throws Exception
//	{
//		String allowedList;
//		int i;
//		String [][] rows;
//
//		allowedList = "";
//		for (i = 0; i < allowedPatients.length; ++i) {
//			allowedList +=
//				((i == 0) ? "" : ", ") +
//				("'" + SqlQuery.escape (allowedPatients [i]) + "'");
//		}
//		rows = SqlQuery.query (
//			"SELECT patientId " +
//			"FROM patients " +
//			"WHERE " +
//				"patientId IN (" + allowedList + ") AND " +
//				"doctorId='" + SqlQuery.escape (doctorId) + "' AND " +
//				"firstName='" + SqlQuery.escape (form.getString ("firstName")) + "' AND " +
//				"lastName='" + SqlQuery.escape (form.getString ("lastName")) + "' AND " +
//				"email='" + SqlQuery.escape (form.getString ("email")) + "'"
//				//"phone='" + SqlQuery.escape (form.getString ("phone")) + "'"
//		);
//		if (rows.length == 0) {
//			return (null);
//		}
//		return (rows [0][0]);
//	}
	/*
	* This method count total appts for a patient
	*
	*/
	static public int getPatientAppointmentCount (String patientId) throws Exception
	{
		String [][] rows;

		rows = SqlQuery.query (
			"SELECT count(*) " +
			"FROM scheduleDays INNER JOIN scheduleAppt USING (scheduleDayId) " +
			"WHERE " +
				"patientId='" + SqlQuery.escape (patientId) + "' AND " +
				"epochDay >= '" +
					SqlQuery.escape (String.valueOf (TimeHelper.currentEpochDay ())) +
				"'"
		);
		return (Integer.parseInt (rows [0][0]));
	}

	static public void patientMatrixUpdate (MessagesInline messages, String patientId, HttpServletRequest request) throws Exception
	{
		String [] patients;
		PatientRecord patientRecord = new PatientRecord ();
		int i;

		if (SessionDoctor.getActiveSuperuser (request) != null) {
			patients = SuperuserHelper.getSiblingControlledPatients (patientId);
		} else {
			patients = new String [] {patientId};
		}
		for (i = 0; i < patients.length; ++i) {
			if(patients[i] != null) {
				patientRecord.readById (patients [i]);
				messages.setPatientOne (
					patientRecord.doctorId,
					patientRecord.patientId
				);
			}
		}
	}

	static public void patientMatrixUpdate (MessagesInlineService messages, String patientId) throws Exception
	{
		String [] patients;
		PatientRecord patientRecord = new PatientRecord ();
		int i;

		patients = new String [] {patientId};
		for (i = 0; i < patients.length; ++i) {
			if(patients[i] != null) {
				patientRecord.readById (patients [i]);
				messages.setPatientOne (
					patientRecord.doctorId,
					patientRecord.patientId
				);
			}
		}
	}

	
	static public int getMaxActivePatients ()
	{
		int MaxActivePatients;
		try {
			MaxActivePatients = SettingsHelper.readInt ("max.activePatients", SettingsHelper.PerformanceSettings);
			if (MaxActivePatients < 0) {
				logger.error ("Got a negative MaxActivePatients, assuming 0");
				return (0);
			}
			logger.debug ("Got an MaxActivePatients of " + String.valueOf (MaxActivePatients) + " patients");
			return (MaxActivePatients);
		} catch (Exception ex) {
				logger.error ("Receive an Exception when reading max.activePatients, assuming 0");
				return (0);
		}
	}		static public int getMaxSearchResult ()
	{
		int MaxSearchResult;
		try {
			MaxSearchResult = SettingsHelper.readInt ("max.searchResult", SettingsHelper.PerformanceSettings);
			if (MaxSearchResult < 0) {
				logger.error ("Got a negative max searchResult, assuming 0");
				return (0);
			}
			logger.debug ("Got an max searchResult of " + String.valueOf (MaxSearchResult) + " patients");
			return (MaxSearchResult);
		} catch (Exception ex) {
				logger.debug ("Receive an Exception when reading max.searchResult, assuming 0");
				return (0);
		}
	}
	
	/**
	 * This method remove the unconfirmed appt for doctors with open scheduler
	 *
	 * @param none. The method scan the entire database
	 */
	static public int deleteUnpaidPendingAppts () throws Exception
	{
		String [][] rows;
		//search all appt profile for pre-paid
		int total_deleted_appt = 0;
		String apptId;
	
		// search doctor records that has open Scehduler 
		rows = SqlQuery.query (
				"SELECT scheduleApptId " +
				"FROM scheduleAppt " +
				"WHERE " +
					"apptStatus='1' " 
		);
			//if pending appt has been found
		if (rows.length != 0){
		logger.info("Have found..."+rows.length+" pending unpaid appt records");
			//Throw exception, stop execution when have found too many appt in pending
			if (rows.length > 100){
				logger.info("Alert Admin.... have found " + rows.length + " unpaid appointments in db");
				String alertMessage = "deleteUnpaidPendingAppts() has found too many -->" + rows.length + " unpaid appointments in db";
				NotifyHelper.sendAdminAlertEmail("PatientHelper  DB Scan", alertMessage);
				return (0);
			}else{
				//delete these pending unconfirmed appts
				for (int j = 0; j < rows.length; ++j) {
					apptId = rows [j][0];
					if (deleteUnpaidAppt(apptId)){
						total_deleted_appt = total_deleted_appt + 1;
					}
				}
			}
		}	
		logger.info("Have deleted total of ..."+ total_deleted_appt + " unpaid appointments");
		return (total_deleted_appt);
	}	
		
	/**
	 * This method remove the unconfirmed appt for doctors with open scheduler
	 *
	 * @param none. The method scan the entire database
	 */
	static public int deleteUnconfirmedOpenSchedulerAppts () throws Exception
	{
		String [][] rows;
		//search all appt profile for pre-paid
		int total_deleted_appt = 0;
		String apptId;
	
		// search doctor records that has open Scehduler 
		rows = SqlQuery.query (
				"SELECT scheduleApptId " +
				"FROM scheduleAppt " +
				"WHERE " +
					"apptStatus='2' " 
		);
			//if pending appt has been found
		if (rows.length != 0){
		logger.info("Have found..."+rows.length+" pending appt records");
			//Throw exception, stop execution when have found too many appt in pending
			if (rows.length > 100){
				logger.info("Alert Admin.... have found " + rows.length + " unconfirmed appointments in db");
				String alertMessage = "deleteUnconfirmedOpenSchedulerAppts() has found too many -->" + rows.length + " unpaid appointments in db";
				NotifyHelper.sendAdminAlertEmail("PatientHelper DB Scan", alertMessage);
				return (0);
			}else{
				//delete these pending unconfirmed appts
				for (int j = 0; j < rows.length; ++j) {
					apptId = rows [j][0];
					if (deleteUnconfirmedAppt(apptId)){
						total_deleted_appt = total_deleted_appt + 1;
					}
				}
			}
		}	
		logger.info("Have deleted total of ..."+ total_deleted_appt + " unconfirmed appointments");
		return (total_deleted_appt);
	}	
	


	/**
	*	Delete an unpaid appt and save a log for the deletion
	*/
	static private boolean deleteUnpaidAppt(String apptId) throws Exception
	{

		AppointmentInfo appointmentInfo = new AppointmentInfo();
		if (appointmentInfo.read (apptId) == false) {
			logger.debug ("Failed to read info for pending appointment \"" + apptId + "\" when try to delete it");
			return(false);
		}

		// Delete the appt record if it is pending for payment
		if(appointmentInfo.apptStatus.equalsIgnoreCase("1")){
			if (appointmentInfo.destroy (apptId) == false) {
				logger.debug ("Failed to delete appointment \"" + apptId + "\" when cancel on reminder");
				return(false);
			}
		}
  		// inser call record after successful pending appt cancellation
  		CallRecordLog callRecordLog = new CallRecordLog();
		callRecordLog.logCallRecord (
			appointmentInfo.doctorId,	//doctorId
			"CancelUnpaid",					//callType
			String.valueOf(TimeHelper.currentEpochSecond()),	// callStartTime
			"none",				//patientPhone
			String.valueOf(appointmentInfo.patientId),			//patientId
			"none",						//transferredToPhone
			String.valueOf(appointmentInfo.epochDay),		//apptEpochDay
			String.valueOf(appointmentInfo.rangeStart),		//apptStartSecond
			"none",						//errorEventLog
			"success"					//callResult
		);
		//Email patient about unpaid appt being deleted
		NotifyHelper.cancelUnpaidAppt (appointmentInfo);
		logger.debug ("Logged a pending appt delete for appointment id \"" + apptId + "\"");
		return true;
	}
	
	/**
	*	Delete an unConfirmed appt and save a log for the deletion
	*/
	static private boolean deleteUnconfirmedAppt(String apptId) throws Exception
	{

		AppointmentInfo appointmentInfo = new AppointmentInfo();
		if (appointmentInfo.read (apptId) == false) {
			logger.error ("Failed to read info for pending appointment \"" + apptId + "\" when try to delete it");
			return(false);
		}
		String patientId = appointmentInfo.patientId;
		// Delete the appt record if it is pending for confirmation
		if(appointmentInfo.apptStatus.equalsIgnoreCase("2")){
			NotifyHelper.unconfirmedApptCancelled(appointmentInfo);
			if (appointmentInfo.destroy (apptId) == false) {
				logger.error ("Failed to delete appointment \"" + apptId + "\" when cancel on reminder");
				return(false);
			}
			//Delete patient record if it is pending
			PatientRecord patientRecord = new PatientRecord();
			boolean isRecordOK = patientRecord.readById(patientId);
			if((patientRecord.isRestricted.equals("2")) && (isRecordOK)){
				multiDestroy(patientId);
				logger.info("removed pending record patientId, sending email..." + patientId);
			}
		}
  		//Will not add call record for unconfirmed appt as patient Record is removed too
		return true;
	}	

	/**
	 * This method updates the activity status of patients.
	 *
	 * @param doctorId	The ID of the doctor whose patients are updated.
	 */
	static public void setActiveStatus (String doctorId) throws Exception
	{
		int today;
		String [][] rows;
		String [] patients;
		int i;
		HashMap mapActive;

		today = TimeHelper.currentEpochDay ();
		/* select patientId who has appt yesterday.
		rows = SqlQuery.query (
			"SELECT patientId " +
			"FROM scheduleDays INNER JOIN scheduleAppt USING (scheduleDayId) " +
			"WHERE " +
				"doctorId='" + SqlQuery.escape (doctorId) + "' AND " +
				"epochDay='" + SqlQuery.escape (String.valueOf (today - 1)) + "' " +
			"GROUP BY patientId"
		);		*/
		// select all patients for the doctor.
		rows = SqlQuery.query (
			"SELECT patientId " +
			"FROM patients WHERE " +
				"doctorId='" + SqlQuery.escape (doctorId) + "'"
		);
		patients = new String [rows.length];
		for (i = 0; i < rows.length; ++i) {
			patients [i] = rows [i][0];
		}
		for (i = 0; i < patients.length; ++i) {
			rows = SqlQuery.query (
				"SELECT epochDay " +
				"FROM scheduleDays INNER JOIN scheduleAppt USING (scheduleDayId) " +
				"WHERE " +
					"patientId='" + SqlQuery.escape (patients [i]) + "' AND " +
					"epochDay >= " + SqlQuery.escape (String.valueOf (today)) + " " +
				"ORDER BY epochDay ASC " +
				"LIMIT 1"
			);
			if (rows.length > 0) {
				SqlQuery.query (
					"UPDATE patients " +
					"SET latestActivityDay='" + SqlQuery.escape (rows [0][0]) + "' " +
					"WHERE patientId='" + patients [i] + "'"
				);
			}
		}
		rows = SqlQuery.query (
			"SELECT patientId " +
			"FROM patients " +
			"WHERE doctorId='" + SqlQuery.escape (doctorId) + "' " +
			"ORDER BY " +
				"latestActivityDay >= " + SqlQuery.escape (String.valueOf (today)) + " DESC, " +
				"ABS(latestActivityDay - " + SqlQuery.escape (String.valueOf (today)) + ") ASC " +
			"LIMIT " + SqlQuery.escape (String.valueOf(getMaxActivePatients()))
		);
		mapActive = new HashMap ();
		for (i = 0; i < rows.length; ++i) {
			mapActive.put (rows [i][0], null);
		}
		rows = SqlQuery.query (
			"SELECT patientId " +
			"FROM patients " +
			"WHERE doctorId='" + SqlQuery.escape (doctorId) + "'"
		);
		for (i = 0; i < rows.length; ++i) {
			SqlQuery.query (
				"UPDATE patients " +
				"SET isActive='" + (mapActive.containsKey (rows [i][0]) ? "1" : "0") + "' " +
				"WHERE patientId='" + SqlQuery.escape (rows [i][0]) + "'"
			);
		}
	}

	/**
	* This method returns patient text link for SMS setting
	* which allows patient to signup to receive Text message
	* reminder
	*/
	public static String getSMSUrl(String patientId) throws Exception {
		String base = SettingsHelper.readString("SMSUrl", SettingsHelper.NetworkDataSettings);
		patientId = CryptoHelper.encrypt(patientId);
		patientId = URLEncoder.encode(patientId, "UTF-8");
		return MessageFormat.format(base, patientId);
	}

	/**
	* This method returns patient text link for open appt 
	* which allows patient to confirm the appt
	* from link included in email
	*/
	public static String getOpenSchedulerApptUrl(
		String patientId, 
		String apptId, 
		String linkType
	) throws Exception {
		
		String base = null;
		if (linkType.equals("ConfirmAppt")){
			base = SettingsHelper.readString("OpenApptConfirmationUrl", SettingsHelper.NetworkDataSettings);
		}else if (linkType.equals("CancelAppt")){
			base = SettingsHelper.readString("OpenApptCancellationUrl", SettingsHelper.NetworkDataSettings);
		}else if (linkType.equals("DoNotContact")){
			base = SettingsHelper.readString("OpenPatientInfoUrl", SettingsHelper.NetworkDataSettings);			
		} else {
			logger.error("No linkType is provided for open scheduler link encryption");
			return (null);
		}
		
		//logger.info("encrypting patientId " + patientId);
		
		String patientIdCode = CryptoHelper.encrypt(patientId);
		patientIdCode = URLEncoder.encode(patientIdCode, "UTF-8");

		//logger.info("encrypting apptId " + apptId);
		String apptIdCode = CryptoHelper.encrypt(apptId);
		apptIdCode = URLEncoder.encode(apptIdCode, "UTF-8");
		
		return MessageFormat.format(base, patientIdCode, apptIdCode);
	}
	
		
	/**
	* This method set/reset a patient's future appt sms status to
	* reflect it's current service profile.
	*/
	public static void updateApptSMSStatus(PatientRecord pr) throws Exception {
		int ced = TimeHelper.currentEpochDay();
		String newValue = pr.isSMSActive() ? "1" : "0";
		String[][] ids = SqlQuery.query("SELECT scheduleApptId " +
						"FROM scheduleAppt sa INNER JOIN scheduleDays sd ON sa.scheduleDayId = sd.scheduleDayId " +
						"WHERE patientId = " + pr.patientId + " AND epochDay >= " + ced);
		for(String[] id : ids) {
			ScheduleApptRecord sar = new ScheduleApptRecord();
			sar.readById(id[0]);
			sar.requireSMS = newValue;
			logger.debug("set ScheduleApptRecord "+ id[0] +" to status.."+ newValue);
			sar.writeById(sar.scheduleApptId);
		}
	}
	
	/**
	* This method create a pending patient record.
	* Pending patient can only make one appt until
	* record is approved and the status turn permanent
	* patientType	=signup (include name, phone, email)
	* 				= unknown (include only email)
	*               = openScheduler (include name, phone, email)
	*/
	
	public static String createPendingPatientRecord(
			String doctorId,
			String patientType,
			String firstName,
			String middleName,
			String lastName,
			String email,
			String phone,
			MessagesInline messages 
	){			
		try {	
				PatientRecord newPatientRecord = new PatientRecord ();
				newPatientRecord.doctorId = doctorId;
				newPatientRecord.firstName = firstName;
				newPatientRecord.middleName = middleName;
				newPatientRecord.lastName = lastName;
				newPatientRecord.reminderType = "3";
				newPatientRecord.language = "EN";
				newPatientRecord.checkInNotes ="";// aForm.getString ("checkInNotes");
				newPatientRecord.street = "";
				newPatientRecord.city = "";
				newPatientRecord.state = "";
				newPatientRecord.zip = "";
				newPatientRecord.phone = phone;
				newPatientRecord.phone = PhoneHelper.normalize (newPatientRecord.phone, false);
				if ((patientType.equals("unknown")==false )&&(newPatientRecord.phone == null)){
					messages.addGenericMessage ("error.invalid.phone");
					return null;
				}
				newPatientRecord.phone2 = "";
				newPatientRecord.smsCarrier = "";
				newPatientRecord.email = email;
				newPatientRecord.isActive = "1";
				// new record is set to pending until confirmed, 0/1=not/restricted, 2=pending
				newPatientRecord.isRestricted = "2";
				newPatientRecord.latestActivityDay = String.valueOf(TimeHelper.currentEpochDay ());
				newPatientRecord.createdDay = String.valueOf(TimeHelper.currentEpochDay ());
				String patientId = newPatientRecord.create();
				return patientId;
			} catch (Exception ex) {
				logger.error ("Receive exception "+ ex.getMessage ());
				return null;
		}
	}
	
	
	/** 
	 * This method creates a temp user account for telephone caller
	 * from a given phone number. It saves the input phone number and uses
	 * "Temp_Record" as the patient's first name field. This indicator
	 * will be used later to inform speech and GUI app that the record
	 * is temporary. The other fields in the patient temp record are blank.
	 *
	 * @return		Return the patient ID of the new account.
	 *
	 * @param phone		Patient's phone number.
	 */

	public static int createTempUserAccount (String phone, String doctorId)
	{
		try {
			PatientRecord patientRecord = new PatientRecord ();
			String patientId;
            String normalizePhone = PhoneHelper.normalize(phone, false);
			patientRecord.doctorId = doctorId;
			/*
			if temp record already exist for the same phone and doctorId
			then do not add new record
			*/
			if (patientRecord.readByFilter (new String [][] {
				{"doctorId", doctorId},
				{"firstName", "Temp_Record"},
				{"phone", normalizePhone}
			})) {
				return (0);
			}
			//The first name field is used to indicate a temp record
			patientRecord.firstName = "Temp_Record";
			patientRecord.middleName = "";
			patientRecord.lastName = "";
			patientRecord.ssn = "";
			patientRecord.street = "";
			patientRecord.city = "";
			patientRecord.state = "";
			patientRecord.zip = "";
			patientRecord.phone = normalizePhone;
			if (patientRecord.phone == null){
				return (0);
			}
			patientRecord.email = "";
			patientRecord.isActive = "1";
			patientRecord.latestActivityDay = String.valueOf(TimeHelper.currentEpochDay());
			patientRecord.createdDay = String.valueOf(TimeHelper.currentEpochDay());
			//Reminder by Telephone
			patientRecord.reminderType="3"; //Both phone and email
			//patientId = PatientHelper.multiCreate (patientRecord);
			patientId = patientRecord.create ();
			logger.info("createTempUserAccount for phone..."+phone+ " getting patientId..."+ patientId);
			return (Integer.parseInt (patientId));
		} catch (Exception ex) {
			logger.error ("Receive exception "+ ex.getMessage ());
			return (0);
		}
	}

	
	/**
	 * Return an array of patient IDs and first names for patients that have a matching phone number.
	 * More than one match is possible as same phone number can be used by multiple patients (as is
	 * the case where people from the same family seeing one doctor).
	 *
	 * @return		Return an array of patient records.
	 *              Each record is a string array containing
	 *			    patient ID and patient first name.
	 *              patients[i][0]=patientId, patients[i][1]=firstName, patients[i][2]=lastName
	 * @param phone		Patient's phone number.
	 */
	public static String [][] getPatientIds (String phone, String doctorId)
	{
		String [][] rows;
		String phoneSearch, phonePatient;
		Vector result;
		int i;

		try { 
		// normalize phone number if input exist, or else use number as-is

		String normalizedPhone = PhoneHelper.normalize(phone, false);
		if (normalizedPhone == null ){
			normalizedPhone = phone;
		}
		rows = SqlQuery.query (
			"SELECT patientId, firstName, lastName, phone " +
			"FROM patients " +
			"WHERE " +
				"(doctorId='" + SqlQuery.escape (doctorId) + "') AND " +
				SqlHelper.searchTerm ("phone", normalizedPhone)
		);
		// if first search produce no match, try by phone2
		if (rows.length == 0) {
			rows = SqlQuery.query (
			"SELECT patientId, firstName, lastName, phone2 " +
			"FROM patients " +
			"WHERE " +
				"(doctorId='" + SqlQuery.escape (doctorId) + "') AND " +
				SqlHelper.searchTerm ("phone2", normalizedPhone)
			);
		}
		if ((phoneSearch = PhoneHelper.normalize (phone, true)) == null) {
			return (new String [0][3]);
		}
		result = new Vector ();
		
		/**
		Add patient into result vector if phone number OK
		Filter the patient names to be TTS readable
		*/
		for (i = 0; i < rows.length; ++i) {
			if ((phonePatient = PhoneHelper.normalize (rows [i][3], false)) != null) {
				if (
					phoneSearch.endsWith (phonePatient) ||
					phonePatient.endsWith (phoneSearch)
				) {
					result.add (new String [] {rows [i][0], MiscHelper.parseForTTS(rows [i][1]), MiscHelper.parseForTTS(rows [i][2])});
				}
			}
		}
		return ((String [][]) result.toArray (new String [0][0]));

		} catch (Exception ex) {
       	logger.error ("Catching exception "+ ex.getMessage ());
		return null;
       	}
	}
	
	/**
	*  Match patient email with doctor's phone to see if
	*  patient is pending (normal = 0, restricted =1, pending = 2)
	*/

	public static boolean isPendingPatient(String doctorPhone, String patientEmail) throws Exception
	{
		String [][] rows;
		int i;

		rows = SqlQuery.query (
			"SELECT doctors.doctorId, patientId " +
			"FROM doctors INNER JOIN patients USING (doctorId) " +
			"WHERE " +
				"(" +
					"bizPhone='" + SqlQuery.escape (doctorPhone) + "' OR " +
					"selfServicePhone='" + SqlQuery.escape (doctorPhone) + "' " +
				") AND " +
				"patients.email='" + SqlQuery.escape (patientEmail.trim()) + "' AND " +
				"patients.isRestricted='2' AND " +
				"hasWebAppt"
		);
		if (rows.length > 0) {
			logger.info("failed patient login for doctor phone..."+ doctorPhone + " and Patient email..."+patientEmail);
			return true;
		}else{
			return false;
		}
		
	}		
	/**
	* Return patient service status (0 = ok, 1 = restricted, 2 = pending)
	*
	*/

	public static String getPatientServiceStatus(String patientId, String doctorId)
	{
		String [][] rows;
		try {
			rows = SqlQuery.query (
				"SELECT isRestricted " +
				"FROM patients " +
				"WHERE " +
					"doctorId='" + SqlQuery.escape (doctorId) + "' AND " +
					"patientId='" + SqlQuery.escape (patientId) + "'"
			);
			String status = rows [0][0];
			logger.info ("patient status is ..." + status);
			return status;
		} catch (Exception ex) {
			logger.error("catch Exception ...\n" + ex.toString());
			return null;
		}// end of try
	}

	/**
	 * Return a list of patient appointments. Only future appointments are returned.
	 *
	 * @return		Return an array containing three vectors.
	 *			v [0] is a vector containing appointment time strings in outputTimeFormat format.
	 *			v [1] is a vector containing appointment date strings in outputDateFormat format.
	 *			v [2] is a vector containing appointment IDs.
	 * @param patientId	Patient ID
	 */
	public static Vector [] getPatientAppts (int patientId, String doctorId)
	{
		String [][] rows;
		Vector apptTimes, apptDates, apptId;
		int currentDay, currentSeconds, i, itemDay, itemSeconds;

		try { 
			currentSeconds = TimeHelper.currentEpochSecond ();
			currentDay = currentSeconds / 86400;
			currentSeconds %= 86400;
			rows = SqlQuery.query (
				"SELECT scheduleApptID, epochDay, slotSize, slotStart " +
				"FROM scheduleAppt INNER JOIN scheduleDays USING (scheduleDayId) " +
				"WHERE " +
					"doctorId='" + SqlQuery.escape (doctorId) + "' AND " +
					"patientId='" + SqlQuery.escape (String.valueOf (patientId)) + "' AND " +
					"epochDay >= '" + SqlQuery.escape (String.valueOf (currentDay)) + "'"
			);
			//logger.info("have found "+ rows.length +" active appt for patientId " + patientId );
			apptTimes = new Vector ();
			apptDates = new Vector ();
			apptId = new Vector ();
			for (i = 0; i < rows.length; ++i) {
				itemDay = Integer.parseInt (rows [i][1]);
				itemSeconds = Integer.parseInt (rows [i][2]) * Integer.parseInt (rows [i][3]);
				if (
					((itemDay == currentDay) && (itemSeconds >= currentSeconds)) ||
					(itemDay > currentDay)
				) {
					apptTimes.add (TimeHelper.daySecondsToString (itemSeconds));
					apptDates.add (TimeHelper.epochDayToString (itemDay));
					apptId.add (new Integer (rows [i][0]));
				}
			}
			return (new Vector [] {apptTimes, apptDates, apptId});
		} catch (Exception ex) {
			logger.error("catch Exception ...\n" + ex.toString());
			return null;
		}// end of try
	}

	/**
	 * Return a list of patient active and history appointments.
	 *
	 * @return		Return an array containing three vectors.
	 *			v [0] is a vector containing appointment time strings in outputTimeFormat format.
	 *			v [1] is a vector containing appointment date strings in outputDateFormat format.
	 *			v [2] is a vector containing appointment IDs.
	 *
	 * @param notes			Appointment notes used to identify demo user in the
	 *						format of "demoCaller_phoneNumber",
	 *						Example "D_2145376788", where the phone
	 *						digits are given by demo caller as ID.
	 * @param patientId		Patient ID
	 */
	public static Vector [] getDemoUserAppts (String idNotes,int patientId, String doctorId)
	{
		String [][] rows;
		Vector apptTimes, apptDates, apptId;
		int currentDay, currentSeconds, i, itemDay, itemSeconds;

		try { // catch exception here
			currentSeconds = TimeHelper.currentEpochSecond ();
			currentDay = currentSeconds / 86400;
			currentSeconds %= 86400;

			rows = SqlQuery.query (
				"SELECT scheduleApptID, epochDay, slotSize, slotStart " +
				"FROM scheduleAppt INNER JOIN scheduleDays USING (scheduleDayId) " +
				"WHERE " +
					"doctorId='" + SqlQuery.escape (doctorId) + "' AND " +
					"patientId='" + SqlQuery.escape (String.valueOf (patientId)) + "' AND " +
					"notes='" + SqlQuery.escape (idNotes) + "'"

					/** code restrict search on active appt only
					"notes='" + SqlQuery.escape (idNotes) + "' AND " +
					"epochDay >= '" + SqlQuery.escape (String.valueOf (currentDay)) + "'"
					*/
			);
			apptTimes = new Vector ();
			apptDates = new Vector ();
			apptId = new Vector ();
			for (i = 0; i < rows.length; ++i) {
				itemDay = Integer.parseInt (rows [i][1]);
				itemSeconds = Integer.parseInt (rows [i][2]) * Integer.parseInt (rows [i][3]);
				
				apptTimes.add (TimeHelper.daySecondsToString (itemSeconds));
				apptDates.add (TimeHelper.epochDayToString (itemDay));
				apptId.add (new Integer (rows [i][0]));
				/** code restrict search on active appt only
				if (
					((itemDay == currentDay) && (itemSeconds >= currentSeconds)) ||
					(itemDay > currentDay)
				) {
					apptTimes.add (TimeHelper.daySecondsToString (itemSeconds));
					apptDates.add (TimeHelper.epochDayToString (itemDay));
					apptId.add (new Integer (rows [i][0]));
				}
				*/
			}
			return (new Vector [] {apptTimes, apptDates, apptId});
		} catch (Exception ex) {
			logger.error("catch Exception ...\n" + ex.toString());
			return null;
		}// end of try

	}

	/**
	 * Return a list of patient active appointments.
	 *
	 * @return		Return an array containing three vectors.
	 *			v [0] is a vector containing appointment time strings in outputTimeFormat format.
	 *			v [1] is a vector containing appointment date strings in outputDateFormat format.
	 *			v [2] is a vector containing appointment IDs.
	 *
	 * @param notes			Appointment notes used to identify demo user in the
	 *						format of "D_phoneNumber",
	 *						Example "D_2145376788", where the phone
	 *						digits are given by demo caller as ID.
	 * @param patientId		Patient ID
	 */
	public static Vector [] getDemoUserActiveAppts (String idNotes, int patientId, String doctorId)
	{
		String [][] rows;
		Vector apptTimes, apptDates, apptId;
		int currentDay, currentSeconds, i, itemDay, itemSeconds;

		try { // catch exception here
			currentSeconds = TimeHelper.currentEpochSecond ();
			currentDay = currentSeconds / 86400;
			currentSeconds %= 86400;

			rows = SqlQuery.query (
				"SELECT scheduleApptID, epochDay, slotSize, slotStart " +
				"FROM scheduleAppt INNER JOIN scheduleDays USING (scheduleDayId) " +
				"WHERE " +
					"doctorId='" + SqlQuery.escape (doctorId) + "' AND " +
					"patientId='" + SqlQuery.escape (String.valueOf (patientId)) + "' AND " +
					"notes='" + SqlQuery.escape (idNotes) + "' AND " +
					"epochDay >= '" + SqlQuery.escape (String.valueOf (currentDay)) + "'"
			);
			apptTimes = new Vector ();
			apptDates = new Vector ();
			apptId = new Vector ();
			for (i = 0; i < rows.length; ++i) {
				itemDay = Integer.parseInt (rows [i][1]);
				itemSeconds = Integer.parseInt (rows [i][2]) * Integer.parseInt (rows [i][3]);

				if (
					((itemDay == currentDay) && (itemSeconds >= currentSeconds)) ||
					(itemDay > currentDay)
				) {
					apptTimes.add (TimeHelper.daySecondsToString (itemSeconds));
					apptDates.add (TimeHelper.epochDayToString (itemDay));
					apptId.add (new Integer (rows [i][0]));
				}
			}
			return (new Vector [] {apptTimes, apptDates, apptId});
		} catch (Exception ex) {
			logger.error("catch Exception ...\n" + ex.toString());
			return null;
		}

	}


	/**
	 * Return a list of patient appointments for a given day and corresonding patient info.
	 *
	 * @return	Return an array containing three vectors.
	 *		v [0] is a vector containing patient phone numbers in string format.
	 *		v [1] is a vector containing patient first and last names in string format.
	 *		v [2] is a vector containing appointment start times in outputTimeFormat string format.
	 * @param date	Appointment date in inputDateFormat format.
	 */
	public static Vector [] getReminderCallList (String date, String doctorId)
	{
		int epochDay;
		String [][] rows;
		Vector apptPhones, apptNames, apptTimes;
		int i;
	try{
		if ((epochDay = TimeHelper.stringToEpochDay (date)) == -1) {
			return (null);
		}
		rows = SqlQuery.query (
			"SELECT slotSize, slotStart, phone, firstName, lastName " +
			"FROM scheduleAppt, scheduleDays, patients " +
			"WHERE " +
				"scheduleAppt.scheduleDayId = scheduleDays.scheduleDayId AND " +
				"scheduleAppt.patientId = patients.patientId AND " +
				"scheduleDays.doctorId='" + SqlQuery.escape (doctorId) + "' AND " +
				"epochDay='" + SqlQuery.escape (String.valueOf (epochDay)) + "'"
		);
		apptPhones = new Vector ();
		apptNames = new Vector ();
		apptTimes = new Vector ();
		for (i = 0; i < rows.length; ++i) {
			apptPhones.add (rows [i][2]);
			apptNames.add (rows [i][3] + " " + rows [i][4]);
			apptTimes.add (TimeHelper.daySecondsToString (
				Integer.parseInt (rows [i][0]) * Integer.parseInt (rows [i][1])
			));
		}
		return (new Vector [] {apptPhones, apptNames, apptTimes});
		} catch (Exception ex) {
       		logger.error("catch Exception ...\n" + ex.toString());
			return (null);
       	}
	}
	
			
	
}
