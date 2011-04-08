package net.angelspeech.object;
import java.lang.System;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.log4j.Logger;

/**
 * This class contains different time helper functions
 */
public class TimeHelper
{
	/*
		NB2: I added a tz=System.setProperty("user.timezone", "US/Eastern");
		to set default time zone for JVM. Let's see if this helps.
	*/
	static private Logger logger = Logger.getLogger (TimeHelper.class);
	static private String tz = System.setProperty("user.timezone", "US/Eastern");
	static public SimpleDateFormat outputDateFormat = new SimpleDateFormat ("EEEE, MMMM dd, yyyy");
	static public SimpleDateFormat weekOfFormat = new SimpleDateFormat ("MMMM dd");
	static public SimpleDateFormat shortDateFormat = new SimpleDateFormat ("MMM d");
	static public SimpleDateFormat outputTimeFormat = new SimpleDateFormat ("h:mm a");
	static public SimpleDateFormat inputDateFormat = new SimpleDateFormat ("M/d/yyyy");
	static public SimpleDateFormat inputTimeFormat = new SimpleDateFormat ("h:mm a");
	static public SimpleDateFormat dayInWeekFormat = new SimpleDateFormat ("EEEE");
	static public SimpleDateFormat shortDateTimeFormat = new SimpleDateFormat ("EEE MMM d, h:mm a");
	static public SimpleDateFormat shortDateTime2Format = new SimpleDateFormat ("M/d, h:mm a");
	static public SimpleDateFormat loggerOutputFormat = new SimpleDateFormat ("MM/dd/yyyy HH:mm:ss");

	static public String[] monthName = {"January", "February", "March",
										"April", "May", "June",
										"July",	"August", "September",
										"October", "November","December"};


	/**
	 * Convert a UNIX epoch day to a GregorianCalendar date.
	 *
	 * @return		Return a GregorianCalendar date.
	 * @param epochDays	Epoch day counted from the beginning of the UNIX epoch (Jan/1/1970).
	 */
	static public GregorianCalendar daysToCalendar (int epochDays)
	{
		GregorianCalendar calendar;

		/*
		   NB: We need to create the calendar initially with UTC timezone, since we don't
		   know beforehand what time correction to apply to the millisecond value, because
		   we don't know if daylight saving is active for the specified time of the year
		*/
		calendar = new GregorianCalendar (TimeZone.getTimeZone ("UTC"), Locale.ENGLISH);
		calendar.setTimeInMillis ((long) epochDays * 86400L * 1000L);
		return (new GregorianCalendar (
			calendar.get (GregorianCalendar.YEAR),
			calendar.get (GregorianCalendar.MONTH),
			calendar.get (GregorianCalendar.DAY_OF_MONTH),
			calendar.get (GregorianCalendar.HOUR_OF_DAY),
			calendar.get (GregorianCalendar.MINUTE),
			calendar.get (GregorianCalendar.SECOND)
		));
	}

	/**
	 * Convert UNIX epoch seconds to a Gregorian date.
	 *
	 * @return		Return a GregorianCalendar date.
	 * @param epochSeconds	Seconds counted from the beginning of the UNIX epoch (Jan/1/1970).
	 */
	static public GregorianCalendar secondsToCalendar (int epochSeconds)
	{
		GregorianCalendar calendar;

		/*
		   NB: We need to create the calendar initially with UTC timezone, since we don't
		   know beforehand what time correction to apply to the millisecond value, because
		   we don't know if daylight saving is active for the specified time of the year

		NB2: I added a tz=System.setProperty("user.timezone", "US/Eastern");
		to set default time zone for JVM. Let's see if this helps.
		*/
		calendar = new GregorianCalendar (TimeZone.getTimeZone ("UTC"), Locale.ENGLISH);
		calendar.setTimeInMillis ((long) epochSeconds * 1000L);
		return (new GregorianCalendar (
			calendar.get (GregorianCalendar.YEAR),
			calendar.get (GregorianCalendar.MONTH),
			calendar.get (GregorianCalendar.DAY_OF_MONTH),
			calendar.get (GregorianCalendar.HOUR_OF_DAY),
			calendar.get (GregorianCalendar.MINUTE),
			calendar.get (GregorianCalendar.SECOND)
		));
	}

	/**
	 * Convert UNIX epochDay and daySeconds(seconds from midnite) to a Gregorian date 
	 * for a given timeZone.
	 *
	 * @return		Return a GregorianCalendar date of the given time zone.
	 * @param epochDay	Days counted from the beginning of the UNIX epoch (Jan/1/1970).
	 * @param daySeconds	Seconds counted from the mid-nite.
	 * @param epochSeconds	Seconds counted from the beginning of the UNIX epoch (Jan/1/1970).
	 */
	static public GregorianCalendar epochTimeToCalendar (int epochDay, int daySeconds)
	{
		GregorianCalendar calendar;
		
		//calendar = new GregorianCalendar (TimeZone.getTimeZone (timeZone), Locale.ENGLISH);
		calendar = new GregorianCalendar (TimeZone.getTimeZone ("UTC"), Locale.ENGLISH);
		long epochSeconds = epochDay * 86400 + daySeconds;
		calendar.setTimeInMillis (epochSeconds * 1000L);
		return (new GregorianCalendar (
			calendar.get (GregorianCalendar.YEAR),
			calendar.get (GregorianCalendar.MONTH),
			calendar.get (GregorianCalendar.DAY_OF_MONTH),
			calendar.get (GregorianCalendar.HOUR_OF_DAY),
			calendar.get (GregorianCalendar.MINUTE),
			calendar.get (GregorianCalendar.SECOND)
		));
	}	
	
	
	/**
	 * Convert a Calendar date to a UNIX epoch day.
	 *
	 * @return		Epoch day counted from the beginning of the UNIX epoch (Jan/1/1970).
	 * @param calendar	A Calendar date.
	 */
	static public int calendarToDays (Calendar calendar)
	{
		return ((int) (
			(calendar.getTimeInMillis () + getCalendarOffset (calendar)) /
			(1000L * 86400L)
		));
	}

	/**
	 * Convert a Calendar date to a UNIX epoch seconds.
	 *
	 * @return		Epoch seconds counted from the beginning of the UNIX epoch (Jan/1/1970).
	 * @param calendar	A Calendar date.
	 */
	static public int calendarToSeconds (Calendar calendar)
	{
		return ((int) (
			(calendar.getTimeInMillis () + getCalendarOffset (calendar)) /
			1000L
		));
	}

	/**
	 * @return	current time in inputformat ("h:mm a").
	 */
	static public String currentShortTime ()
	{
		String shortTime="";
		GregorianCalendar now=currentCalendar ();
		int AMPM_value = now.get(Calendar.AM_PM);
		String AMPM = "AM";
		if (AMPM_value == 1){
			AMPM="PM";
		}
		// add "0" before minutes if it is less than 10
		if (now.get(Calendar.MINUTE)>9){
			shortTime = now.get(Calendar.HOUR)+ ":"+ now.get(Calendar.MINUTE)+" "+ AMPM;
		}else{
			shortTime=now.get(Calendar.HOUR)+ ":"+ "0"+now.get(Calendar.MINUTE)+" "+ AMPM;
		}
		return shortTime;
	}

	/**
	 * @return	current date in inputformat("M/d/yyyy").
	 */
	static public String currentShortDate ()
	{
		return (inputDateFormat.format (currentCalendar ()));
	}

	/**
	 *
	 * Return a calendar set to current time.
	 *
	 * @return		Calendar set to current time.
	 */
	static public GregorianCalendar currentCalendar ()
	{
		return (new GregorianCalendar (TimeZone.getTimeZone ("US/Eastern")));
	}

	/**
	 * Return current epoch day.
	 * FIXME: This function should accept a parameter specifying time zone.
	 *
	 * @return		Current epoch day counted from the beginning of the UNIX epoch (Jan/1/1970).
	 */
	static public int currentEpochDay ()
	{
		return (calendarToDays (currentCalendar ()));
	}

	/**
	 * Return current epoch second.
	 * FIXME: This function should accept a parameter specifying time zone.
	 *
	 * @return		Current epoch day second from the beginning of the UNIX epoch (Jan/1/1970).
	 */
	static public int currentEpochSecond ()
	{
		return (calendarToSeconds (currentCalendar ()));
	}
	/**
	 * This method convert date object to epochDay
	 */
	static public int dateToEpochDay (Date date)
	{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime (date);
		return (calendarToDays (calendar));
	}
	static public boolean isEpochdayMonday(int epochDay)
	{
		boolean isMonday = false;
		String dayInWeek = epochDayToDayInWeek(epochDay);
		if (dayInWeek.equalsIgnoreCase("Monday")){
			isMonday = true;
		}
		return isMonday;
	}

	static public boolean isEpochdayTuesday(int epochDay)
	{
		boolean isTuesday = false;
		String dayInWeek = epochDayToDayInWeek(epochDay);
		if (dayInWeek.equalsIgnoreCase("Tuesday")){
			isTuesday = true;
		}
		return isTuesday;
	}

	static public boolean isEpochdayWednesday(int epochDay)
	{
		boolean isWednesday = false;
		String dayInWeek = epochDayToDayInWeek(epochDay);
		if (dayInWeek.equalsIgnoreCase("Wednesday")){
			isWednesday = true;
		}
		return isWednesday;
	}

	static public boolean isEpochdayThursday(int epochDay)
	{
		boolean isThursday = false;
		String dayInWeek = epochDayToDayInWeek(epochDay);
		if (dayInWeek.equalsIgnoreCase("Thursday")){
			isThursday = true;
		}
		return isThursday;
	}

	static public boolean isEpochdayFriday(int epochDay)
	{
		boolean isFriday = false;
		String dayInWeek = epochDayToDayInWeek(epochDay);
		if (dayInWeek.equalsIgnoreCase("Friday")){
			isFriday = true;
		}
		return isFriday;
	}

	static public boolean isEpochdaySaturday(int epochDay)
	{
		boolean isSaturday = false;
		String dayInWeek = epochDayToDayInWeek(epochDay);
		if (dayInWeek.equalsIgnoreCase("Saturday")){
			isSaturday = true;
		}
		return isSaturday;
	}

	static public boolean isEpochdaySunday(int epochDay)
	{
		boolean isSunday = false;
		String dayInWeek = epochDayToDayInWeek(epochDay);
		if (dayInWeek.equalsIgnoreCase("Sunday")){
			isSunday = true;
		}
		return isSunday;
	}

	static public boolean isEpochdayWeekend(int epochDay)
	{
		boolean isWeekend = false;
		String dayInWeek = epochDayToDayInWeek(epochDay);
		if (dayInWeek.equalsIgnoreCase("Sunday")||dayInWeek.equalsIgnoreCase("Saturday")){
			isWeekend = true;
		}
		return isWeekend;
	}


	/**
	 * This method check if current time is between
	 * 12:00 PM - 8:00 PM local time and disallow
	 * any automated reminder call to be made outside this time range
	 * to customers.
	 *
	 */

	static public boolean isWeekendCallingTimeAllowed (String timeZone)
		{
			boolean isAllowed = false;
			GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone (timeZone));
			int hourOfDay = now.get(Calendar.HOUR_OF_DAY);
			logger.debug("timeZone "+timeZone + " calendar HOUR_OF_DAY is " + hourOfDay + " oclock");
			if ( (hourOfDay > 11)&&(hourOfDay < 20)){
				isAllowed = true;
			}else{
				isAllowed = false;
			}
			logger.debug ("permission for Weekend outbound call is " + (isAllowed? "YES" : "NO") );
			return isAllowed;
		}

	/**
	 * This method check if current time is between
	 * 9:00 AM - 8:00 PM local time and disallow
	 * any automated reminder and cancellation
	 * call to be made outside this time range
	 * to customers.
	 *
	 */
	static public boolean isCallingTimeAllowed (String timeZone)
	{
		boolean isAllowed = false;
		GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone (timeZone));
		int hourOfDay = now.get(Calendar.HOUR_OF_DAY);
		logger.debug("timeZone "+timeZone + " calendar HOUR_OF_DAY is " + hourOfDay + " oclock");
		if ( (hourOfDay > 8)&&(hourOfDay < 20)){
			isAllowed = true;
		}else{
			isAllowed = false;
		}
		logger.debug ("permission for outbound call is " + (isAllowed? "YES" : "NO") );
		return isAllowed;
	}
	/**
	 * This method check if the given appt time (epochDay and StartSecond)
	 * is within 24 hour of current time.
	 *
	 * @param epochDay		Day number of the appointment
	 *						counted from the beginning of the UNIX epoch (Jan/1/1970)
	 * @param rangeStart	Starting second of the appointment, counted from midnight.
	 *
	 */
	static public boolean isApptWithin24Hours (int epochDay, int rangeStart)
	{
		boolean within24Hours = false;
		int nowSecond = currentEpochSecond ();
		int apptSecond = epochDay*24*3600 + rangeStart;
		int diffHours = Math.abs(apptSecond - nowSecond)/3600;
		if (diffHours < 24) {
			within24Hours = true;
		}
		//logger.info("within24Hours tester is " +within24Hours+" diff in hour is "+diffHours);
		return within24Hours;
	}

	/**
	 * Get the difference in days between two dates.
	 *
	 * @return		The difference in days between two dates.
	 * @param calFirst	The first date.
	 * @param calSecond	The second date.
	 */
	static public int getDiffDays (Calendar calFirst, Calendar calSecond)
	{
		return (calendarToDays (calFirst) - calendarToDays (calSecond));
	}

	/**
	 * Get the difference in weeks between two dates.
	 *
	 * @return		The difference in days between two dates.
	 * @param calFirst	The first date.
	 * @param calSecond	The second date.
	 */
	static public int getDiffWeeks (Calendar calFirst, Calendar calSecond)
	{
		int weekFirst, weekSecond;

		weekFirst = (calendarToDays (calFirst) + 3) / 7;
		weekSecond = (calendarToDays (calSecond) + 3) / 7;
		return (weekFirst - weekSecond);
	}

	/**
	 * Get the difference in months between two dates.
	 *
	 * @return		The difference in days between two dates.
	 * @param calFirst	The first date.
	 * @param calSecond	The second date.
	 */
	static public int getDiffMonths (Calendar calFirst, Calendar calSecond)
	{
		int monthFirst, monthSecond;

		monthFirst = calFirst.get (Calendar.YEAR) * 12 + calFirst.get (Calendar.MONTH);
		monthSecond = calSecond.get (Calendar.YEAR) * 12 + calSecond.get (Calendar.MONTH);
		return (monthSecond - monthFirst);
	}

	/**
	 * This method returns next month
	 * to the input
	 */
	static public String getNextMonth (String month)
	{
		if (month.equalsIgnoreCase("Start")){

			GregorianCalendar currentDate = currentCalendar ();
			return monthName[currentDate.get(Calendar.MONTH)];

		} else if (month.equalsIgnoreCase("January")){
			return "February";
		} else if (month.equalsIgnoreCase("February")){
			return "March";
		} else if (month.equalsIgnoreCase("March")){
			return "April";
		} else if (month.equalsIgnoreCase("April")){
			return "May";
		} else if (month.equalsIgnoreCase("May")){
			return "June";
		} else if (month.equalsIgnoreCase("June")){
			return "July";
		} else if (month.equalsIgnoreCase("July")){
			return "August";
		} else if (month.equalsIgnoreCase("August")){
			return "September";
		} else if (month.equalsIgnoreCase("September")){
			return "October";
		} else if (month.equalsIgnoreCase("October")){
			return "November";
		} else if (month.equalsIgnoreCase("November")){
			return "December";
		} else if (month.equalsIgnoreCase("December")){
			return "January";
		}
		return null;
	}
	/************************************************
	* This method convert a integer string into integer
	***********************************************/
    static public int getStringInt(String s) {
        return (new Integer(s)).intValue();
    }

	static private long getCalendarOffset (Calendar calendar)
	{
		return (
			calendar.get (GregorianCalendar.ZONE_OFFSET) +
			calendar.get (GregorianCalendar.DST_OFFSET)
		);
	}

	/*
	* Convert epochDay to String in given format
	*/
	
	static public String epochDayToString (int epochDay,SimpleDateFormat format){
		GregorianCalendar cal;

		cal = TimeHelper.daysToCalendar (epochDay);
		return (format.format (cal.getTime ()));
	}
		
	/*
	* Convert epochDay to String in outputDateFormat
	*/	
	static public String epochDayToString (int epochDay)
	{
		GregorianCalendar cal;

		cal = TimeHelper.daysToCalendar (epochDay);
		return (outputDateFormat.format (cal.getTime ()));
	}
	static public String epochDayToShortString (int epochDay)
	{
		GregorianCalendar cal;

		cal = TimeHelper.daysToCalendar (epochDay);
		return (shortDateFormat.format (cal.getTime ()));
	}

	static public String epochDayToDayInWeek (int epochDay)
	{
		GregorianCalendar cal;

		cal = TimeHelper.daysToCalendar (epochDay);
		return (dayInWeekFormat.format (cal.getTime ()));
	}

	/**
	 * Convert daySeconds value to time-of-day.
	 *
	 * Note that this method should be used
	 * for timeZone neutral time display such as appt time.
	 *
	 * Logging time for user transaction, however, is NOT timeZone neutral and
	 * should be formatted with a user's local timeZone by
	 * the overload method provided below. example, if
	 * a patient cancelled an appt at 8:10 AM and he is in US central
	 * time zone, the log display should be formatted to
	 * that time Zone
	 */
	static public String daySecondsToString (int seconds)
	{
		GregorianCalendar cal;

		cal = secondsToCalendar (seconds);
		return (outputTimeFormat.format (cal.getTime ()));
	}
	/**
	 * Convert daySeconds value to time-of-day for given
	 * time zone. MUST NOT be used to format appt time of
	 * scheduler as they are time Zone neutral.
	 *
	 * Logging time for user transaction, however, is NOT timeZone neutral and
	 * should be formatted with a user's local timeZone.
	 * example, if a patient cancelled an appt at 8:10 AM and he is in US central
	 * time zone, the log display should be formatted to
	 * that time Zone.
	 */
	static public String daySecondsToString (
		int seconds,
		String timeZone,
		SimpleDateFormat myFormat
	)
	{
		GregorianCalendar cal = secondsToCalendar (seconds);
		//convert this US/estaren cal time to given time zone
		if (timeZone.equalsIgnoreCase("America/Chicago")){
			cal.add(Calendar.HOUR, -1);
		}else if (timeZone.equalsIgnoreCase("Canada/Central")){
			cal.add(Calendar.HOUR, -1);
		}else if (timeZone.equalsIgnoreCase("America/Denver")){
			cal.add(Calendar.HOUR, -2);
		}else if (timeZone.equalsIgnoreCase("Canada/Mountain")){
			cal.add(Calendar.HOUR, -2);
		}else if (timeZone.equalsIgnoreCase("America/Los_Angeles")){
			cal.add(Calendar.HOUR, -3);
		}else if (timeZone.equalsIgnoreCase("Canada/Pacific")){
			cal.add(Calendar.HOUR, -3);
		}else if (timeZone.equalsIgnoreCase("Canada/Atlantic")){
			cal.add(Calendar.HOUR, 1);
		}else if (timeZone.equalsIgnoreCase("Canada/Newfoundland")){
			cal.add(Calendar.MINUTE, 90);
		}else if (timeZone.equalsIgnoreCase("America/Anchorage")){
			cal.add(Calendar.HOUR, -4);
		}else if (timeZone.equalsIgnoreCase("America/Adak")){
			cal.add(Calendar.HOUR, -6);
		}else if (timeZone.equalsIgnoreCase("Europe/London")){
			cal.add(Calendar.HOUR, +5);
		}else if (timeZone.equalsIgnoreCase("Australia/Sydney")){
			cal.add(Calendar.HOUR, +16);
		}else{
			//use same zone
		}

		String myTime = myFormat.format (cal.getTime ());
		logger.debug("timeZone "+timeZone + " formatted time is " + myTime);
		return myTime;
	}
	/**
	 * This method convert date from input format to epochDay
	 */
	static public int stringToEpochDay (String dateString)
	{
		ParsePosition pos;
		Date dateObj;
		GregorianCalendar calendar;

		pos = new ParsePosition (0);
		if ((dateObj = inputDateFormat.parse (dateString,  pos)) == null) {
			return (-1);
		}
		if (pos.getIndex () != dateString.length ()) {
			return (-1);
		}
		calendar = new GregorianCalendar ();
		calendar.setTime (dateObj);
		return (calendarToDays (calendar));
	}

	/**
	 * This method convert date from a given format to epochDay
	 */
	static public int stringToEpochDay (String dateString, SimpleDateFormat format)
	{
		ParsePosition pos;
		Date dateObj;
		GregorianCalendar calendar;

		pos = new ParsePosition (0);
		if ((dateObj = format.parse (dateString,  pos)) == null) {
			return (-1);
		}
		if (pos.getIndex () != dateString.length ()) {
			return (-1);
		}
		calendar = new GregorianCalendar ();
		calendar.setTime (dateObj);
		return (calendarToDays (calendar));
	}

	/**
	 * This method convert time from input format to daySeconds
	 */
	static public int stringToDaySeconds (String secondString)
	{
		ParsePosition pos;
		Date dateObj;
		GregorianCalendar calendar;

		pos = new ParsePosition (0);
		if ((dateObj = inputTimeFormat.parse (secondString,  pos)) == null) {
			return (-1);
		}
		if (pos.getIndex () != secondString.length ()) {
			return (-1);
		}
		/*
		NB: Setting time zone here will cause appt be saved onto
		wrong time slot. We had a bug cleared here.
		*/
		calendar = new GregorianCalendar ();
		calendar.setTime (dateObj);
		return (calendarToSeconds (calendar));
	}

	/**
	 * This method parses a date and returns a GregorianCalendar object or null on parse failure.
	 *
	 * @return		Return a GregorianCalendar object describing the resulting date/time.
	 * @param format	Description of passed date string in SimpleDateFormat
	 * @param dateString	String that must be parsed
	 */
	static public GregorianCalendar parseDate (String dateString, String format)
	{
		SimpleDateFormat simpleDateFormat;
		ParsePosition pos;
		Date dateObj;
		GregorianCalendar calendar;

		simpleDateFormat = new SimpleDateFormat (format);
		pos = new ParsePosition (0);
		if ((dateObj = simpleDateFormat.parse (dateString,  pos)) == null) {
			return (null);
		}
		if (pos.getIndex () != dateString.length ()) {
			return (null);
		}
		calendar = new GregorianCalendar ();
		calendar.setTime (dateObj);
		return (calendar);
	}

	/**
	 * This method parses a date and returns a GregorianCalendar object or null on parse failure.
	 *
	 * @return		Return a GregorianCalendar object describing the resulting date/time.
	 * @param format	Description of passed date string in SimpleDateFormat
	 * @param dateString	String that must be parsed
	 */
	static public GregorianCalendar parseDate (String dateString, SimpleDateFormat format)
	{
		ParsePosition pos;
		Date dateObj;
		GregorianCalendar calendar;

		pos = new ParsePosition (0);
		if ((dateObj = format.parse (dateString,  pos)) == null) {
			return (null);
		}
		if (pos.getIndex () != dateString.length ()) {
			return (null);
		}
		calendar = new GregorianCalendar ();
		calendar.setTime (dateObj);
		return (calendar);
	}

	/**
	 * This method parses a MONDAY date in output Date format
	 * and check if the week is this week or next.
	 *
	 * @return		Return a String "this week", "next week", or input String itself.
	 * @param dateString	Description of date string in output date format
	 */
	static public String whichWeek (String mondayDateString)
	{
		if (mondayDateString == null) return mondayDateString;

		int mondayEpochDay = stringToEpochDay(mondayDateString, outputDateFormat);
		if (mondayEpochDay == -1) {
			return mondayDateString;
		}
		int today = currentEpochDay();
		int todayNextWeek = today + 7;
		if ((mondayEpochDay <= today)&&(Math.abs(mondayEpochDay - today) < 7)){
			return "this week";
		}else if ((mondayEpochDay > today)&&(Math.abs(mondayEpochDay - today) < 8)){
			return "next week";
		}else{
			return mondayDateString;
		}
	}

	/**
	 * Added by mathias retuns the second of the day for the given
	 * {@link Calendar}.
	 */
	public static int calendarToDaySecond(Calendar calendar) {
		return calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60
				+ calendar.get(Calendar.SECOND);
	}

	/**
	 * Added by mathias. Calculates the difference in seconds between two
	 * timezones.
	 *
	 * @param tzSource
	 * @param tzDest
	 * @return
	 */
	public static int getTimezoneDifference(TimeZone tzSource, TimeZone tzDest) {
		Date date = new Date();
		Calendar calSource = new GregorianCalendar(tzSource);
		Calendar calDest = new GregorianCalendar(tzDest);
		calSource.setTime(date);
		calDest.setTime(date);
		int offsetMS = 0;
		int gmtOffsetSource = calSource.get(Calendar.ZONE_OFFSET) + calSource.get(Calendar.DST_OFFSET);
		int gmtOffsetDest = calDest.get(Calendar.ZONE_OFFSET) + calDest.get(Calendar.DST_OFFSET);
		if (gmtOffsetSource > 0 && gmtOffsetDest > 0 || gmtOffsetSource < 0 && gmtOffsetDest < 0) {
			offsetMS = Math.abs(gmtOffsetSource - gmtOffsetDest);
		} else {
			offsetMS = Math.abs(gmtOffsetSource) + Math.abs(gmtOffsetDest);
		}
		if (gmtOffsetSource > gmtOffsetDest) {
			offsetMS *= -1;
		}
		return offsetMS / 1000;
	}


}
