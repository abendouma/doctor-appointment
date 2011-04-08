/**
 * This helper class is written based on the Gdata API sample
 * Java class CalendarFeedDemo.java and EventFeedDemo.java
 */
package net.angelspeech.object;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;

import net.angelspeech.database.ApptLocationRecord;
import net.angelspeech.database.CallRecord;
import net.angelspeech.database.DoctorRecord;
import net.angelspeech.database.PatientRecord;
import net.angelspeech.database.ScheduleApptRecord;
import net.angelspeech.database.SqlQuery;

import org.apache.log4j.Logger;

import com.google.gdata.client.Query;
import com.google.gdata.client.Query.CustomParameter;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.batch.BatchOperationType;
import com.google.gdata.data.batch.BatchStatus;
import com.google.gdata.data.batch.BatchUtils;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.calendar.ColorProperty;
import com.google.gdata.data.calendar.HiddenProperty;
import com.google.gdata.data.calendar.SelectedProperty;
import com.google.gdata.data.calendar.TimeZoneProperty;
import com.google.gdata.data.calendar.WebContent;
import com.google.gdata.data.extensions.ExtendedProperty;
import com.google.gdata.data.extensions.Recurrence;
import com.google.gdata.data.extensions.Reminder;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;
import com.google.gdata.data.extensions.BaseEventEntry.EventStatus;
import com.google.gdata.data.extensions.Reminder.Method;
import com.google.gdata.util.ServiceException;

public class GCalendarHelper {

	static private Logger logger = Logger.getLogger(GCalendarHelper.class);

	// The base URL for a user's calendar metafeed (needs a username appended).
	public static final String METAFEED_URL_BASE = "http://www.google.com/calendar/feeds/";

	// The string to add to the user's metafeedUrl to access the allcalendars
	// feed.
	private static final String ALLCALENDARS_FEED_URL_SUFFIX = "/allcalendars/full";

	// The string to add to the user's metafeedUrl to access the owncalendars
	// feed.
	public static final String OWNCALENDARS_FEED_URL_SUFFIX = "/owncalendars/full";

	// The URL for the metafeed of the specified user.
	// (e.g. http://www.google.com/feeds/calendar/jdoe@gmail.com)
	public static URL metafeedUrl = null;

	// The URL for the allcalendars feed of the specified user.
	// (e.g.
	// http://www.googe.com/feeds/calendar/jdoe@gmail.com/allcalendars/full)
	public static URL allcalendarsFeedUrl = null;

	// The URL for the owncalendars feed of the specified user.
	// (e.g.
	// http://www.googe.com/feeds/calendar/jdoe@gmail.com/owncalendars/full)
	public static URL owncalendarsFeedUrl = null;

	// The calendar ID of the public Google Doodles calendar
	private static final String DOODLES_CALENDAR_ID = "c4o4i7m2lbamc4k26sc2vokh5g%40group.calendar.google.com";

	// The HEX representation of red, blue and green
	private static final String RED = "#A32929";
	private static final String BLUE = "#2952A3";
	private static final String GREEN = "#0D7813";

	// The string to add to the user's metafeedUrl to access the event feed for
	// their primary calendar.
	public static final String EVENT_FEED_URL_SUFFIX = "/private/full";

	private static final String SHOW_DELETED_EVENTS_PARAMETER = "showdeleted";

	// The URL for the event feed of the specified user's primary calendar.
	// (e.g. http://www.googe.com/feeds/calendar/jdoe@gmail.com/private/full)
	public static URL eventFeedUrl = null;

	// Added by Mathias: This is the name of the ExtendedProperty that defines
	// if an appointment
	// in google calendar has been synced with AngelSpeech
	public static final String ANGELSPEECH_APPOINTMENT_ID_EP_TAG = "http://schemas.google.com/g/2005#as.apptId";

	//private static final String ANGELSPEECH_TIMESTAMP_TAG = "http://schemas.google.com/g/2005#as.ts";

	/**
	 * Prints the titles of calendars in the feed specified by the given URL.
	 *
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param feedUrl
	 *            The URL of a calendar feed to retrieve.
	 * @throws IOException
	 *             If there is a problem communicating with the server.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 */
	public static void printUserCalendars(CalendarService service, URL feedUrl) throws IOException, ServiceException {

		// Send the request and receive the response:
		CalendarFeed resultFeed = service.getFeed(feedUrl, CalendarFeed.class);

		// Print the title of each calendar
		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			CalendarEntry entry = resultFeed.getEntries().get(i);
			logger.debug("\t" + entry.getTitle().getPlainText());
		}
	}

	/**
	 * Creates a new secondary calendar using the owncalendars feed.
	 *
	 * @param service
	 *            An authenticated CalendarService object.
	 * @return The newly created calendar entry.
	 * @throws IOException
	 *             If there is a problem communicating with the server.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 */
	public static CalendarEntry createCalendar(CalendarService service) throws IOException, ServiceException {
		logger.debug("Creating a secondary calendar");

		// Create the calendar
		CalendarEntry calendar = new CalendarEntry();
		calendar.setTitle(new PlainTextConstruct("Little League Schedule"));
		calendar.setSummary(new PlainTextConstruct("This calendar contains the practice schedule and game times."));
		calendar.setTimeZone(new TimeZoneProperty("America/Los_Angeles"));
		calendar.setHidden(HiddenProperty.FALSE);
		calendar.setColor(new ColorProperty(BLUE));
		calendar.addLocation(new Where("", "", "Oakland"));

		// Insert the calendar
		return service.insert(owncalendarsFeedUrl, calendar);
	}

	/**
	 * Updates the title, color, and selected properties of the given calendar
	 * entry using the owncalendars feed. Note that the title can only be
	 * updated with the owncalendars feed.
	 *
	 * @param calendar
	 *            The calendar entry to update.
	 * @return The newly updated calendar entry.
	 * @throws IOException
	 *             If there is a problem communicating with the server.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 */
	public static CalendarEntry updateCalendar(CalendarEntry calendar) throws IOException, ServiceException {
		logger.debug("Updating the secondary calendar");

		calendar.setTitle(new PlainTextConstruct("New title"));
		calendar.setColor(new ColorProperty(GREEN));
		calendar.setSelected(SelectedProperty.TRUE);
		return calendar.update();
	}

	/**
	 * Deletes the given calendar entry.
	 *
	 * @param calendar
	 *            The calendar entry to delete.
	 * @throws IOException
	 *             If there is a problem communicating with the server.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 */
	public static void deleteCalendar(CalendarEntry calendar) throws IOException, ServiceException {
		logger.debug("Deleting the secondary calendar");
		calendar.delete();
	}

	public static void printUserCalendars(CalendarService service) throws IOException, ServiceException {
		// Send the request and receive the response:
		CalendarFeed resultFeed = service.getFeed(metafeedUrl, CalendarFeed.class);

		logger.debug("Your calendars:");

		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			CalendarEntry entry = resultFeed.getEntries().get(i);
			logger.debug("\t" + entry.getTitle().getPlainText());
		}
		logger.debug(" ");
	}

	/**
	 * Prints the titles of all events on the calendar specified by {@code
	 * feedUri}.
	 *
	 * @param service
	 *            An authenticated CalendarService object.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static void printAllEvents(CalendarService service) throws ServiceException, IOException {
		// Send the request and receive the response:
		CalendarEventFeed resultFeed = service.getFeed(eventFeedUrl, CalendarEventFeed.class);

		logger.debug("All events on your calendar:");
		logger.debug(" ");
		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			CalendarEventEntry entry = resultFeed.getEntries().get(i);
			logger.debug("\t" + entry.getTitle().getPlainText());
		}
		logger.debug(" ");
	}

	/**
	 * Prints the titles of all events matching a full-text query.
	 *
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param query
	 *            The text for which to query.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static void fullTextQuery(CalendarService service, String query) throws ServiceException, IOException {
		Query myQuery = new Query(eventFeedUrl);
		myQuery.setFullTextQuery("Tennis");

		CalendarEventFeed resultFeed = service.query(myQuery, CalendarEventFeed.class);

		logger.debug("Events matching " + query + ":");
		logger.debug(" ");
		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			CalendarEventEntry entry = resultFeed.getEntries().get(i);
			logger.debug("\t" + entry.getTitle().getPlainText());
		}
		logger.debug(" ");
	}

	/**
	 * Prints the titles of all events in a specified date/time range.
	 *
	 * Modified by Mathias: Added userName parameter. This parameter is needed
	 * if {@link #eventFeedUrl} hasn't been initialized before. If it has
	 * already been initialized, this parameter can be <code>null</code>.
	 *
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param startTime
	 *            Start time (inclusive) of events to print.
	 * @param endTime
	 *            End time (exclusive) of events to print.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static List<CalendarEventEntry> dateRangeQuery(CalendarService service, DateTime startTime,
			DateTime endTime, String userName, boolean getDeletedEvents) throws ServiceException, IOException {
		URL feedURL = eventFeedUrl;
		if (userName != null) {
			if (userName.indexOf('@') == -1) {
				userName = userName + "@gmail.com";
			}
			String urlString = METAFEED_URL_BASE + userName + EVENT_FEED_URL_SUFFIX;
			// if (getDeletedEvents) {
			// urlString += SHOW_DELETED_EVENTS_SUFFIX;
			// }
			feedURL = new URL(urlString);
		}
		CalendarQuery myQuery = new CalendarQuery(feedURL);
		if (getDeletedEvents) {
			myQuery.setStringCustomParameter(SHOW_DELETED_EVENTS_PARAMETER, "true");
		}
		myQuery.setMinimumStartTime(startTime);
		myQuery.setMaximumStartTime(endTime);

		// Send the request and receive the response:
		CalendarEventFeed resultFeed = service.query(myQuery, CalendarEventFeed.class);

		logger.debug("Events from " + startTime.toString() + " to " + endTime.toString() + ":");
		logger.debug(" ");
		for (int i = 0; i < resultFeed.getEntries().size(); i++) {
			CalendarEventEntry entry = resultFeed.getEntries().get(i);
			logger.debug("\t" + entry.getTitle().getPlainText());
		}
		logger.debug(" ");
		return resultFeed.getEntries();
	}

	/**
	 * Helper method to create either single-instance or recurring events. For
	 * simplicity, some values that might normally be passed as parameters (such
	 * as author name, email, etc.) are hard-coded.
	 *
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param eventTitle
	 *            Title of the event to create.
	 * @param eventContent
	 *            Text content of the event to create.
	 * @param recurData
	 *            Recurrence value for the event, or null for single-instance
	 *            events.
	 * @param isQuickAdd
	 *            True if eventContent should be interpreted as the text of a
	 *            quick add event.
	 * @param wc
	 *            A WebContent object, or null if this is not a web content
	 *            event.
	 * @return The newly-created CalendarEventEntry.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static CalendarEventEntry createEvent(CalendarService service, String eventTitle, String eventContent,
			String recurData, boolean isQuickAdd, WebContent wc) throws ServiceException, IOException {
		CalendarEventEntry myEntry = new CalendarEventEntry();

		myEntry.setTitle(new PlainTextConstruct(eventTitle));
		myEntry.setContent(new PlainTextConstruct(eventContent));
		myEntry.setQuickAdd(isQuickAdd);
		myEntry.setWebContent(wc);

		// If a recurrence was requested, add it. Otherwise, set the
		// time (the current date and time) and duration (30 minutes)
		// of the event.
		if (recurData == null) {
			Calendar calendar = new GregorianCalendar();
			DateTime startTime = new DateTime(calendar.getTime(), TimeZone.getDefault());

			calendar.add(Calendar.MINUTE, 30);
			DateTime endTime = new DateTime(calendar.getTime(), TimeZone.getDefault());

			When eventTimes = new When();
			eventTimes.setStartTime(startTime);
			eventTimes.setEndTime(endTime);
			myEntry.addTime(eventTimes);
		} else {
			Recurrence recur = new Recurrence();
			recur.setValue(recurData);
			myEntry.setRecurrence(recur);
		}

		// Send the request and receive the response:
		return service.insert(eventFeedUrl, myEntry);
	}

	/**
	 * Creates a single-occurrence event.
	 *
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param eventTitle
	 *            Title of the event to create.
	 * @param eventContent
	 *            Text content of the event to create.
	 * @return The newly-created CalendarEventEntry.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static CalendarEventEntry createSingleEvent(CalendarService service, String eventTitle, String eventContent)
			throws ServiceException, IOException {
		return createEvent(service, eventTitle, eventContent, null, false, null);
	}

	/**
	 * Creates a quick add event.
	 *
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param quickAddContent
	 *            The quick add text, including the event title, date and time.
	 * @return The newly-created CalendarEventEntry.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static CalendarEventEntry createQuickAddEvent(CalendarService service, String quickAddContent)
			throws ServiceException, IOException {
		return createEvent(service, null, quickAddContent, null, true, null);
	}

	/**
	 * Creates a web content event.
	 *
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param title
	 *            The title of the web content event.
	 * @param type
	 *            The MIME type of the web content event, e.g. "image/gif"
	 * @param url
	 *            The URL of the content to display in the web content window.
	 * @param icon
	 *            The icon to display in the main Calendar user interface.
	 * @param width
	 *            The width of the web content window.
	 * @param height
	 *            The height of the web content window.
	 * @return The newly-created CalendarEventEntry.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static CalendarEventEntry createWebContentEvent(CalendarService service, String title, String type,
			String url, String icon, String width, String height) throws ServiceException, IOException {
		WebContent wc = new WebContent();

		wc.setHeight(height);
		wc.setWidth(width);
		wc.setTitle(title);
		wc.setType(type);
		wc.setUrl(url);
		wc.setIcon(icon);

		return createEvent(service, title, null, null, false, wc);
	}

	/**
	 * Creates a new recurring event.
	 *
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param eventTitle
	 *            Title of the event to create.
	 * @param eventContent
	 *            Text content of the event to create.
	 * @return The newly-created CalendarEventEntry.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static CalendarEventEntry createRecurringEvent(CalendarService service, String eventTitle,
			String eventContent) throws ServiceException, IOException {
		// Specify a recurring event that occurs every Tuesday from May 1,
		// 2007 through September 4, 2007. Note that we are using iCal (RFC
		// 2445)
		// syntax; see http://www.ietf.org/rfc/rfc2445.txt for more information.
		String recurData = "DTSTART;VALUE=DATE:20070501\r\n" + "DTEND;VALUE=DATE:20070502\r\n"
				+ "RRULE:FREQ=WEEKLY;BYDAY=Tu;UNTIL=20070904\r\n";

		return createEvent(service, eventTitle, eventContent, recurData, false, null);
	}

	/**
	 * Updates the title of an existing calendar event.
	 *
	 * @param entry
	 *            The event to update.
	 * @param newTitle
	 *            The new title for this event.
	 * @return The updated CalendarEventEntry object.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static CalendarEventEntry updateTitle(CalendarEventEntry entry, String newTitle) throws ServiceException,
			IOException {
		entry.setTitle(new PlainTextConstruct(newTitle));
		return entry.update();
	}

	/**
	 * Adds a reminder to a calendar event.
	 *
	 * @param entry
	 *            The event to update.
	 * @param numMinutes
	 *            Reminder time, in minutes.
	 * @param methodType
	 *            Method of notification (e.g. email, alert, sms).
	 * @return The updated EventEntry object.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static CalendarEventEntry addReminder(CalendarEventEntry entry, int numMinutes, Method methodType)
			throws ServiceException, IOException {
		Reminder reminder = new Reminder();
		reminder.setMinutes(numMinutes);
		reminder.setMethod(methodType);
		entry.getReminder().add(reminder);

		return entry.update();
	}

	/**
	 * Adds an extended property to a calendar event.
	 *
	 * @param entry
	 *            The event to update.
	 * @return The updated EventEntry object.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static CalendarEventEntry addExtendedProperty(CalendarEventEntry entry) throws ServiceException, IOException {
		// Add an extended property "id" with value 1234 to the EventEntry
		// entry.
		// We specify the complete schema URL to avoid namespace collisions with
		// other applications that use the same property name.
		ExtendedProperty property = new ExtendedProperty();
		property.setName("http://www.example.com/schemas/2005#mycal.id");
		property.setValue("1234");

		entry.addExtension(property);

		return entry.update();
	}

	/**
	 * Makes a batch request to delete all the events in the given list. If any
	 * of the operations fails, the errors returned from the server are
	 * displayed. The CalendarEntry objects in the list given as a parameters
	 * must be entries returned from the server that contain valid edit links
	 * (for optimistic concurrency to work). Note: You can add entries to a
	 * batch request for the other operation types (INSERT, QUERY, and UPDATE)
	 * in the same manner as shown below for DELETE operations.
	 *
	 * @param service
	 *            An authenticated CalendarService object.
	 * @param eventsToDelete
	 *            A list of CalendarEventEntry objects to delete.
	 * @throws ServiceException
	 *             If the service is unable to handle the request.
	 * @throws IOException
	 *             Error communicating with the server.
	 */
	public static void deleteEvents(CalendarService service, List<CalendarEventEntry> eventsToDelete)
			throws ServiceException, IOException {

		// Add each item in eventsToDelete to the batch request.
		CalendarEventFeed batchRequest = new CalendarEventFeed();
		for (int i = 0; i < eventsToDelete.size(); i++) {
			CalendarEventEntry toDelete = eventsToDelete.get(i);
			// Modify the entry toDelete with batch ID and operation type.
			BatchUtils.setBatchId(toDelete, String.valueOf(i));
			BatchUtils.setBatchOperationType(toDelete, BatchOperationType.DELETE);
			batchRequest.getEntries().add(toDelete);
		}

		// Get the URL to make batch requests to
		CalendarEventFeed feed = service.getFeed(eventFeedUrl, CalendarEventFeed.class);
		Link batchLink = feed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
		URL batchUrl = new URL(batchLink.getHref());

		// Submit the batch request
		CalendarEventFeed batchResponse = service.batch(batchUrl, batchRequest);

		// Ensure that all the operations were successful.
		boolean isSuccess = true;
		for (CalendarEventEntry entry : batchResponse.getEntries()) {
			String batchId = BatchUtils.getBatchId(entry);
			if (!BatchUtils.isSuccess(entry)) {
				isSuccess = false;
				BatchStatus status = BatchUtils.getBatchStatus(entry);
				logger.info("\n" + batchId + " failed (" + status.getReason() + ") " + status.getContent());
			}
		}
		if (isSuccess) {
			logger.debug("Successfully deleted all events via batch request.");
		}
	}

	/**
	 * Instantiates a CalendarService object and uses the command line arguments
	 * to authenticate. The CalendarService object is used to demonstrate
	 * interactions with the Calendar data API's event feed.
	 *
	 * @param args
	 *            Must be length 2 and contain a valid username/password
	 */
	public static void main(String[] args) {
		CalendarService myService = new CalendarService("exampleCo-exampleApp-1");

		// Set username and password from command-line arguments.
		if (args.length != 2) {
			usage();
			return;
		}

		String userName = args[0];
		String userPassword = args[1];

		// Create the necessary URL objects.
		try {
			metafeedUrl = new URL(METAFEED_URL_BASE + userName);
			eventFeedUrl = new URL(METAFEED_URL_BASE + userName + EVENT_FEED_URL_SUFFIX);
		} catch (MalformedURLException e) {
			// Bad URL
			logger.error("Uh oh - you've got an invalid URL.");
			logger.error(NotifyHelper.getStackTrace(e));
			return;
		}

		try {
			myService.setUserCredentials(userName, userPassword);

			// Demonstrate retrieving a list of the user's calendars.
			printUserCalendars(myService);

			// Demonstrate various feed queries.
			logger.debug("Printing all events");
			printAllEvents(myService);
			logger.info("Full text query");
			fullTextQuery(myService, "Tennis");
			dateRangeQuery(myService, DateTime.parseDate("2007-01-17"), DateTime.parseDate("2009-01-21"), null, false); // Changed
			// by
			// Mathias

			// Demonstrate creating a single-occurrence event.
			CalendarEventEntry singleEvent = createSingleEvent(myService, "Tennis with Mike",
					"Meet for a quick lesson.");
			logger.info("Successfully created event " + singleEvent.getTitle().getPlainText());

			// Demonstrate creating a quick add event.
			CalendarEventEntry quickAddEvent = createQuickAddEvent(myService, "Tennis with John April 11 3pm-3:30pm");
			logger.info("Successfully created quick add event " + quickAddEvent.getTitle().getPlainText());

			// Demonstrate creating a web content event.
			CalendarEventEntry webContentEvent = createWebContentEvent(myService, "World Cup", "image/gif",
					"http://www.google.com/logos/worldcup06.gif",
					"http://www.google.com/calendar/images/google-holiday.gif", "276", "120");
			logger.info("Successfully created web content event " + webContentEvent.getTitle().getPlainText());

			// Demonstrate creating a recurring event.
			CalendarEventEntry recurringEvent = createRecurringEvent(myService, "Tennis with Dan",
					"Weekly tennis lesson.");
			logger.info("Successfully created recurring event " + recurringEvent.getTitle().getPlainText());

			// Demonstrate updating the event's text.
			singleEvent = updateTitle(singleEvent, "Important meeting");
			logger.info("Event's new title is \"" + singleEvent.getTitle().getPlainText() + "\".");

			// Demonstrate adding a reminder. Note that this will only work on a
			// primary calendar.
			singleEvent = addReminder(singleEvent, 15, Method.EMAIL);
			logger.info("Set a " + singleEvent.getReminder().get(0).getMinutes() + " minute "
					+ singleEvent.getReminder().get(0).getMethod() + " reminder for the event.");

			// Demonstrate adding an extended property.
			singleEvent = addExtendedProperty(singleEvent);

			// Demonstrate deleting the entries with a batch request.

			List<CalendarEventEntry> eventsToDelete = new ArrayList<CalendarEventEntry>();
			eventsToDelete.add(singleEvent);
			eventsToDelete.add(quickAddEvent);
			eventsToDelete.add(webContentEvent);
			eventsToDelete.add(recurringEvent);
			deleteEvents(myService, eventsToDelete);
			logger.info("Successfully deleted multiple events in a batch request");

		} catch (IOException e) {
			// Communications error
			logger.error("There was a problem communicating with the service.");
			logger.error(NotifyHelper.getStackTrace(e));
		} catch (ServiceException e) {
			// Server side error
			logger.error("The server had a problem handling your request.");
			logger.error(NotifyHelper.getStackTrace(e));
		}
	}

	/**
	 * Prints the command line usage of this sample application.
	 */
	private static void usage() {
		logger.debug("Syntax: CalendarFeedDemo <username> <password>");
		logger.debug("\nThe username and password are used for "
				+ "authentication.  The sample application will modify the specified "
				+ "user's calendars so you may want to use a test account.");
	}


	/**
	 * This method is used to construct Google data CalendarEventEntry object,
	 * which is sent to a Google calendar
	 *
	 * @param reminderInfo
	 *            ReminderInfo object
	 * @param timeZone
	 * @return CalendarEventEntry object, which is added to a batch and then
	 *         posted into Google calendar
	 */
	public static CalendarEventEntry getEventByReminderInfo(ReminderInfo reminderInfo, String timeZone)
			throws Exception {
		CalendarEventEntry calendarEvent = new CalendarEventEntry();
		PlainTextConstruct pText = new PlainTextConstruct();
		StringBuilder sb = new StringBuilder();
		sb.append(reminderInfo.patientRecord.firstName);
		sb.append(" ");
		sb.append(reminderInfo.patientRecord.middleName);
		sb.append(" ");
		sb.append(reminderInfo.patientRecord.lastName);
		sb.append(", ");
		sb.append(reminderInfo.patientRecord.phone);
		if (!"0".equals(reminderInfo.appointmentInfo.apptProfileId)) {
			sb.append(", ");
			sb.append(reminderInfo.apptProfileRecord.name);
		}
		pText.setText(sb.toString());
		calendarEvent.setTitle(pText);
		GregorianCalendar calendar = TimeHelper.daysToCalendar(reminderInfo.appointmentInfo.epochDay);
		calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
		calendar.add(Calendar.SECOND, reminderInfo.appointmentInfo.rangeStart);
		DateTime startTime = new DateTime(calendar.getTime(), TimeZone.getTimeZone(timeZone));
		calendar.add(Calendar.SECOND, reminderInfo.appointmentInfo.rangeEnd - reminderInfo.appointmentInfo.rangeStart);
		DateTime endTime = new DateTime(calendar.getTime(), TimeZone.getTimeZone(timeZone));
		When eventTimes = new When();
		eventTimes.setStartTime(startTime);
		eventTimes.setEndTime(endTime);
		calendarEvent.addTime(eventTimes);
		String locationString = null;
		/*
		 * If apptProfileId or apptLocationId is unspecified then use business
		 * name as location
		 */

		if ("0".equals(reminderInfo.appointmentInfo.apptProfileId)) {
			locationString = reminderInfo.doctorRecord.businessName;
		} else {
			if (reminderInfo.apptProfileRecord.apptLocationId.equals("0")) {
				locationString = reminderInfo.doctorRecord.businessName;
			} else {
				ApptLocationRecord apptLocationRecord = new ApptLocationRecord();
				apptLocationRecord.readById(reminderInfo.apptProfileRecord.apptLocationId);
				locationString = apptLocationRecord.businessName;
			}
		}
		Where location = new Where("", "", locationString);
		calendarEvent.addLocation(location);
		calendarEvent.setContent(new PlainTextConstruct(reminderInfo.appointmentInfo.notes));
		logDebugInfo(calendarEvent, reminderInfo.appointmentInfo.apptId,
				"Sending the the following appointment information:");

		return calendarEvent;
	}

	/**
	 * This method is used for printing debug information
	 *
	 * @param calendarEvent
	 *            calendar event object
	 * @param id
	 *            object id
	 * @param debugTitle
	 *            the title of the log string
	 */
	public static void logDebugInfo(CalendarEventEntry calendarEvent, String id, String debugTitle) {
		logger
				.info("\n"
						+ debugTitle
						+ "\n"
						+ "Event id: "
						+ id
						+ "\n"
						+ "Event title: "
						+ calendarEvent.getTitle().getPlainText()
						+ "\n"
						+ "Event decription: "
						+ calendarEvent.getTextContent().getContent().getPlainText()
						+ "\n"
						/*
						 * It is necessary to check locations array for null and
						 * emptyness
						 */
						+ ((calendarEvent.getLocations() != null && calendarEvent.getLocations().size() > 0) ? ("Event location: "
								+ calendarEvent.getLocations().get(0).getValueString() + "\n")
								: "")
						/*
						 * It is necessary to check times array for null and
						 * emptyness
						 */
						+ ((calendarEvent.getTimes() != null && calendarEvent.getTimes().size() > 0) ? ("Event time: "
								+ calendarEvent.getTimes().get(0).getStartTime().toUiString() + " to "
								+ calendarEvent.getTimes().get(0).getEndTime().toUiString() + "\n") : ""));
	}

	/**
	 * This method is used to initialize google calendar service
	 *
	 * @param doctorRecord
	 *            - doctor record, which contains proper credentials information
	 * @return calendar service object
	 * @throws Exception
	 *             when there is invalid credentials or any network error
	 */
	public static synchronized CalendarService getGoogleService(DoctorRecord doctorRecord) throws Exception {
		CalendarService myService = new CalendarService("exampleCo-exampleApp-1");
		String userName = doctorRecord.googleUsername;
		// this is a patch for userid without domain name
		if (userName.indexOf('@') == -1) {
			userName = userName + "@gmail.com";
		}
		String userPassword = CryptoHelper.decrypt(doctorRecord.googlePassword);

		try {
			myService.setUserCredentials(userName, userPassword);
		} catch (Exception e) {
			logger.error("Exception when getGoogleService()... ");
			logger.error(NotifyHelper.getStackTrace(e));
			throw e;
		}
		return myService;
	}



	/**
	 * This method construct google event query according to Google API to
	 * search an appt that was cancelled from scheduler
	 */
	public static Query costructQuery(CallRecord callRecord, PatientRecord patientRecord, String timeZone) {
		StringBuilder sb = new StringBuilder();
		sb.append(patientRecord.firstName);
		sb.append(" ");
		sb.append(patientRecord.middleName);
		sb.append(" ");
		sb.append(patientRecord.lastName);
		sb.append(", ");
		sb.append(patientRecord.phone);
		String fullTextQueryString = sb.toString();
		GregorianCalendar calendar = TimeHelper.daysToCalendar(Integer.valueOf(callRecord.apptEpochDay));
		calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
		calendar.add(Calendar.SECOND, Integer.valueOf(callRecord.apptStart));
		DateTime eventStartDateTime = new DateTime(calendar.getTime(), TimeZone.getTimeZone(timeZone));
		calendar.add(Calendar.SECOND, 1);
		DateTime eventEndDateTime = new DateTime(calendar.getTime(), TimeZone.getTimeZone(timeZone));

		Query query = new Query(eventFeedUrl);
		query.setFullTextQuery(fullTextQueryString);
		query.setStringCustomParameter("ctz", timeZone);
		query.addCustomParameter(new CustomParameter("start-min", eventStartDateTime.toString()));
		query.addCustomParameter(new CustomParameter("start-max", eventEndDateTime.toString()));

		CalendarQuery query2 = new CalendarQuery(eventFeedUrl);
		query2.setMinimumStartTime(eventStartDateTime);
		query2.setMaximumStartTime(eventEndDateTime);
		return query2;

		// log info about this query
		/**
		 * logger.info("\n" + "Constructed query to search cancelled appt..." +
		 * "\n" + "Query fullTextQueryString: " + fullTextQueryString + "\n" +
		 * "Time Zone: " + timeZone + "\n");
		 */
	}


	/**
	 * Added by Mathias. Adds the angelspeech
	 * {@link ScheduleApptRecord#scheduleApptId} to a Google
	 * {@link CalendarEventEntry} as an {@link ExtendedProperty}.
	 *
	 */
	public static void setAngelSpeechId(CalendarEventEntry event, String apptId) throws IOException, ServiceException {
		for (ExtendedProperty e : event.getExtendedProperty()) {
			if (e.getName().equals(ANGELSPEECH_APPOINTMENT_ID_EP_TAG)) {
				e.setValue(apptId);
				return;
			}
		}
		ExtendedProperty ep = new ExtendedProperty();
		ep.setName(ANGELSPEECH_APPOINTMENT_ID_EP_TAG);
		ep.setValue(apptId);
		event.addExtendedProperty(ep);
	}

	/**
	 * Added by Mathias. Gets the {@link TimeZone} of the given user's calendar.
	 *
	 * @param service
	 * @param userName
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ServiceException
	 */
	public static TimeZone getCalendarTimezone(CalendarService service, String userName) throws MalformedURLException,
			IOException, ServiceException {
		if (userName.indexOf('@') == -1) {
			userName = userName + "@gmail.com";
		}
		CalendarFeed calendarFeed = service.getFeed(
				new URL(METAFEED_URL_BASE + userName + OWNCALENDARS_FEED_URL_SUFFIX), CalendarFeed.class);
		CalendarEntry calendar = calendarFeed.getEntries().get(0);
		String tz = calendar.getTimeZone().getValue();
		return TimeZone.getTimeZone(tz);
	}

	/**
	 * Sets the google calendar timezone to the timezone of the doctor record
	 * (if they are different) and returns the (new) {@link TimeZone} of the
	 * Calendar.
	 *
	 * @param service
	 * @param dr
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ServiceException
	 */
	public static TimeZone setCalendarTimezone(CalendarService service, DoctorRecord dr) throws MalformedURLException,
			IOException, ServiceException {
		String userName = dr.googleUsername;
		if (userName.indexOf('@') == -1) {
			userName = userName + "@gmail.com";
		}
		CalendarFeed calendarFeed = service.getFeed(
				new URL(METAFEED_URL_BASE + userName + OWNCALENDARS_FEED_URL_SUFFIX), CalendarFeed.class);
		CalendarEntry calendar = calendarFeed.getEntries().get(0);
		String timeZone = dr.timeZone;
		if ((calendar.getTimeZone() != null && timeZone != null && !calendar.getTimeZone().getValue().equals(timeZone))
				|| (calendar.getTimeZone() == null && timeZone != null)) {
			logger.info("Google calendar's time zone differs from user profile setting. "
					+ "The current Google calendar setting is " + calendar.getTimeZone().getValue()
					+ ". User profile time zone:" + timeZone);
			calendar.setTimeZone(new TimeZoneProperty(timeZone));
			calendar = calendar.update();
			logger.info("Google calendar's time zone is updated. " + "The current Google calendar setting is "
					+ calendar.getTimeZone().getValue() + ". User profile time zone:" + timeZone);
		}
		return TimeZone.getTimeZone(calendar.getTimeZone().getValue());
	}


	/**
	 * Gets a {@link Calendar} for the start time of the given
	 * {@link CalendarEventEntry}
	 *
	 * @param event
	 * @param calendarTimezone
	 * @return
	 */
	public static Calendar getEventStartTime(CalendarEventEntry event, TimeZone calendarTimezone, When eventTime) {
		if(eventTime == null){
			eventTime = event.getTimes().get(0);
		}
		Calendar eventStartTime = new GregorianCalendar(calendarTimezone);
		eventStartTime.setTime(new Date(eventTime.getStartTime().getValue()));
		return eventStartTime;
	}

	public static Calendar getEventStartTime(CalendarEventEntry event, TimeZone calendarTimezone) {
		return getEventStartTime(event, calendarTimezone, null);
	}

	public static Calendar getEventStartTime(When when, TimeZone calendarTimezone) {
		return getEventStartTime(null, calendarTimezone, when);
	}

	/**
	 * Gets a {@link Calendar} for the end time of the given
	 * {@link CalendarEventEntry}
	 *
	 * @param event
	 * @param calendarTimezone
	 * @return
	 */
	public static Calendar getEventEndTime(CalendarEventEntry event, TimeZone calendarTimezone, When eventTime) {
		if(eventTime == null) {
			eventTime = event.getTimes().get(0);
		}
		Calendar eventEndTime = new GregorianCalendar(calendarTimezone);
		eventEndTime.setTime(new Date(eventTime.getEndTime().getValue()));
		return eventEndTime;
	}

	public static Calendar getEventEndTime(CalendarEventEntry event, TimeZone calendarTimezone) {
		return getEventEndTime(event, calendarTimezone, null);
	}

	/**
	 * Added by Mathias. Checks if the given Google {@link CalendarEventEntry}
	 * is already present in the AngelSpeech system. This is done by checking
	 * the {@link #ANGELSPEECH_APPOINTMENT_ID_EP_TAG} {@link ExtendedProperty}
	 * on the {@link CalendarEventEntry}.
	 *
	 * @see #setAngelSpeechId(CalendarEventEntry, String)
	 */
	public static boolean eventExistsInAngelSpeech(CalendarEventEntry event) {
		List<ExtendedProperty> propList = event.getExtendedProperty();
		for (ExtendedProperty prop : propList) {
			if (ANGELSPEECH_APPOINTMENT_ID_EP_TAG.equals(prop.getName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isRecurrent(CalendarEventEntry event) {
		return event.getRecurrence() != null;
	}

	public static boolean isDeleted(CalendarEventEntry event) {
		return event.getStatus().getValue().equals(EventStatus.CANCELED_VALUE);
	}


}