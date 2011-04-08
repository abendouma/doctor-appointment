package net.angelspeech.database;

import net.angelspeech.database.GenericRecord;

/**
 * This class is an object mapping of the "callRecord" SQL table
 */
public class CallRecord extends GenericRecord
{
	public String callRecordId;
	public String doctorId;
	public String patientPhone;
	public String patientId;	public String transferredToPhone;
	public String apptEpochDay;
	public String apptStart;
	public String callEpochDay;
	public String callStart;
	public String callEnd;	public String callType;
	public String callResult;
	public String errorEventLog;
	public String SIUStatus;
	public String doneGsync;

	public CallRecord () throws Exception
	{
		super ("callRecord", "callRecordId", CallRecord.class);
	}
}
