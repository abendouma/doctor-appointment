package net.angelspeech.object;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 *  This class is used to send pushlet/polling data to client's web browser
 */
public class PeriodicUpdate
{
	static private Logger logger = Logger.getLogger (PeriodicUpdate.class);

	static public boolean writeString (OutputStream outputStream, String text)
	{
		byte [] data;

		data = text.getBytes ();
		try {
			outputStream.write (data);
			outputStream.flush ();
		} catch (IOException ex) {
			logger.debug ("Got an IOException - probably client has closed connection");
			return (false);
		}
		return (true);
	}
	
	/**
	 * This method is used by Polling design. It returns the account data
	 * to a servelet that controls data polling by client
	 */
	static public String getJSObject (HttpServletRequest request, String superuserId, String doctorId) throws Exception {
		String id;
		String [] accounts;
		// doctorId and superuserId are sent from client and can be string "null"
		//logger.info("Polling request received from sessionid..."+ request.getSession().getId());
		//logger.info("superuserId..."+superuserId + " and doctorId..."+doctorId);
		
		if (superuserId.equalsIgnoreCase("null")== false){
			if (SessionDoctor.checkAccessSuperuser (request, superuserId) == false) {
				logger.info ("!!! Polling FAILED checkAccessSuperuser() for superuserId.." + superuserId);
				return null;
			}
		}
		if (doctorId.equalsIgnoreCase("null")== false){
			if (SessionDoctor.checkAccess (request, doctorId) == false) {
				logger.info ("!!! Polling FAILED checkAccess() for doctorId.." + doctorId);
				return null;
			}
		}		
		if ((id = SessionDoctor.getActiveSuperuser (request)) != null) {
			accounts = SuperuserHelper.getLinkedDoctors (id);
			//logger.info ("Polling find Superuser id..." + id);
		} else if ((id = SessionDoctor.getActiveDoctor (request)) != null) {
			accounts = new String [] {id};
			//logger.info ("Polling did not find superuser but find Doctor id..." + id);
		} else {
			logger.info("NULL object received for polling!");
			logger.info ("!!! Polling DID NOT FIND ANY ID in SessionDoctor");
			return null;
		}

		MessagesStore messages;
		int i;
		String jsObject;

		if (accounts.length == 0) {
			logger.info ("Empty account update list, not sending Polling update");
			return null;
		}
		messages = new MessagesStore ();
		for (i = 0; i < accounts.length; ++i) {
			messages.setDoctor (accounts [i]);
			messages.setLocationAll (accounts [i]);
			messages.setAppointmentProfileAll (accounts [i]);
			messages.setSchedule (accounts [i], -1, null);
			messages.setPatientAll (accounts [i]);
		}
		jsObject = BuildJavaScript.build (messages.fetch ().toArray (new Object [0]));
		return jsObject;
	}

	static public boolean sendLoop (HttpServletRequest request, OutputStream outputStream) throws Exception
	{
		String id;
		String [] accounts;

		if ((id = SessionDoctor.getActiveSuperuser (request)) != null) {
			accounts = SuperuserHelper.getLinkedDoctors (id);
		} else if ((id = SessionDoctor.getActiveDoctor (request)) != null) {
			accounts = new String [] {id};
		} else {
			return (false);
		}
		/*
			NB:
			1. The default "out" object which is of type JspWriter does not
			cause IOException when out.print() is called and user has already
			closed its side of the connection. That is why we use an
			OutputStream object which correctly causes IOException for writes
			to an already closed connection. According to
			http://www.pushlets.com/doc/whitepaper-s4.html this problem can
			be avoided if the pushlet code is moved to a servlet instead of
			a JSP, however according to JDK 1.4 docs PrintWriter never
			invokes IOException, so even a servlet would not help.

			2. Using the current method we will get he following exception:

				java.lang.IllegalStateException: getOutputStream() has
				already been called for this response

			because the jsp servlet tries to call HttpServletResponse.getWriter().
			This exception is expected and cannot be avoided without moving
			the code to a servlet.
		*/
		return (sendUpdates (accounts, outputStream));
	}

	static private boolean sendUpdates (String [] ids, OutputStream outputStream) throws Exception
	{
		int updateInterval;
		MessagesStore messages;
		int i;
		String jsObject;

		if (ids.length == 0) {
			logger.debug ("Empty update list, not sending an update");
			return (false);
		}
		if ((updateInterval = getUpdateInterval ()) <= 0) {
			logger.debug ("Non-positive update interval. Not sending an update");
			return (false);
		}
		messages = new MessagesStore ();
		for (;;) {
			logger.debug ("Sending a periodic update");
			for (i = 0; i < ids.length; ++i) {
				messages.setDoctor (ids [i]);
				messages.setLocationAll (ids [i]);
				messages.setAppointmentProfileAll (ids [i]);
				messages.setSchedule (ids [i], -1, null);
				messages.setPatientAll (ids [i]);
			}
			jsObject = BuildJavaScript.build (messages.fetch ().toArray (new Object [0]));
			if (writeString (
				outputStream,
				"<script language=\"JavaScript\">" +
					"msg_handler.broadcast (MSG_INT_MESSAGE_PUSHLET, " + jsObject + ");" +
				"</script>"
			) == false) {
				break;
			}
			Thread.sleep (updateInterval * 1000);
		}
		return (true);
	}

	static public int getUpdateInterval () throws Exception
	{
		int interval, result;

		interval = SettingsHelper.readInt ("settings.updateInterval", SettingsHelper.PerformanceSettings);
		if ((result = interval * 60) < 0) {
			logger.debug ("Got a negative update interval, assuming 0");
			return (0);
		}
		logger.debug ("Got an update interval of " + String.valueOf (result) + " seconds");
		return (result);
	}


}
