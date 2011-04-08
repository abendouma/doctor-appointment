package net.angelspeech.object;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import net.angelspeech.database.DoctorRecord;
import net.angelspeech.database.PatientRecord;
import net.angelspeech.database.SqlQuery;

public class CallLogHelper {


	static private Logger logger = Logger.getLogger(CallLogHelper.class);

	/**
	* This method query database to retrieve daily call log
	* record and prepare the data to be displayed in table
	*/
	public static String [][] getCallInfo (String doctorId, int epochDay) throws Exception
	{
		String [][] rows;
		int i;
		String [][] result;
		int apptEpochDay;
		String apptDay, apptTime, timeZone, patientName;
		DoctorRecord doctorRecord = new DoctorRecord();

		rows = SqlQuery.query (
			"SELECT " +
				"callType, " +
				"callResult, " +
				"patientPhone, " +
				"transferredToPhone, " +
				"callStart, " +
				"apptEpochDay, " +
				"apptStart, " +
				"errorEventLog, " +
				"patientId " +
			"FROM callRecord " +
			"WHERE " +
				"doctorId='" + SqlQuery.escape (doctorId) + "' AND " +
				"callEpochDay='" + SqlQuery.escape (String.valueOf (epochDay)) + "' " +
			"ORDER BY apptStart"
		);
		// get doctor TimeZone to format log time to local time Zone
		doctorRecord.readById(doctorId);
		timeZone = doctorRecord.timeZone;
		result = new String [rows.length][];
		for (i = 0; i < rows.length; ++i) {
			// convert appt time if appt time is available
			apptEpochDay = Integer.parseInt (rows [i][5]);
			if (apptEpochDay != 0){
				apptDay = TimeHelper.epochDayToShortString (apptEpochDay);
				apptTime = TimeHelper.daySecondsToString (Integer.parseInt (rows [i][6]));
			}else{
				apptDay = "none";
				apptTime = "";
			}
			/*
			if error log contain over_10_minutes indictaion then replace
			apptDay and apptTime with "over 10 minutes (1)" for display
			*/
			if (rows[i][7].startsWith("over 10 minutes")){
			int index_parse = rows[i][7].indexOf("_");
				apptDay = "";
				apptTime = rows[i][7].substring(0, index_parse);
			}
			// retrive patient name from patientId or patientPhone
			if (rows[i][0].equalsIgnoreCase("deletePatient")){
				patientName = parsePatientDeleteLogInfo (rows[i][7]);
				logger.debug("parsePatientDeleteLogInfo return name.."+ patientName);
			}else{
				patientName = findPatientName(doctorId, rows [i][8], rows [i][2]);
			}
			// if call Transfer number is "", set it to "none".
			if (rows [i][3].length()==0){
				rows [i][3] = "none";
			}
			result [i] = new String [] {
				rows [i][0],
				rows [i][1],
				rows [i][2],
				patientName,
				rows [i][3],
				// format log time to doctor's local timeZone
				TimeHelper.daySecondsToString (Integer.parseInt (rows [i][4]), timeZone, TimeHelper.outputTimeFormat),
				// if callType="callBack", then errorEventLog contain callBack appt Name
				// otherwise display appt Date and Time
				(
					(rows [i][0].equalsIgnoreCase("callBack")==true) ?
					" " : apptTime

				) +
				(apptDay.equalsIgnoreCase("none") ?  " " : ", ")+
				(
					(rows [i][0].equalsIgnoreCase("callBack")==true) ?
					rows [i][7] : apptDay
				)
			};
		}
		return (result);
	}

	/**
	* This method parse the delete log for patient and retrive patient name
	*
	* String errorEventLog =  "patientId: "+ "%%"+ patientRecord.patientId + "%%" +
								"firstName: " + "%%" + patientRecord.firstName + "%%" +
								"middleName: " + "%%" + patientRecord.middleName + "%%" +
								"lastName: "+ "%%" + patientRecord.lastName + "%%" +
								"phone: " + "%%" + patientRecord.phone + "%%" +
								"phone2: " + "%%" + patientRecord.phone2 + "%%" +
								"email: " + "%%"+ patientRecord.email + "%%";
	*/
	public static  String parsePatientDeleteLogInfo (String compressedList)
	{
		// parse compressedList to a ArraryList<String>
		ArrayList<String> deleteLog = new ArrayList<String>();
		try {
				String notAvailable="NA";
				StringTokenizer st = new StringTokenizer(compressedList, "%%");
				while (st.hasMoreTokens()) {
					String companyName = st.nextToken();
					if (companyName != null) {
						deleteLog.add(companyName);
					}else{
						deleteLog.add(notAvailable);
					}
				}
				//Expand the ArraryList<String> into String []
				String [][] logItems = new String[deleteLog.size()][1];
				for (int i = 0; i < deleteLog.size(); ++i) {
					logItems[i][0] = deleteLog.get(i);
					logger.debug("deleted patient info array index.."+i+ "..is.."+ logItems[i][0]);
				}
				// patient first and last name is on item 3 and 7.
				String name = logItems[3][0].charAt(0)+". "+logItems[7][0];
				return name;
		} catch (Exception ex) {
			logger.error("catching exception ..."+ ex.getMessage());
			return null;
		}
	}

	/**
	* This method get patient info from doctor and patient record.
	*/
	public static String findPatientName(String doctorId, String patientId, String phone) throws Exception
	{
			PatientRecord patientRecord = new PatientRecord();
			String name = "unknown";
			//first find patient name by patienId
			if (patientId.equalsIgnoreCase("0")==false){
				if (patientRecord.readById(patientId)){
					name = patientRecord.firstName.charAt(0)+". "+patientRecord.lastName;
					return name;
				}
			}
			// retrieve patient names by phone, display first match
			String normalizedPhone = PhoneHelper.normalize(phone, false);
			if (normalizedPhone != null){
					if (patientRecord.readByFilter (new String [][] {
						{"doctorId", doctorId},
						{"phone", normalizedPhone}
						}) == true) {
							name = patientRecord.firstName.charAt(0)+". "+patientRecord.lastName;
							return name;
					}
					// if the doctorId belongs to shared phone service group then search in group
					String superuserId = SuperuserHelper.getSuperuser(doctorId);
					if (superuserId != null){
					String [] linkedDoctors = SuperuserHelper.getLinkedDoctors (superuserId);
					for (int i = 0; i < linkedDoctors.length; ++i) {
							// return first match
							if (patientRecord.readByFilter (new String [][] {
								{"doctorId", linkedDoctors[i]},
								{"phone", phone}
								}) == true) {
									name = patientRecord.firstName.charAt(0)+". "+patientRecord.lastName;
									return name;
							}
						}
					}
			}
			return name;
	}
	
}
