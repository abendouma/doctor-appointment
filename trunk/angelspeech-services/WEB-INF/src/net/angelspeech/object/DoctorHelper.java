package net.angelspeech.object;

import java.net.URLEncoder;
import java.text.MessageFormat;

import net.angelspeech.database.ApptProfileRecord;
import net.angelspeech.database.DoctorRecord;
import net.angelspeech.database.SqlQuery;
import net.angelspeech.object.SqlHelper;

public class DoctorHelper {	
	
/**
* This method provide email customization link with encrypted doctorId as parameters
*/
	public static String getTemplateCustomizationUrl(String doctorId) throws Exception {
		String base = SettingsHelper.readString("TemplateCustomizationUrl", SettingsHelper.NetworkDataSettings);
		doctorId = CryptoHelper.encrypt(doctorId);
		doctorId = URLEncoder.encode(doctorId, "UTF-8");
		return MessageFormat.format(base, doctorId);
	}
	

/**
* This method provide doctor's patients link to answer customized form
* with encrypted doctorId, apptId as parameters
*/
	public static String getApptFormUrl(String doctorId, String apptId) throws Exception {
		String base = SettingsHelper.readString("ApptFormUrl", SettingsHelper.NetworkDataSettings);
		
		String doctorIdCode = CryptoHelper.encrypt(doctorId);
		doctorIdCode = URLEncoder.encode(doctorIdCode, "UTF-8");

		String apptIdCode = CryptoHelper.encrypt(apptId);
		apptIdCode = URLEncoder.encode(apptIdCode, "UTF-8");
		
		return MessageFormat.format(base, doctorIdCode,apptIdCode);
	}
	
			
/**
* This method provide doctor form customization link with encrypted doctorId and apptProfileId as parameters
*/
	public static String getFormCustomizationUrl(String doctorId, String apptProfileId) throws Exception {
		String base = SettingsHelper.readString("FormCustomizationUrl", SettingsHelper.NetworkDataSettings);
		
		String doctorIdCode = CryptoHelper.encrypt(doctorId);
		doctorIdCode = URLEncoder.encode(doctorIdCode, "UTF-8");

		String apptProfileIdCode = CryptoHelper.encrypt(apptProfileId);
		apptProfileIdCode = URLEncoder.encode(apptProfileIdCode, "UTF-8");
		
		return MessageFormat.format(base, doctorIdCode,apptProfileIdCode);
	}
	
	
	/**
	 * Indicate IF custom form should be used for new patient signup 
	 * FIXME: it should check if question form exists at all for the apptProfileId
	 * (doctor.newPatient && doctor.hasCustomizeForm && apptProfile.hasQuestionForm set to "1")
	 * @param doctorId
	 * @return
	 * @throws Exception
	 */
	public static boolean useCustiomizedFormForSignup(String doctorId) throws Exception{
		String apptProfileId=getDoctorNewPatientProfile(doctorId);
		
		boolean res=false;
		DoctorRecord doctor=new DoctorRecord();
		doctor.readById(doctorId);
		ApptProfileRecord apptProfile=new ApptProfileRecord();
		apptProfile.readById(apptProfileId);
	
		if(("1".equals(doctor.newPatient)||"2".equals(doctor.newPatient)) && "1".equals(doctor.hasCustomizeForm) && "1".equals(apptProfile.hasQuestionForm)){
			res=true;
		}		
				
		return res;
	}  
	
/**
* The method search for apptProfileId that contain keywords 
* "new patient" or "new customer" as name
* for a given dictorId
*/

	public static String getDoctorNewPatientProfile(String doctorId) throws Exception {
    	//fetching profiles using doctorId and name as filter
    	  	
		String [][] rows = SqlQuery.query (
			"SELECT " +
				"apptProfileId, " +
				"name " +
			"FROM apptProfile " +
			"WHERE " +
				"doctorId='" + SqlQuery.escape (doctorId) + "'" +
				" AND " +SqlHelper.searchTerm ("selectByPatient", "1") + 
				" AND " +"((name LIKE 'new patient%') OR (name LIKE 'new customer%'))" 
		);    	

    	// return the found id or null    	
		if (rows.length == 0){
			return null;
		}else{
			return (rows [0][0]);
		}
	}

/**
* The method search for apptProfileRecord.name that contain keywords 
* "new patient" or "new customer" as name
* for a given dictorId
*/

	public static String getDoctorNewPatientProfileName(String doctorId) throws Exception {
    	//fetching profiles using doctorId and name as filter
    	  	
		String [][] rows = SqlQuery.query (
			"SELECT " +
				"name " +
			"FROM apptProfile " +
			"WHERE " +
				"doctorId='" + SqlQuery.escape (doctorId) + "' AND" +
				"((name LIKE 'new patient%') OR (name LIKE 'new customer%'))" 
		);    	

    	// return the found id or null    	
		if (rows.length == 0){
			return " ";
		}else{
			return (rows [0][0]);
		}
	}	

/**
* get "patient", or "customer" to based on how doctor is addressed
*
*/ 
	public static String patientOrCustomer(String calledAs) {

		if (calledAs.equals("Dr.")==true){
			return "patient";
		}else{
			return "client";
		}
	}
	
/**
* get office name based on how doctor is addressed
*
*/	
	public static String getOfficeName(DoctorRecord doctorRecord) {

		String officeName;
			if (doctorRecord.calledAs.equals("none")==false){
				officeName = doctorRecord.calledAs + " "
							+ doctorRecord.firstName + " "
							+ doctorRecord.lastName;
			}else{
				officeName = doctorRecord.businessName;
			}

		return officeName;
	}
	
/**
* get business calendar title for single or shared practice
*
*/	
	public static String getCalendarTitle(
		boolean isShared, 
		String calledAs,
		String firstName,
		String lastName,
		String businessName
	) 
	{

		String calendarTitle = "";
		if (isShared) {
			calendarTitle = calendarTitle + businessName;
		}else if (calledAs.equalsIgnoreCase("none")== true){
			calendarTitle = calendarTitle + businessName;
		}else{
			calendarTitle = calendarTitle + calledAs + " " + firstName + " "+ lastName;
		}
		return calendarTitle;
	}	

	/**
	 * return doctor call log balance
	 * @param doctorId
	 * @return
	 * @throws Exception 
	 */
	public static String getDoctorCallLogBalance(String doctorId) throws Exception{
		DoctorRecord doctor=new DoctorRecord();
		doctor.readById(doctorId);
		return doctor.prepaidCallBalance;
	}	
	
}
