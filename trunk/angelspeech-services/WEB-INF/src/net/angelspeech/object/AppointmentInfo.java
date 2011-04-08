package net.angelspeech.object;

import java.util.GregorianCalendar;
import net.angelspeech.database.PatientRecord;
import net.angelspeech.database.ScheduleApptRecord;
import net.angelspeech.database.ScheduleDayRecord;
import net.angelspeech.database.ApptProfileRecord;
import net.angelspeech.database.SqlQuery;
import org.apache.log4j.Logger;
import java.io.Serializable;


/**
 * This class is used to crete/delete/read/write normal appointments
 */
public class AppointmentInfo implements Serializable
{

	//requireGsync value defintion
	public static final String GSYNC_NO = "0";
	public static final String GSYNC_FROM_GOOGLE = "2";
	public static final String GSYNC_TO_GOOGLE = "1";
	public static final String GSYNC_NO_CONFLICT = "0";
    public static final String GSYNC_CONFLICT = "1";


	static private Logger logger = Logger.getLogger (AppointmentInfo.class);
	public String apptId;			// Appointment ID. Set upon appointment creation.
	public String doctorId;			// Doctor ID
	public String patientId;		// Patient ID
	public String apptProfileId;		// Appointment profile ID
	public String recurrentApptId;		// Appointment profile ID
	public String apptStatus;			// 0=ok, 1= pending for payment
	public boolean fromPatientGui;		// True iff this appointment has been added from the patient GUI.
	public String notes;			// Appointment notes
	public boolean remReqPhone;		// Phone reminder is required.
	public boolean remReqEmail;		// Email reminder is required.
	public boolean requireSIU;		// HL7 message SIU is required.
	public String requireGsync;		// googleSync status.
    public String requireSMS;       // text reminder status.
	public boolean remSentPhone;		// Phone reminder has been sent. Not used for appointment creation.
	public boolean remSentEmail;		// Email reminder has been sent. Not used for appointment creation.
	public int epochDay;			// Day number of the appointment counted from the beginning of the UNIX epoch (Jan/1/1970)
	public int rangeStart;			// Starting second of the appointment, counted from midnight
	public int rangeEnd;			// Ending second of the appointment, counted from midnight
	public int durationInSeconds;	// appt duration in seconds

	static private class SlotRange
	{
		public int slotStart;
		public int slotEnd;
	}

	/**
	 * Try to create a new appointment.
	 *
	 * @return		Return true on success or false on failure.
	 * @param strictProfile	When this flag is set, stricter profile checks apply:
	 *			1. If there is profile day mismatch the operation fails.
	 *			2. If the profile is too long to fit in the free slot
	 *			   range, the operation fails. When the flag is not set
	 *			   and the free slot range is not big enough the
	 *			   appointment is automatically shortened (shortening
	 *			   is performed only applies when appointment profile
	 *			   is used).
	 *			3. If the selectByPatient profile flag is not set the
	 *			   operation fails.
	 */
	public boolean create (boolean strictProfile) throws Exception
	{

		SlotRange slotRange;
		PatientRecord patientRecord = new PatientRecord ();
		ScheduleDayRecord scheduleDayRecord = new ScheduleDayRecord ();
		ScheduleApptRecord scheduleApptRecord = new ScheduleApptRecord ();
		ApptProfileRecord apptProfileRecord = new ApptProfileRecord ();

		if ((slotRange = createApptRange (null, strictProfile)) == null) {
			return (false);
		}
		scheduleDayRecord.readByFilter (new String [][] {
			{"doctorId", doctorId},
			{"epochDay", String.valueOf (epochDay)}
		});
		patientRecord.readById (patientId);
		scheduleApptRecord.scheduleDayId = scheduleDayRecord.scheduleDayId;
		scheduleApptRecord.patientId = patientId;
		scheduleApptRecord.apptProfileId = apptProfileId;
		scheduleApptRecord.fromPatientGui = fromPatientGui ? "1" : "0";
		scheduleApptRecord.notes = notes;
		scheduleApptRecord.recurrentApptId = recurrentApptId; //Modified by Mathias
		scheduleApptRecord.apptStatus = apptStatus;
		scheduleApptRecord.requireSIU = requireSIU ? "1" : "0";
		scheduleApptRecord.requireGsync = requireGsync;
		scheduleApptRecord.requireSMS = requireSMS;
		scheduleApptRecord.remReqPhone =
			(
				patientRecord.reminderType.equals ("1") ||
				patientRecord.reminderType.equals ("3")
			) ? "1" : "0";
		scheduleApptRecord.remReqEmail =
			(
				patientRecord.reminderType.equals ("2") ||
				patientRecord.reminderType.equals ("3")
			) ? "1" : "0";
		scheduleApptRecord.remSentPhone = "0";
		scheduleApptRecord.remSentEmail = "0";
		scheduleApptRecord.slotStart = String.valueOf (slotRange.slotStart);
		scheduleApptRecord.slotEnd = String.valueOf (slotRange.slotEnd);
		//NOTE: This apptId setting is added for Pre-paid appt
		apptId = scheduleApptRecord.create ();
		// calculate regular and profile-based appt duration
		rangeStart =
			Integer.parseInt (scheduleApptRecord.slotStart) *
			Integer.parseInt (scheduleDayRecord.slotSize);
		rangeEnd =
			Integer.parseInt (scheduleApptRecord.slotEnd) *
			Integer.parseInt (scheduleDayRecord.slotSize);
		if (apptProfileId.equals ("0") == true) {
			durationInSeconds = rangeEnd - rangeStart;
		}else{
			apptProfileRecord.readById (apptProfileId);
			durationInSeconds =
				Integer.parseInt(apptProfileRecord.duration)*
				Integer.parseInt (scheduleDayRecord.slotSize);
		}
		return (true);
	}

	/**
	 * Try to create a new recurrent appointment.
	 *
	 * @return		Return new apptId on success or ? on failure.
	 * This method perform similar work as
	 */
	public String createRecurrentAppt (boolean strictProfile, String recurrentApptId) throws Exception
	{
		String apptId;
		SlotRange slotRange;
		PatientRecord patientRecord = new PatientRecord ();
		ScheduleDayRecord scheduleDayRecord = new ScheduleDayRecord ();
		ScheduleApptRecord scheduleApptRecord = new ScheduleApptRecord ();
		ApptProfileRecord apptProfileRecord = new ApptProfileRecord ();

		if ((slotRange = createApptRange (null, strictProfile)) == null) {
			return (null);
		}
		scheduleDayRecord.readByFilter (new String [][] {
			{"doctorId", doctorId},
			{"epochDay", String.valueOf (epochDay)}
		});
		patientRecord.readById (patientId);
		scheduleApptRecord.scheduleDayId = scheduleDayRecord.scheduleDayId;
		scheduleApptRecord.patientId = patientId;
		scheduleApptRecord.apptProfileId = apptProfileId;
		scheduleApptRecord.recurrentApptId = recurrentApptId;
		scheduleApptRecord.fromPatientGui = fromPatientGui ? "1" : "0";
		scheduleApptRecord.notes = notes;
		scheduleApptRecord.requireSIU = requireSIU ? "1" : "0";
		scheduleApptRecord.requireGsync = requireGsync;
		scheduleApptRecord.requireSMS = requireSMS;
		scheduleApptRecord.remReqPhone =
			(
				patientRecord.reminderType.equals ("1") ||
				patientRecord.reminderType.equals ("3")
			) ? "1" : "0";
		scheduleApptRecord.remReqEmail =
			(
				patientRecord.reminderType.equals ("2") ||
				patientRecord.reminderType.equals ("3")
			) ? "1" : "0";
		scheduleApptRecord.remSentPhone = "0";
		scheduleApptRecord.remSentEmail = "0";
		scheduleApptRecord.slotStart = String.valueOf (slotRange.slotStart);
		scheduleApptRecord.slotEnd = String.valueOf (slotRange.slotEnd);
		String cretedApptId = scheduleApptRecord.create ();
		// calculate regular and profile-based appt duration
		rangeStart =
			Integer.parseInt (scheduleApptRecord.slotStart) *
			Integer.parseInt (scheduleDayRecord.slotSize);
		rangeEnd =
			Integer.parseInt (scheduleApptRecord.slotEnd) *
			Integer.parseInt (scheduleDayRecord.slotSize);
		if (apptProfileId.equals ("0") == true) {
			durationInSeconds = rangeEnd - rangeStart;
		}else{
			apptProfileRecord.readById (apptProfileId);
			durationInSeconds =
				Integer.parseInt(apptProfileRecord.duration)*
				Integer.parseInt (scheduleDayRecord.slotSize);
		}
		return (cretedApptId);
	}

	/**
	 * Try to cancel an existing appointment.
	 *
	 * @return		Return true iff the appointment was cancelled successfully.
	 * @param apptId	The appointment ID.
	 */
	static public boolean destroy (String apptId) throws Exception
	{
		ScheduleApptRecord scheduleApptRecord = new ScheduleApptRecord ();

		if (scheduleApptRecord.readById (apptId) == false) {
			return (false);
		}
		scheduleApptRecord.destroyById (apptId);
		return (true);
	}

	/**
	 * Try to read an existing appointment.
	 *
	 * @return		Return true iff the appointment data was read successfully.
	 */
	public boolean read (String itemId) throws Exception
	{

		ScheduleApptRecord scheduleApptRecord = new ScheduleApptRecord ();
		ScheduleDayRecord scheduleDayRecord = new ScheduleDayRecord ();
		ApptProfileRecord apptProfileRecord = new ApptProfileRecord ();

		if (scheduleApptRecord.readById (itemId) == false) {
			return (false);
		}
		scheduleDayRecord.readById (scheduleApptRecord.scheduleDayId);
		apptId = scheduleApptRecord.scheduleApptId;
		doctorId = scheduleDayRecord.doctorId;
		patientId = scheduleApptRecord.patientId;
		apptProfileId = scheduleApptRecord.apptProfileId;
		recurrentApptId = scheduleApptRecord.recurrentApptId;
		fromPatientGui = scheduleApptRecord.fromPatientGui.equals ("1");
		notes = scheduleApptRecord.notes;
		apptStatus = scheduleApptRecord.apptStatus;
		requireSIU = scheduleApptRecord.requireSIU.equals ("1");
		requireGsync = scheduleApptRecord.requireGsync;
		requireSMS = scheduleApptRecord.requireSMS;
		remReqPhone = scheduleApptRecord.remReqPhone.equals ("1");
		remReqEmail = scheduleApptRecord.remReqEmail.equals ("1");
		remSentPhone = scheduleApptRecord.remSentPhone.equals ("1");
		remSentEmail = scheduleApptRecord.remSentEmail.equals ("1");
		epochDay = Integer.parseInt (scheduleDayRecord.epochDay);
		// calculate regular and profile-based appt duration
		rangeStart =
			Integer.parseInt (scheduleApptRecord.slotStart) *
			Integer.parseInt (scheduleDayRecord.slotSize);
		rangeEnd =
			Integer.parseInt (scheduleApptRecord.slotEnd) *
			Integer.parseInt (scheduleDayRecord.slotSize);
		if (apptProfileId.equals ("0") == true) {
			durationInSeconds = rangeEnd - rangeStart;
		}else{
			apptProfileRecord.readById (apptProfileId);
			durationInSeconds =
				Integer.parseInt(apptProfileRecord.duration)*
				Integer.parseInt (scheduleDayRecord.slotSize);
		}
		return (true);
	}

	/**
	 * Try to modify an existing appointment.
	 *
	 * @return		Return true iff the update was successfull.
	 * @param strictProfile	When this flag is set, stricter profile checks apply:
	 *			1. If there is profile day mismatch the operation fails.
	 *			2. If the profile is too long to fit in the free slot
	 *			   range, the operation fails. When the flag is not set
	 *			   and the free slot range is not big enough the
	 *			   appointment is automatically shortened (shortening
	 *			   is performed only applies when appointment profile
	 *			   is used).
	 *			3. If the selectByPatient profile flag is not set the
	 *			   operation fails.
	 */
	public boolean update (boolean strictProfile) throws Exception
	{
		ScheduleApptRecord scheduleApptRecord = new ScheduleApptRecord ();
		ScheduleDayRecord oldDayRecord = new ScheduleDayRecord ();
		ScheduleDayRecord newDayRecord = new ScheduleDayRecord ();
		SlotRange slotRange;

		if ((slotRange = createApptRange (apptId, strictProfile)) == null) {
			return (false);
		}
		if (scheduleApptRecord.readById (apptId) == false) {
			return (false);
		}
		oldDayRecord.readById (scheduleApptRecord.scheduleDayId);
		newDayRecord.readByFilter (new String [][] {
			{"doctorId", doctorId},
			{"epochDay", String.valueOf (epochDay)}
		});
		scheduleApptRecord.scheduleDayId = newDayRecord.scheduleDayId;
		scheduleApptRecord.apptProfileId = apptProfileId;
		scheduleApptRecord.fromPatientGui = fromPatientGui ? "1" : "0";
		scheduleApptRecord.notes = notes;
		scheduleApptRecord.apptStatus = apptStatus;
		scheduleApptRecord.requireSIU = requireSIU ? "1" : "0";
		scheduleApptRecord.requireGsync = requireGsync;
		scheduleApptRecord.requireSMS = requireSMS;
		scheduleApptRecord.remReqPhone = remReqPhone ? "1" : "0";
		scheduleApptRecord.remReqEmail = remReqEmail ? "1" : "0";
		scheduleApptRecord.remSentPhone = remSentPhone ? "1" : "0";
		scheduleApptRecord.remSentEmail = remSentEmail ? "1" : "0";
		scheduleApptRecord.slotStart = String.valueOf (slotRange.slotStart);
		scheduleApptRecord.slotEnd = String.valueOf (slotRange.slotEnd);
		scheduleApptRecord.writeById (apptId);
		return (true);
	}

	private SlotRange createApptRange (String matchId, boolean strictProfile) throws Exception
	{
		ApptProfileRecord apptProfileRecord;
		boolean relaxedChecks;
		DayInfo [] dayInfo;
		DayInfo dayOne;
		int slotStart, slotEnd, i;
		int [] slots;
		SlotRange slotRange;

		if (apptProfileId.equals ("0") == false) {
			apptProfileRecord = new ApptProfileRecord ();
			apptProfileRecord.readById (apptProfileId);
			relaxedChecks = (strictProfile == false);
		} else {
			apptProfileRecord = null;
			relaxedChecks = false;
		}
		dayInfo = DayInfo.read (doctorId, epochDay, epochDay + 1);
		if (dayInfo [0] == null) {
			return (null);
		}
		dayOne = dayInfo [0];
		if (
			(apptProfileRecord != null) &&
			(relaxedChecks == false) &&
			(
				(checkProfileDay (apptProfileRecord, dayOne.epochDay) == false) ||
				(apptProfileRecord.selectByPatient.equals ("0"))
			)
		) {
			return (null);
		}
		if (
			(rangeStart < 0) ||
			(rangeStart > 86400) ||
			(rangeStart % dayOne.slotSize != 0)
		) {
			return (null);
		}
		slotStart = rangeStart / dayOne.slotSize;
		slotEnd =
			(apptProfileRecord != null) ?
			slotStart + Integer.parseInt (apptProfileRecord.duration) :
			rangeEnd / dayOne.slotSize;
		slots = dayOne.getArraySlots ();
		for (i = slotStart; i < slotEnd; ++i) {
			if ((slots [i] != 0) && ((matchId == null) || (Integer.parseInt (matchId) != slots [i]))) {
				if (relaxedChecks && (i != slotStart)) {
					slotEnd = i;
					break;
				} else {
					return (null);
				}
			}
		}
		slotRange = new SlotRange ();
		slotRange.slotStart = slotStart;
		slotRange.slotEnd = slotEnd;
		return (slotRange);
	}

	private boolean checkProfileDay (ApptProfileRecord apptProfileRecord, int epochDay)
	{
		GregorianCalendar cal;

		cal = TimeHelper.daysToCalendar (epochDay);
		switch (cal.get (GregorianCalendar.DAY_OF_WEEK)) {
		case GregorianCalendar.MONDAY:
			return (apptProfileRecord.allowMonday.equals ("1"));
		case GregorianCalendar.TUESDAY:
			return (apptProfileRecord.allowTuesday.equals ("1"));
		case GregorianCalendar.WEDNESDAY:
			return (apptProfileRecord.allowWednesday.equals ("1"));
		case GregorianCalendar.THURSDAY:
			return (apptProfileRecord.allowThursday.equals ("1"));
		case GregorianCalendar.FRIDAY:
			return (apptProfileRecord.allowFriday.equals ("1"));
		case GregorianCalendar.SATURDAY:
			return (apptProfileRecord.allowSaturday.equals ("1"));
		case GregorianCalendar.SUNDAY:
			return (apptProfileRecord.allowSunday.equals ("1"));
		default:
			return (false);
		}
	}

	/**
	* This tester check if a appt type is pre-paid appt
	*/

	public boolean isPrepaidAppt(String apptProfileId) throws Exception
	{
		ApptProfileRecord apptProfileRecord = new ApptProfileRecord ();
		apptProfileRecord.readById(apptProfileId);
		boolean isPrepaid = false;
		if ((apptProfileRecord.prepaidFee).equalsIgnoreCase("0")==false){
			isPrepaid = true;
		}
		return (isPrepaid);
	}

}
