package net.angelspeech.object;

import java.util.List;

import net.angelspeech.database.PatientRecord;
import net.angelspeech.database.SqlQuery;

import org.apache.log4j.Logger;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.ExtendedProperty;

/**
 * Contains some helper methods for GSync2 that are called from different places
 * in the Gsync2 code.
 * 
 * @author Mathias
 * 
 */
public class GSync2Helper {

	private static final Logger logger = Logger.getLogger(GSync2Helper.class);

	/**
	 * Gets the timestamp at which the gsync2 application was last run
	 * (excluding current run).
	 * 
	 * @return
	 * @throws Exception
	 */
	public static long getLastRunTimestamp() throws Exception {
		String[][] result = SqlQuery.query("select timestamp from gsyncParam");
		if (result == null || result.length == 0) {
			createTimestampRecord();
			return 0;
		}
		return Long.parseLong(result[0][0]);
	}

	private static void createTimestampRecord() throws Exception {
		SqlQuery.query("insert into gsyncParam(timestamp) values(0)");
	}

	public static void setLastRunTimestamp(long timestamp) throws Exception {
		SqlQuery.query("update gsyncParam set timestamp = " + timestamp);
	}

	public static String getAngelSpeechId(CalendarEventEntry event) {
		return getExtendedPropertyValue(event, GCalendarHelper.ANGELSPEECH_APPOINTMENT_ID_EP_TAG);
	}

	public static String getExtendedPropertyValue(CalendarEventEntry event, String propertyName) {
		ExtendedProperty prop = getExtendedProperty(event, propertyName);
		if (prop == null) {
			return null;
		} else {
			return prop.getValue();
		}
	}

	public static ExtendedProperty getExtendedProperty(CalendarEventEntry event, String propertyName) {
		List<ExtendedProperty> propList = event.getExtendedProperty();
		for (ExtendedProperty prop : propList) {
			if (propertyName.equals(prop.getName())) {
				return prop;
			}
		}
		return null;
	}

	/**
	 * Gets the google patient for the given <code>doctorId</code>. In the event
	 * no such patient exists, a new {@link PatientRecord} will be created.
	 * 
	 * @param doctorId
	 * @return The Google {@link PatientRecord}. This method will never return
	 *         <code>null</code>.
	 * @throws Exception
	 */
	public static PatientRecord getGooglePatient(String doctorId, boolean create) throws Exception {
		logger.debug("Method getGooglePatient starting in class GDataGeneric");
		PatientRecord pr = new PatientRecord();
		if (!pr.readByFilter(new String[][] { { "firstName", "GOOGLE" }, { "lastName", "EVENT" },
				{ "doctorId", doctorId } })) {
			if (create) {
				logger.debug("Google EVENT static record doesn't exist in db, creating record");
				pr.firstName = "GOOGLE";
				pr.lastName = "EVENT";
				pr.doctorId = doctorId;
				pr.isActive = "1";
				pr.isRestricted = "0";
				pr.language = "EN";
				pr.reminderType = "0"; // Gsync2 appts will not have any
				// reminder
				pr.createdDay = "" + TimeHelper.currentEpochDay();
				String id = PatientHelper.multiCreate(pr);
				pr.patientId = id;
			} else {
				return null;
			}
		}
		logger.debug("Method getGooglePatient ended, returning patient " + pr.patientId);
		return pr;
	}

}
