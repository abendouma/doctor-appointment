package net.angelspeech.database;

import net.angelspeech.database.GenericRecord;

/**
 * This class is an object mapping of the "scheduleDays" SQL table
 */
public class ScheduleDayRecord extends GenericRecord
{
	public String scheduleDayId;
	public String doctorId;
	public String epochDay;
	public String slotSize;
	public String callBackScheduled;

	public ScheduleDayRecord () throws Exception
	{
		super ("scheduleDays", "scheduleDayId", ScheduleDayRecord.class);
	}
}
