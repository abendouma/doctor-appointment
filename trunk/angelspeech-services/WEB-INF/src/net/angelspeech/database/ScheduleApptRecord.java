package net.angelspeech.database;

import net.angelspeech.database.GenericRecord;

/**
 * This class is an object mapping of the "scheduleAppt" SQL table
 * The recurrentApptId = 0 for regular non-recurrent appt
 *					   = 1 for the first recurrent appt
 *					   = scheduleApptId of initial appt id on subsequent appt record
 *				apptStatus = 0 default, OK/confirmed
 *						   = 1 pending for payment
 *
 */
public class ScheduleApptRecord extends GenericRecord
{
	public String scheduleApptId;
	public String scheduleDayId;
	public String patientId;
	public String apptProfileId;
	public String recurrentApptId;
	public String fromPatientGui;
	public String notes;
	public String remReqPhone;
	public String remReqEmail;
	public String remSentPhone;
	public String remSentEmail;
	public String requireSIU;
	public String requireGsync;
	public String requireSMS;
	public String slotStart;
	public String slotEnd;
	public String apptStatus;


	public ScheduleApptRecord () throws Exception
	{
		super ("scheduleAppt", "scheduleApptId", ScheduleApptRecord.class);
	}
}
