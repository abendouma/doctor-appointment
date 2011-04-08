package net.angelspeech.util;

import java.util.GregorianCalendar;

import net.angelspeech.database.ScheduleDayRecord;
import net.angelspeech.object.TimeHelper;

public class AngelspeedUtils {
	
	public static int getStartDay(String date)
	{
		String [] dateParts;
		try {
			dateParts = date.split ("/");
			return (TimeHelper.calendarToDays (new GregorianCalendar (
				Integer.parseInt (dateParts [2]) + 2000,
				Integer.parseInt (dateParts [0]) - 1,
				Integer.parseInt (dateParts [1])
			)));		
		
		} catch (Exception ex) {
			return (-1);
		}

	}
	/**
	 * This method convert form input to startSlot
	 */
	public static int getStartSlot (String startHourString, String startMinuteString, int epochDay, String doctorId)
	{
		System.out.println( startHourString +", "+ startMinuteString +", "+  epochDay +", "+  doctorId);
		
		int startSlot=0;
		
		try {
			ScheduleDayRecord epochDayRecord = new ScheduleDayRecord ();
			if ((epochDayRecord.readByFilter (new String [][] {
				{"doctorId", doctorId},
				{"epochDay", String.valueOf (epochDay)}
				}))==true){
					// calculate startSlot in int
					int startHour =	(new Integer(startHourString)).intValue();
					int startMinutes = (new Integer (startMinuteString)).intValue();
					int rangeStart = startHour * 3600 +	startMinutes * 60;
					int i_slotSize = (new Integer(epochDayRecord.slotSize)).intValue();
					startSlot = rangeStart / i_slotSize;
			}
			return startSlot;
		} catch (Exception ex) {
			System.out.println(ex);
			ex.printStackTrace();
			return (-1);
		}
	}
	
	public static void main(String[] args) {
		System.out.println(getStartSlot("10", "45", 15073, "1"));
	}
}
