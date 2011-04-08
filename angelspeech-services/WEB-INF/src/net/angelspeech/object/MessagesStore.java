package net.angelspeech.object;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;

import net.angelspeech.database.DoctorRecord;
import net.angelspeech.database.SqlQuery;
import net.angelspeech.database.SuperuserRecord;

public class MessagesStore
{
	static final private int MSG_SERVER_DISPLAY = 200;
	static final private int MSG_SERVER_SET_ACTIVE_ID = 201;
	static final private int MSG_SERVER_RESET_CALENDAR = 202;
	static final private int MSG_SERVER_UPDATE_START = 203;
	static final private int MSG_SERVER_UPDATE_STOP = 204;
	static final private int MSG_SERVER_SET_SUPERUSER = 205;
	static final private int MSG_SERVER_SET_DOCTOR = 206;
	static final private int MSG_SERVER_SET_LOCATION = 207;
	static final private int MSG_SERVER_DELETE_LOCATION = 208;
	static final private int MSG_SERVER_SET_APPOINTMENT_PROFILE = 209;
	static final private int MSG_SERVER_DELETE_APPOINTMENT_PROFILE = 210;
	static final private int MSG_SERVER_SET_SCHEDULE = 211;
	static final private int MSG_SERVER_SET_PATIENT = 212;
	static final private int MSG_SERVER_DELETE_PATIENT = 213;
	static final private int MSG_SERVER_DELETE_ALL = 214;

	private LinkedList messages;

	/**
	 * Create a new messages store object
	 */
	public MessagesStore ()
	{
		messages = new LinkedList ();
	}

	/**
	 * Display a group of messages in the client's browser
	 *
	 * @param display	The messages that will be displayed
	 */
	public void addDisplay (String [] display)
	{
		messages.add (new Integer (MSG_SERVER_DISPLAY));
		messages.add (display);
	}

	/**
	 * Inform client about superuser/doctor ID change.
	 *
	 * @param superuserId	The superuser ID. Can be null if there is no active superuser ID.
	 * @param doctorId	The doctor ID. Can be null if there is no active doctor ID.
	 */
	public void setActiveId (String superuserId, String doctorId)
	{
		messages.add (new Integer (MSG_SERVER_SET_ACTIVE_ID));
		messages.add (new Integer [] {
			(superuserId != null) ? new Integer (superuserId) : null,
			(doctorId != null) ? new Integer (doctorId) : null
		});
	}

	/**
	 * Instruct the browser to reset the monthly calendar.
	 */
	public void resetCalendar ()
	{
		messages.add (new Integer (MSG_SERVER_RESET_CALENDAR));
		messages.add (null);
	}

	/**
	 * Inform client browser that periodic update must start.
	 */
	public void updateStart ()
	{
		messages.add (new Integer (MSG_SERVER_UPDATE_START));
		messages.add (null);
	}

	/**
	 * Inform client browser that periodic update must stop.
	 */
	public void updateStop ()
	{
		messages.add (new Integer (MSG_SERVER_UPDATE_STOP));
		messages.add (null);
	}

	/**
	 * Store superuser profile info
	 *
	 * @param superuserId	The superuser ID.
	 */
	public void setSuperuser (String superuserId) throws Exception
	{
		SuperuserRecord superuserRecord = new SuperuserRecord ();

		if (superuserRecord.readById (superuserId) == false) {
			return;
		}
		messages.add (new Integer (MSG_SERVER_SET_SUPERUSER));
		messages.add (new Object [] {
			new String (superuserRecord.firstName),
			new String (superuserRecord.lastName),			new String (((superuserRecord.calledAs).equalsIgnoreCase("none"))? " " : (superuserRecord.calledAs))
		});
	}

	/**
	 * Store doctor profile info
	 *
	 * @param doctorId	The doctor ID.
	 */
	public void setDoctor (String doctorId) throws Exception
	{
		DoctorRecord doctorRecord = new DoctorRecord ();

		if (doctorRecord.readById (doctorId) == false) {
			return;
		}
		messages.add (new Integer (MSG_SERVER_SET_DOCTOR));
		messages.add (new Object [] {
			new Integer (doctorId),
			new String (doctorRecord.firstName),
			new String (doctorRecord.lastName),
			new String (doctorRecord.businessName),
			new String (doctorRecord.street),
			new String (doctorRecord.city),
			new String (doctorRecord.state),
			new String (doctorRecord.zip),
			new String (doctorRecord.bizPhone),
			new Integer (doctorRecord.workMonday),
			new Integer (doctorRecord.workTuesday),
			new Integer (doctorRecord.workWednesday),
			new Integer (doctorRecord.workThursday),
			new Integer (doctorRecord.workFriday),
			new Integer (doctorRecord.workSaturday),
			new Integer (doctorRecord.workSunday),
			new Integer (doctorRecord.slotSize),
			new Integer (doctorRecord.workdayStart),
			new Integer (doctorRecord.workdayEnd),
			new Integer (doctorRecord.lunchStart),
			new Integer (doctorRecord.lunchEnd),
			new Integer (doctorRecord.hasCustomizeAppt),
			new String (((doctorRecord.calledAs).equalsIgnoreCase("none"))? " " : (doctorRecord.calledAs))
		});
	}

	/**
	 * Update the client cache of all the location records.
	 *
	 * @param doctorId	The doctor ID.
	 */
	public void setLocationAll (String doctorId) throws Exception
	{
		setLocationItems (
			doctorId,
			"SELECT " +
				"apptLocationId, " +
				"businessName, " +
				"street, " +
				"city, " +
				"state, " +
				"zip, " +
				"bizPhone, " +
				"mapURL " +
			"FROM apptLocation " +
			"WHERE doctorId='" + SqlQuery.escape (doctorId) + "'"
		);
	}

	/**
	 * Update the client cache for the specified location ID.
	 *
	 * @param doctorId	The doctor ID.
	 * @param locationId	The location ID.
	 */
	public void setLocationOne (String doctorId, String locationId) throws Exception
	{
		setLocationItems (
			doctorId,
			"SELECT " +
				"apptLocationId, " +
				"businessName, " +
				"street, " +
				"city, " +
				"state, " +
				"zip, " +
				"bizPhone, " +
				"mapURL " +
			"FROM apptLocation " +
			"WHERE apptLocationId='" + SqlQuery.escape (locationId) + "'"
		);
	}

	private void setLocationItems (String doctorId, String query) throws Exception
	{
		String [][] rows;
		LinkedList items;
		int i;

		messages.add (new Integer (MSG_SERVER_SET_LOCATION));
		rows = SqlQuery.query (query);
		items = new LinkedList ();
		items.add (new Integer (doctorId));
		for (i = 0; i < rows.length; ++i) {
			items.add (new Integer (rows [i][0]));
			items.add (new String (rows [i][1]));
			items.add (new String (rows [i][2]));
			items.add (new String (rows [i][3]));
			items.add (new String (rows [i][4]));
			items.add (new String (rows [i][5]));
			items.add (new String (rows [i][6]));
			items.add (new String (rows [i][7]));
		}
		messages.add (items.toArray (new Object [0]));
	}

	/**
	 * Remove the specified location from the client cache.
	 *
	 * @param doctorId	The doctor ID.
	 * @param locationId	The location ID.
	 */
	public void deleteLocation (String doctorId, String locationId)
	{
		messages.add (new Integer (MSG_SERVER_DELETE_LOCATION));
		messages.add (new Integer [] {
			new Integer (doctorId),
			new Integer (locationId)
		});
	}

	/**
	 * Update the client cache of all the appointment profile records.
	 *
	 * @param doctorId	The doctor ID.
	 */
	public void setAppointmentProfileAll (String doctorId) throws Exception
	{
		setAppointmentProfileItems (
			doctorId,
			"SELECT " +
				"apptProfileId, " +
				"isDefault, " +
				"name, " +
				"duration, " +
				"selectByPatient, " +
				"allowMonday, " +
				"allowTuesday, " +
				"allowWednesday, " +
				"allowThursday, " +
				"allowFriday, " +
				"allowSaturday, " +
				"allowSunday, " +
				"apptLocationId," +
				"ampmOnly " +
			"FROM apptProfile " +
			"WHERE doctorId='" + SqlQuery.escape (doctorId) + "'"
		);
	}

	/**
	 * Update the client cache for the specified appointment profile ID.
	 *
	 * @param doctorId		The doctor ID.
	 * @param apptProfileId	The appointment profile ID.
	 */
	public void setAppointmentProfileOne (String doctorId, String apptProfileId) throws Exception
	{
		setAppointmentProfileItems (
			doctorId,
			"SELECT " +
				"apptProfileId, " +
				"isDefault, " +
				"name, " +
				"duration, " +
				"selectByPatient, " +
				"allowMonday, " +
				"allowTuesday, " +
				"allowWednesday, " +
				"allowThursday, " +
				"allowFriday, " +
				"allowSaturday, " +
				"allowSunday, " +
				"apptLocationId," +
				"ampmOnly " +
			"FROM apptProfile " +
			"WHERE apptProfileId='" + SqlQuery.escape (apptProfileId) + "'"
		);
	}

	private void setAppointmentProfileItems (String doctorId, String query) throws Exception
	{
		String [][] rows;
		LinkedList items;
		int i;

		messages.add (new Integer (MSG_SERVER_SET_APPOINTMENT_PROFILE));
		rows = SqlQuery.query (query);
		items = new LinkedList ();
		items.add (new Integer (doctorId));
		for (i = 0; i < rows.length; ++i) {
			items.add (new Integer (rows [i][0]));
			items.add (new Integer (rows [i][1]));
			items.add (new String (rows [i][2]));
			items.add (new Integer (rows [i][3]));
			items.add (new Integer (rows [i][4]));
			items.add (new Integer (rows [i][5]));
			items.add (new Integer (rows [i][6]));
			items.add (new Integer (rows [i][7]));
			items.add (new Integer (rows [i][8]));
			items.add (new Integer (rows [i][9]));
			items.add (new Integer (rows [i][10]));
			items.add (new Integer (rows [i][11]));
			items.add (new Integer (rows [i][12]));
			items.add (new Integer (rows [i][13]));
		}
		messages.add (items.toArray (new Object [0]));
	}

	/**
	 * Remove the specified appointment profile from the client cache.
	 *
	 * @param doctorId		The doctor ID.
	 * @param apptProfileId	The appointment profile ID.
	 */
	public void deleteAppointmentProfile (String doctorId, String apptProfileId)
	{
		messages.add (new Integer (MSG_SERVER_DELETE_APPOINTMENT_PROFILE));
		messages.add (new Integer [] {
			new Integer (doctorId),
			new Integer (apptProfileId)
		});
	}

	/**
	 * Store the appointment schedule.
	 *
	 * @param doctorId	The doctor ID.
	 * @param epochDay	The day number counted from the beginning of
	 *			the UNIX epoch (Jan/1/1970). If the epoch day is -1
	 *			then the schedule is set for the whole allowed time
	 *			range.
	 * @param patients	If this array is not null then appointments for patients
	 *			not in this array are stripped of any patient data to
	 *			protect patient privacy.
	 */
	public void setSchedule (String doctorId, int epochDay, String [] patients) throws Exception
	{
		int dayStart, dayEnd;
		DoctorRecord doctorRecord = new DoctorRecord ();
		GregorianCalendar calCurrent, calStart, calEnd;
		HashMap allowedPatients;
		LinkedList items;
		DayInfo [] dayInfo;
		int i;

		if (epochDay != -1) {
			dayStart = epochDay;
			dayEnd = epochDay + 1;
		} else {
			doctorRecord.readById (doctorId);
			calCurrent = TimeHelper.currentCalendar ();
			calStart = (GregorianCalendar) calCurrent.clone ();
			calStart.add (
				GregorianCalendar.MONTH,
				-3 * Integer.parseInt (doctorRecord.calHistory)
			);
			calStart.set (GregorianCalendar.DAY_OF_MONTH, 1);
			calEnd = (GregorianCalendar) calCurrent.clone ();
			calEnd.add (
				GregorianCalendar.MONTH,
				3 * Integer.parseInt (doctorRecord.calActive)
			);
			calEnd.set (GregorianCalendar.DAY_OF_MONTH, 1);
			dayStart = TimeHelper.calendarToDays (calStart);
			dayEnd = TimeHelper.calendarToDays (calEnd);
		}
		if (patients != null) {
			allowedPatients = new HashMap ();
			for (i = 0; i < patients.length; ++i) {
				allowedPatients.put (patients [i], null);
			}
		} else {
			allowedPatients = null;
		}
		messages.add (new Integer (MSG_SERVER_SET_SCHEDULE));
		items = new LinkedList ();
		items.add (new Integer (doctorId));
		items.add (new Integer (dayStart));
		dayInfo = DayInfo.read (doctorId, dayStart, dayEnd);
		for (i = 0; i < dayInfo.length; ++i) {
			if (dayInfo [i] != null) {
				items.add (new Integer (dayInfo [i].slotSize));
				items.add (getDayFree (dayInfo [i]));
				items.add (getDayAppt (dayInfo [i], allowedPatients));
				items.add (getDayEmrg (dayInfo [i]));
			} else {
				items.add (null);
			}
		}
		messages.add (items.toArray (new Object [0]));
	}

	private Object [] getDayFree (DayInfo dayInfo)
	{
		Object [] result;
		int i, offset;

		result = new Object [2 * dayInfo.rangesFree.length];
		offset = 0;
		for (i = 0; i < dayInfo.rangesFree.length; ++i) {
			result [offset++] = new Integer (dayInfo.rangesFree [i].slotStart);
			result [offset++] = new Integer (dayInfo.rangesFree [i].slotEnd);
		}
		return (result);
	}

	private Object [] getDayAppt (DayInfo dayInfo, HashMap patients)
	{
		Object [] result;
		int i, offset;
		boolean fullData;

		result = new Object [7 * dayInfo.rangesAppt.length];
		offset = 0;
		for (i = 0; i < dayInfo.rangesAppt.length; ++i) {
			fullData =
				(patients == null) ||
				patients.containsKey (String.valueOf (dayInfo.rangesAppt [i].patientId));
			result [offset++] = new Integer (dayInfo.rangesAppt [i].apptId);
			result [offset++] = new Integer (fullData ? dayInfo.rangesAppt [i].patientId : -1);
			result [offset++] = new Integer (fullData ? dayInfo.rangesAppt [i].apptProfileId : -1);
			result [offset++] = dayInfo.rangesAppt [i].notes;
			result [offset++] = new Integer (
				fullData ?
				(
					(dayInfo.rangesAppt [i].remSentPhone ? 1 : 0) |
					(dayInfo.rangesAppt [i].remSentEmail ? 2 : 0)
				) :
				0
			);
			result [offset++] = new Integer (dayInfo.rangesAppt [i].slotStart);
			result [offset++] = new Integer (dayInfo.rangesAppt [i].slotEnd);
		}
		return (result);
	}

	private Object [] getDayEmrg (DayInfo dayInfo)
	{
		Object [] result;
		int i, offset;

		result = new Object [5 * dayInfo.rangesEmrg.length];
		offset = 0;
		for (i = 0; i < dayInfo.rangesEmrg.length; ++i) {
			result [offset++] = new Integer (dayInfo.rangesEmrg [i].emrgId);
			result [offset++] = new Integer (dayInfo.rangesEmrg [i].patientId);
			result [offset++] = dayInfo.rangesEmrg [i].notes;
			result [offset++] = new Integer (dayInfo.rangesEmrg [i].slotStart);
			result [offset++] = new Integer (dayInfo.rangesEmrg [i].slotEnd);
		}
		return (result);
	}

	/**
	 * Update the client cache of all the patient records.
	 *
	 * @param doctorId	The doctor ID.
	 */
	public void setPatientAll (String doctorId) throws Exception
	{
		setPatientItems (
			doctorId,
			"SELECT patientId, firstName, lastName, phone, email " +
			"FROM patients " +
			"WHERE " +
				"doctorId='" + SqlQuery.escape (doctorId) + "' AND " +
				"isActive"
		);
	}

	/**
	 * Update the client cache of the specified patient.
	 *
	 * @param doctorId	The doctor ID.
	 * @param patientId	The patient ID.
	 */
	public void setPatientOne (String doctorId, String patientId) throws Exception
	{
		setPatientItems (
			doctorId,
			"SELECT patientId, firstName, lastName, phone, email " +
			"FROM patients " +
			"WHERE " +
				"patientId='" + SqlQuery.escape (patientId) + "' AND " +
				"isActive"
		);
	}

	private void setPatientItems (String doctorId, String query) throws Exception
	{
		String [][] rows;
		LinkedList items;
		int i;

		messages.add (new Integer (MSG_SERVER_SET_PATIENT));
		rows = SqlQuery.query (query);
		items = new LinkedList ();
		items.add (new Integer (doctorId));
		for (i = 0; i < rows.length; ++i) {
			items.add (new Integer (rows [i][0]));
			items.add (new String (rows [i][1]));
			items.add (new String (rows [i][2]));
			items.add (new String (rows [i][3]));
			items.add (new String (rows [i][4]));
		}
		messages.add (items.toArray (new Object [0]));
	}

	/**
	 * Remove the specified patient from the client cache.
	 *
	 * @param doctorId	The doctor ID.
	 * @param patientId	The patient ID.
	 */
	public void deletePatient (String doctorId, String patientId)
	{
		messages.add (new Integer (MSG_SERVER_DELETE_PATIENT));
		messages.add (new Integer [] {
			new Integer (doctorId),
			new Integer (patientId)
		});
	}

	/**
	 * Delete all doctor settings/patients/schedules from the client cache.
	 */
	public void deleteAll ()
	{
		messages.add (new Integer (MSG_SERVER_DELETE_ALL));
		messages.add (null);
	}

	/**
	 * Remove all update requests from object and return them in a linked list
	 *
	 * @return	Return a linked list with all messages
	 */
	public LinkedList fetch ()
	{
		LinkedList result;

		result = messages;
		messages = new LinkedList ();
		return (result);
	}
}
