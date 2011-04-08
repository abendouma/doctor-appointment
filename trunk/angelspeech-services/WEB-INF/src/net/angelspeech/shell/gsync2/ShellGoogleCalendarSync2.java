//package net.angelspeech.shell.gsync2;
//
//import java.text.SimpleDateFormat;
//
//import java.util.GregorianCalendar;
//import java.util.HashMap;
//
//import net.angelspeech.object.EmailHelper;
//import net.angelspeech.object.NotifyHelper;
//import net.angelspeech.object.SettingsHelper;
//import net.angelspeech.object.TimeHelper;
//
//import org.apache.log4j.Logger;
//import org.apache.naming.config.XmlConfigurator;
//
///**
// * This class sends schedule data to Google Calendar.
// */
//public class ShellGoogleCalendarSync2 {
//
//	private static Logger logger = org.apache.log4j.Logger.getLogger(ShellGoogleCalendarSync2.class);
//
//	static public void main(String[] args) {
//		try {
//			runGoogleCalendarSync2();
//		} catch (Exception ex) {
//			// Alert admin when code exceptions are caught
//			String alertMessage = "ShellGoogleCalendarSync2.main() \n" + NotifyHelper.getStackTrace(ex);
//			String cronJobName = "Google Calendar Sync2";
//			System.out.println("Catch Exception " + ex.toString());
//			NotifyHelper.sendAdminAlertEmail(cronJobName, alertMessage);
//		}
//	}
//
//	static private void runGoogleCalendarSync2() throws Exception {
//		logger.debug("Starting method runGoogleCalendarSync2 in class ShellGoogleCalendarSync2");
//		GoogleCalendarSync2 googleCalendarSync2;
//		int timeStart, timeEnd, durationInSecond;
//
//		logger.debug("Loading XML configuration");
//		XmlConfigurator.loadConfiguration(new FileInputStream("conf/resources.xml"));
//		// read doctor group size configured by doctorId range
//		int doctorIdStart = SettingsHelper.readInt("reminderRangeStart", SettingsHelper.PerformanceSettings);
//		int doctorIdEnd = SettingsHelper.readInt("reminderRangeEnd", SettingsHelper.PerformanceSettings);
//		String serverPairNames = SettingsHelper.readString("reminder.serverPairNames", SettingsHelper.ReminderSettings);
//		if (doctorIdEnd < doctorIdStart) {
//			throw new Exception("doctorIdEnd is samller than doctorIdStart in PerformanceSettings.properties");
//		}
//
//		googleCalendarSync2 = new GoogleCalendarSync2();
//		timeStart = TimeHelper.currentEpochSecond();
//		logger.debug("Starting synchronization action");
//		HashMap<String, Integer> totalCount = googleCalendarSync2.synchronize();
//		timeEnd = TimeHelper.currentEpochSecond();
//		durationInSecond = timeEnd - timeStart;
//		System.out.println("Google Calendar Data Sync2 report by " + serverPairNames + ".\n" + "Started on "
//				+ secondsToString(timeStart) + ".\n" + "Stopped on " + secondsToString(timeEnd) + ".\n" + "Run-time "
//				+ String.valueOf(durationInSecond / 60) + " minutes " + String.valueOf(durationInSecond % 60)
//				+ " seconds.\n" + "A Total of " + String.valueOf(totalCount.get(GoogleSyncDataHolder2.RECEIVED_KEY))
//				+ " Google calendar events have been successfully added to AngelSpeech Schedulers, \n" + "A Total of "
//				+ String.valueOf(totalCount.get(GoogleSyncDataHolder2.RESCHEDULED_KEY))
//				+ " Google calendar events have been successfully rescheduled in AngelSpeech, \n" + "A total of "
//				+ +totalCount.get(GoogleSyncDataHolder2.CANCEL_KEY)
//				+ " Google calendar events have been successfully cancelled from AngelSpeech Schedulers.\n"
//				+ "A total of " + +totalCount.get(GoogleSyncDataHolder2.RECURRENT_RECEIVED_KEY)
//				+ " Recurrent google calendar events have been successfully added to AngelSpeech Schedulers.\n"
//				+ "A total of " + +totalCount.get(GoogleSyncDataHolder2.RECURRENT_CANCELED_KEY)
//				+ " Recurrent google calendar events have been successfully removed from AngelSpeech Schedulers.\n"
//				+ "for appointments scheduled between \n\n" + TimeHelper.epochDayToString(TimeHelper.currentEpochDay())
//				+ " and " + TimeHelper.epochDayToString(googleCalendarSync2.dataHolder.getEpochDayEnd()) + "\n\n");
//
//		/* Send a execution report email to admin */
//		EmailHelper
//				.send(
//						"myReminder@angelspeech.com",
//						SettingsHelper.readString("copy.support.toAddress", SettingsHelper.EmailSettings),
//						"RE: Google Calendar Data sync2 cron job execution result",
//						"Google Calendar Data Sync2 report by "
//								+ serverPairNames
//								+ ".\n\n"
//								+ "Started on "
//								+ secondsToString(timeStart)
//								+ ".\n"
//								+ "Stopped on "
//								+ secondsToString(timeEnd)
//								+ ".\n"
//								+ "Run-time "
//								+ String.valueOf(timeEnd - timeStart)
//								+ " seconds.\n"
//								+ "A total of "
//								+ totalCount.get(GoogleSyncDataHolder2.RECEIVED_KEY)
//								+ " Google calendar events have been successfully added to AngelSpeech Schedulers, \n"
//								+ "A total of "
//								+ totalCount.get(GoogleSyncDataHolder2.RESCHEDULED_KEY)
//								+ " Google calendar events have been successfully resheduled in AngelSpeech Schedulers, \n"
//								+ "A total of "
//								+ totalCount.get(GoogleSyncDataHolder2.CANCEL_KEY)
//								+ " Google calendar events have been successfully cancelled from AngelSpeech Schedulers.\n"
//								+ "A total of "
//								+ totalCount.get(GoogleSyncDataHolder2.RECURRENT_RECEIVED_KEY)
//								+ " Recurrent google calendar events have been successfully added to AngelSpeech Schedulers, \n"
//								+ "A total of "
//								+ totalCount.get(GoogleSyncDataHolder2.RECURRENT_RESCHEDULED_KEY)
//								+ " Recurrent google calendar events have been successfully resheduled in AngelSpeech Schedulers, \n"
//								+ "A total of "
//								+ totalCount.get(GoogleSyncDataHolder2.RECURRENT_CANCELED_KEY)
//								+ " Recurrent google calendar events have been successfully removed from AngelSpeech Schedulers, \n"
//								+ "for appointments scheduled between \n\n"
//								+ TimeHelper.epochDayToString(TimeHelper.currentEpochDay()) + " and "
//								+ TimeHelper.epochDayToString(googleCalendarSync2.dataHolder.getEpochDayEnd())
//								+ "\n\n\n", false);
//		// if exception is thrown in any thread then re-throw error to print
//		// exception in email as well
//		if (googleCalendarSync2.getError() != null) {
//			throw googleCalendarSync2.getError();
//		}
//	}
//
//	public static String secondsToString(int seconds) {
//		GregorianCalendar cal;
//		SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//
//		cal = TimeHelper.secondsToCalendar(seconds);
//		return (outputFormat.format(cal.getTime()));
//	}
//}
