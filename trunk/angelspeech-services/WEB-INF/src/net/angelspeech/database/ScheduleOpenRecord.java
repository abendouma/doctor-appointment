package net.angelspeech.database;

import net.angelspeech.database.GenericRecord;

/**
 * This class is an object mapping of the "scheduleOpen" SQL table
 */
public class ScheduleOpenRecord extends GenericRecord
{
	public String scheduleDayId;
	public String slotStart;
	public String slotEnd;

	public ScheduleOpenRecord () throws Exception
	{
		super ("scheduleOpen", null, ScheduleOpenRecord.class);
	}
}
