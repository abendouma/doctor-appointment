package net.angelspeech.object;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.angelspeech.object.MessagesInline;
import net.angelspeech.object.SessionDoctor;
import net.angelspeech.object.SuperuserHelper;
import net.angelspeech.object.ApptSearchHelper;

import net.angelspeech.database.PatientRecord;
import net.angelspeech.database.DoctorRecord;
import net.angelspeech.database.SuperuserRecord;

import org.apache.log4j.Logger;
import org.apache.struts.validator.DynaValidatorForm;

/**
 * This class contains helper functions which login doctors/superusers.
 */
public class LoginHelper{
	
	static private Logger logger = Logger.getLogger (LoginHelper.class);
	static final public int LOGIN_SUCCESS = 1;
	static final public int LOGIN_FAILED = 2;
	static final public int LOGIN_CONTINUE = 3;
	static final public int LOGIN_AS_PREPAID = 4;
	
	
	
	/**
	 * This method logins a doctor. It starts an HTTP session, populates the browser cache matrix, 
	 * starts the periodic update and
	 * prepares request attributes for the main frame page.
	 *
	 * @param doctorId		The ID of the doctor that will be logged in.
	 * @param maximizeWindow	A boolean flag which is true iff we want to maximize window on login.
	 * @param request		The HTTP request we are processing
	 * @param messages		The MessagesInline object which will hold system messages for the js framework.
	 */
	static public void loginDoctor (
		String doctorId, 
		boolean maximizeWindow, 
		HttpServletRequest request, 
		MessagesInline messages
	) throws Exception
	{
		SessionDoctor.start (request, null, doctorId);
		messages.setActiveId (null, doctorId);
		messages.resetCalendar ();
		messages.deleteAll ();
		messages.setDoctor (doctorId);
		messages.setLocationAll (doctorId);
		messages.setAppointmentProfileAll (doctorId);
		messages.setPatientAll (doctorId);
		messages.setSchedule (doctorId, -1, null);
		messages.updateStart ();
		request.setAttribute ("loginType", new Integer (1));
		request.setAttribute ("loginMaximize", new Boolean (maximizeWindow));
	}

	/**
	 * This method logins a superuser. It starts an HTTP session, 
	 * populates the browser cache matrix, starts the periodic update and
	 * prepares request attributes for the main frame page.
	 *
	 * @param superuserId		The ID of the superuser that will be logged in.
	 * @param maximizeWindow	A boolean flag which is true iff we want to maximize window on login.
	 * @param request		The HTTP request we are processing
	 * @param messages		The MessagesInline object which will hold system messages for the js framework.
	 */
	static public void loginSuperuser (
		String superuserId,
		boolean maximizeWindow,
		HttpServletRequest request,
		MessagesInline messages
	) throws Exception
	{
		String [] accounts;
		int i;

		SessionDoctor.start (request, superuserId, null);
		messages.setActiveId (superuserId, null);
		messages.resetCalendar ();
		messages.deleteAll ();
		messages.setSuperuser (superuserId);
		//login only those doctors with active billing status
		accounts = SuperuserHelper.getLinkedActiveDoctors (superuserId);
		for (i = 0; i < accounts.length; ++i) {
			messages.setDoctor (accounts [i]);
			messages.setLocationAll (accounts [i]);
			messages.setAppointmentProfileAll (accounts [i]);
			messages.setPatientAll (accounts [i]);
			messages.setSchedule (accounts [i], -1, null);
		}
		messages.updateStart ();
		request.setAttribute ("loginType", new Integer (2));
		request.setAttribute ("loginMaximize", new Boolean (maximizeWindow));
	}
	
	/**
	 * This method logins a patient. It starts an HTTP session, 
	 * populates the browser cache matrix, 
	 * prepares request attributes for the main frame page.
	 * It checks if doctor has "new patient/customer"
	 * appt profile first. If yes, patient will go directly
	 * into new patient/customer appt page. or else into a
	 * regular weekly appt page.
	 *
	 * @param patientRecord		The record of the patient that will be logged in.
	 * @param doctorId		the doctor to which the patient belongs.
	 * @param request		The HTTP request we are processing
	 * @param messages		The MessagesInline object which will hold system messages for the js framework.	
	*/
	static public void loginPatient(
			HttpServletRequest request, 
			DoctorRecord doctorRecord, 
			MessagesInline messages,
			PatientRecord patientRecord, 
			String patientId
	) throws Exception {
		String [] doctors, patients;
		int i;
		String maximizeField;
		logger.info("auto login pending new patientId..." + patientId);
		doctors = new String [1];
		doctors[0]= doctorRecord.doctorId;
		patients = new String [1];
		patients [0] = patientId;

		//setting session patient to session
		logger.debug("Setting session patient...");
		SessionPatient.start (
			request,
			null,
			doctors,
			patients
		);

		messages.setActiveId (null, (doctors.length == 1) ? doctors [0] : null);
		messages.resetCalendar ();
		messages.deleteAll ();
		for (i = 0; i < doctors.length; ++i) {
			messages.setDoctor (doctors [i]);
			messages.setAppointmentProfileAll (doctors [i]);
			messages.setSchedule (doctors [i], -1, patients);
		}

		// activate patient in case it is inactive
		for (i = 0; i < patients.length; ++i) {
			patientRecord.readById (patients [i]);
			patientRecord.isActive = "1";
			PatientHelper.multiUpdate (patientRecord);
			messages.setPatientOne (patientRecord.doctorId, patients [i]);
		}
		// search new patient appt profile id and login to new patient appt page.		
		Integer loginType;
		if (doctorRecord.hasCustomizeAppt.equals("0")){
			// new Integer (1) for doctor's weekly page
			loginType = new Integer (1);
		}else{
			//doctor scheduler has customized appt
			String newPatientApptProfileId = ApptSearchHelper.getNewPatientApptProfileId (doctorRecord.doctorId);
			if ((newPatientApptProfileId == null)||(doctorRecord.newPatient.equals("4"))){
				loginType = new Integer (1);
			}else{
				loginType = new Integer (Integer.parseInt(newPatientApptProfileId));
			}
		}
		request.setAttribute ("loginType", loginType);
		logger.info("Login Helper login using loginType..." + loginType);
		request.setAttribute ("loginMaximize", new Boolean (true));
	}

	
	
	
	/**
	 * Execute doctor login operation. Method check if doctor login/pwd is correct. 
	 * If doctor is accepted, then loginDoctor method is called. 
	 * 
	 * @param username
	 * @param password
	 * @param maximize
	 * @param request
	 * @param response
	 * @param messages
	 * @return one of following state (LOGIN_SUCCESS, LOGIN_FAILED,LOGIN_CONTINUE,LOGIN_AS_PREPAID). 
	 * Action should do approptiate forwarding
	 * @throws Exception
	 */
	public static int doDoctorLogin(
		String username, 
		String password,
		String maximizeField,
		HttpServletRequest request, 
		HttpServletResponse response,
		MessagesInline messages
	) throws Exception {
		
		DoctorRecord doctorRecord = new DoctorRecord();
		String decryptedPassword;

		if (doctorRecord.readByFilter(new String[][] { { "username",
				username.trim() } }) == false) {
			return (LOGIN_CONTINUE);
		}
		
		decryptedPassword = CryptoHelper.decrypt(doctorRecord.password);
		if (decryptedPassword.equals(password) == false) {
			messages.addGenericMessage("error.failed.login");
			return (LOGIN_FAILED);
		}
		if (doctorRecord.active.equals("0")) {
			messages.addGenericMessage("error.failed.suspended");
			return (LOGIN_FAILED);
		}
		
		HttpHelper.restoreDefaultStyle(request, response,
				doctorRecord.defaultTheme);

		LoginHelper.loginDoctor(doctorRecord.doctorId,
				((maximizeField != null) && maximizeField.equals("on")),
				request, messages);
		
		//if doctor is prepaid we return special code
		if ("1".equals(doctorRecord.accountType)) {
			return LoginHelper.LOGIN_AS_PREPAID;
		}
		
		return (LOGIN_SUCCESS);
	}
	
	
	/**
	 * Execute super user login operation. Method check if super user login/pwd is correct. 
	 * If doctor is accepted, then loginSuperUser method is called. 
	 *
	 * @param username
	 * @param password
	 * @param maximizeField
	 * @param request
	 * @param messages
	 * @return
	 * @throws Exception
	 */
	public static int doSuperuserLogin(
		String username, 
		String password,
		String maximizeField, 
		HttpServletRequest request,
		MessagesInline messages
	) throws Exception {

		SuperuserRecord superuserRecord = new SuperuserRecord();
		String decryptedPassword;

		if (superuserRecord.readByFilter(new String[][] { 
			{ "username", username.trim() } 
		}) == false) {
			return (LoginHelper.LOGIN_CONTINUE);
		}
		decryptedPassword = CryptoHelper.decrypt(superuserRecord.password);
		if (decryptedPassword.equals(password) == false) {
			messages.addGenericMessage("error.failed.login");
			return (LoginHelper.LOGIN_FAILED);
		}

		LoginHelper.loginSuperuser(superuserRecord.superuserId,
				((maximizeField != null) && maximizeField.equals("on")),
				request, messages);
		return (LoginHelper.LOGIN_SUCCESS);
	}
	
	
	/**
	 * generate parameters for prepaid user internal login operation
	 * @param login
	 * @param pwd
	 * @param activeDoctorId optional param
	 * @return string with concatenated parameters
	 */
	public static String generateLoginUrlParameters(
		String login, 
		String pwd, 
		String activeDoctorId
	){
		
		String res="";
		res+="?username="+ login;
		res+="&password="+ pwd;

		if(activeDoctorId!=null){
			//logger.debug("generateLoginUrlParameters using doctorId..."+ activeDoctorId);
			res+="&doctorId="+ activeDoctorId;			
		}
	
		return res;		
	}
}
