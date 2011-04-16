package net.angelspeech.object;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import net.angelspeech.database.DoctorRecord;
import net.angelspeech.database.SqlQuery;
import net.angelspeech.service.dto.sync.AppointmentProfileSyncInfo;
import net.angelspeech.service.dto.sync.DayInfoDto;
import net.angelspeech.service.dto.sync.DoctorSyncInfo;
import net.angelspeech.service.dto.sync.LocationSyncInfo;
import net.angelspeech.service.dto.sync.PatientItemSyncInfo;
import net.angelspeech.service.dto.sync.PatientSyncInfo;
import net.angelspeech.service.dto.sync.ScheduleSyncInfo;
import net.angelspeech.service.dto.sync.SyncInfo;

/**
 * @author Quang
 *
 */
public class IphoneSyncDataStore {
	private SyncInfo syncInfo = new SyncInfo();
	
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
		DoctorSyncInfo doctorSyncInfo = new DoctorSyncInfo(new Integer (doctorId),
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
		);
		
		syncInfo.doctor = doctorSyncInfo;
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

	private void setLocationItems (String doctorId, String query) throws Exception
	{
		String [][] rows;
		List<LocationSyncInfo> locationSync = new ArrayList<LocationSyncInfo>();
		rows = SqlQuery.query (query);
		
		for (int i = 0; i < rows.length; ++i) {
			locationSync.add(new LocationSyncInfo(new Integer (rows [i][0]), new String (rows [i][1]), new String (rows [i][2]), new String (rows [i][3]), new String (rows [i][4]), new String (rows [i][5]), new String (rows [i][6]), new String (rows [i][7])));
		}

		this.syncInfo.locationSyncInfo = locationSync;
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


	private void setAppointmentProfileItems(String doctorId, String query)
			throws Exception {
		List<AppointmentProfileSyncInfo> appts = new ArrayList<AppointmentProfileSyncInfo>();
		String[][] rows;
		rows = SqlQuery.query(query);
		for (int i = 0; i < rows.length; ++i) {

			appts.add(new AppointmentProfileSyncInfo(new Integer(rows[i][0]),
					new Integer(rows[i][1]), new String(rows[i][2]),
					new Integer(rows[i][3]), new Integer(rows[i][4]),
					new Integer(rows[i][5]), new Integer(rows[i][6]),
					new Integer(rows[i][7]), new Integer(rows[i][8]),
					new Integer(rows[i][9]), new Integer(rows[i][10]),
					new Integer(rows[i][11]), new Integer(rows[i][12]),
					new Integer(rows[i][13])));
		}
		
		this.syncInfo.appointmentProfileSyncInfo = appts;
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
		
		dayInfo = DayInfo.read (doctorId, dayStart, dayEnd);
		
		List<DayInfoDto> dayInfos = new ArrayList<DayInfoDto>();
		
		for (i = 0; i < dayInfo.length; ++i) {
			if (dayInfo [i] != null) {
				dayInfos.add(new DayInfoDto(new Integer (dayInfo [i].slotSize), getDayFree (dayInfo [i]), getDayAppt (dayInfo [i], allowedPatients), getDayEmrg (dayInfo [i])));
			}
		}
		ScheduleSyncInfo scheduleInfo = new ScheduleSyncInfo(new Integer (doctorId), new Integer (dayStart), dayInfos);
		
		this.syncInfo.scheduleSyncInfo = scheduleInfo;
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


	private void setPatientItems (String doctorId, String query) throws Exception
	{
		String [][] rows;
		int i;

		rows = SqlQuery.query (query);
		
		List<PatientSyncInfo> patients = new ArrayList<PatientSyncInfo>();
		for (i = 0; i < rows.length; ++i) {
			patients.add(new PatientSyncInfo(new Integer(rows[i][0]),
					     new String(rows[i][1]), new String(rows[i][2]), 
					     new String(rows[i][3]), new String(rows[i][4])));
		}
		
		PatientItemSyncInfo patientItem = new PatientItemSyncInfo(new Integer (doctorId), patients);
		
		this.syncInfo.patientItems = patientItem;
	}
	
	public SyncInfo getSyncInfo() {
		return syncInfo;
	}
}
