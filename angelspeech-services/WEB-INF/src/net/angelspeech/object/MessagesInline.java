package net.angelspeech.object;

//import net.angelspeech.object.CacheItems;

import java.util.LinkedList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.ActionMessages;

/**
 * This class contains code methods for handling of server messages that are sent to the client
 * browser "inline" (in the HTML code of the served page).
 */
public class MessagesInline
{
	static final private String attributeName = "messagesInline";

	private HttpServletRequest request;
	private MessagesStore messagesStore;
	private MessagesDisplay messagesDisplay;

	/**
	 * Create a new messages cache object and link it to the specified
	 * HTTP request.
	 *
	 * @param request	The HTTP request we are processing.
	 */
	public MessagesInline (HttpServletRequest request)
	{
		this.request = request;
		messagesStore = new MessagesStore ();
		messagesDisplay = new MessagesDisplay ();
	}

	/**
	 * Add the specified action messages to the list of displayed alert messages
	 *
	 * @param actionMessages	The action messages which are added to the
	 *				list of displayed alert messages.
	 */
	public void addActionMessages (ActionMessages actionMessages)
	{
		messagesDisplay.addActionMessages (actionMessages);
	}

	/**
	 * Add the specified generic message to the list of displayed alert messages
	 *
	 * @param property	The name of the property which contains the message.
	 */
	public void addGenericMessage (String property)
	{
		messagesDisplay.addGenericMessage (property);
	}

	/**
	 * Add the specified dynamic (runtime-specified) message to the list of the
	 * displayed alert messages.
	 *
	 * @param text		The text of the added message
	 */
	public void addDynamicMessage (String text)
	{
		messagesDisplay.addDynamicMessage (text);
	}

	/**
	 * Check if the list of displayed messages is empty
	 *
	 * @return	Return true iff the list of displayed messages is empty
	 */
	public boolean isEmptyDisplay ()
	{
		return (messagesDisplay.isEmpty ());
	}

	/**
	 * Inform client about superuser/doctor ID change.
	 *
	 * @param superuserId	The superuser ID. Can be null if there is no active superuser ID.
	 * @param doctorId	The doctor ID. Can be null if there is no active doctor ID.
	 */
	public void setActiveId (String superuserId, String doctorId)
	{
		messagesStore.setActiveId (superuserId, doctorId);
	}

	/**
	 * Instruct the browser to reset the monthly calendar.
	 */
	public void resetCalendar ()
	{
		messagesStore.resetCalendar ();
	}

	/**
	 * Inform client browser that periodic update must start.
	 */
	public void updateStart ()
	{
		messagesStore.updateStart ();
	}

	/**
	 * Inform client browser that periodic update must stop.
	 */
	public void updateStop ()
	{
		messagesStore.updateStop ();
	}

	/**
	 * Update the client cache of the superuser profile info
	 *
	 * @param superuserId	The superuser ID.
	 */
	public void setSuperuser (String superuserId) throws Exception
	{
		messagesStore.setSuperuser (superuserId);
	}

	/**
	 * Update the client cache of the doctor profile info
	 *
	 * @param doctorId	The doctor ID.
	 */
	public void setDoctor (String doctorId) throws Exception
	{
		messagesStore.setDoctor (doctorId);
	}

	/**
	 * Update the client cache of all the location records.
	 *
	 * @param doctorId	The doctor ID.
	 */
	public void setLocationAll (String doctorId) throws Exception
	{
		messagesStore.setLocationAll (doctorId);
	}

	/**
	 * Update the client cache for the specified location ID.
	 *
	 * @param doctorId	The doctor ID.
	 * @param locationId	The location ID.
	 */
	public void setLocationOne (String doctorId, String locationId) throws Exception
	{
		messagesStore.setLocationOne (doctorId, locationId);
	}

	/**
	 * Remove the specified location from the client cache.
	 *
	 * @param doctorId	The doctor ID.
	 * @param locationId	The location ID.
	 */
	public void deleteLocation (String doctorId, String locationId)
	{
		messagesStore.deleteLocation (doctorId, locationId);
	}

	/**
	 * Update the client cache of all the appointment profile records.
	 *
	 * @param doctorId	The doctor ID.
	 */
	public void setAppointmentProfileAll (String doctorId) throws Exception
	{
		messagesStore.setAppointmentProfileAll (doctorId);
	}

	/**
	 * Update the client cache for the specified appointment profile ID.
	 *
	 * @param doctorId		The doctor ID.
	 * @param apptProfileId	The appointment profile ID.
	 */
	public void setAppointmentProfileOne (String doctorId, String apptProfileId) throws Exception
	{
		messagesStore.setAppointmentProfileOne (doctorId, apptProfileId);
	}

	/**
	 * Remove the specified appointment profile from the client cache.
	 *
	 * @param doctorId		The doctor ID.
	 * @param apptProfileId	The appointment profile ID.
	 */
	public void deleteAppointmentProfile (String doctorId, String apptProfileId)
	{
		messagesStore.deleteAppointmentProfile (doctorId, apptProfileId);
	}

	/**
	 * Update the client cache of the appointment schedule for
	 * the specified timerange and patients.
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
		messagesStore.setSchedule (doctorId, epochDay, patients);
	}

	/**
	 * Update the client cache of all the patient records.
	 *
	 * @param doctorId	The doctor ID.
	 */
	public void setPatientAll (String doctorId) throws Exception
	{
		messagesStore.setPatientAll (doctorId);
	}

	/**
	 * Update the client cache of the specified patient.
	 *
	 * @param patientId	The patient ID.
	 */
	public void setPatientOne (String doctorId, String patientId) throws Exception
	{
		messagesStore.setPatientOne (doctorId, patientId);
	}

	/**
	 * Remove the specified patient from the client cache.
	 *
	 * @param patientId	The patient ID.
	 */
	public void deletePatient (String doctorId, String patientId)
	{
		messagesStore.deletePatient (doctorId, patientId);
	}

	/**
	 * Delete all doctor settings/patients/schedules from the client cache.
	 */
	public void deleteAll ()
	{
		messagesStore.deleteAll ();
	}

	/**
	 * Save all update requests into a session variable so that the
	 * next call to getJavaScript() will return all the request info.
	 * If there is already saved update information the currently saved
	 * update information is added at the end of the old one.
	 */
	public void save () throws Exception
	{
		HttpSession session;
		LinkedList list;
		LinkedList display;

		session = request.getSession (true);
		if ((list = (LinkedList) session.getAttribute (attributeName)) == null) {
			list = new LinkedList ();
		}
		display = messagesDisplay.fetch ();
		if (display.isEmpty () == false) {
			messagesStore.addDisplay ((String []) display.toArray (new String [0]));
		}
		list.addAll (messagesStore.fetch ());
		session.setAttribute (attributeName, list);
	}

	/**
	 * Return a single string representing the update information in
	 * javascript format. Usually this method is called from a .jsp page.
	 *
	 * @return		Return the update information as a javascript array
	 * @param request	The HTTP request we are processing.
	 */
	static public String getJavaScript (HttpServletRequest request) throws Exception
	{
		HttpSession session;
		LinkedList list;

		session = request.getSession (true);
		if ((list = (LinkedList) session.getAttribute (attributeName)) == null) {
			return (null);
		}
		session.removeAttribute (attributeName);
		return (BuildJavaScript.build (list.toArray (new Object [0])));
	}
}
