package net.angelspeech.shell.gsync1;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.angelspeech.database.CallRecord;
import net.angelspeech.object.ReminderInfo;
import net.angelspeech.object.SettingsHelper;
import net.angelspeech.object.TimeHelper;

/**
 * This class is used for storing all constants and necessary data.
 * Used in Google Calendar Sync
 */
public class GoogleSyncDataHolder {
	private final int epochDayStart;
	private final int maxSyncApptDay;
	private final int maxApptsPerGoogleRequest;
	private final int epochDayEnd;
	private int doctorIdStart;
	private int doctorIdEnd;
	private final int numThreads;

	public final static String SENT_KEY = "sent";

	public final static String CANCEL_KEY = "cancelled";

	public final static String RECURRENT_CANCELED = "recurrent_canceled";
	/**
	 * This field stores a queues of added appts
	 */
	private ConcurrentLinkedQueue<Queue<ReminderInfo>> sentAppts = new ConcurrentLinkedQueue<Queue<ReminderInfo>>();

	/**
	 * This field stores a queues of cancelled appts
	 */
	private ConcurrentLinkedQueue<Queue<CallRecord>> cancelledAppts = new ConcurrentLinkedQueue<Queue<CallRecord>>();

	private ConcurrentHashMap<String, Boolean> doctorSyncErrorStatus = new ConcurrentHashMap<String, Boolean>();

	/**
	 * @return the doctorSyncErrorStatus
	 */
	public ConcurrentHashMap<String, Boolean> getDoctorSyncErrorStatus() {
		return doctorSyncErrorStatus;
	}

	/**
	 * @param doctorSyncErrorStatus the doctorSyncErrorStatus to set
	 */
	public void setDoctorSyncErrorStatus(
			ConcurrentHashMap<String, Boolean> doctorSyncStatus) {
		this.doctorSyncErrorStatus = doctorSyncStatus;
	}

	/**
	 * The map stores statistics information
	 */
	private HashMap<String, Integer> totalStats = new HashMap<String, Integer>();

	/**
	 * @return the totalStats
	 */
	public HashMap<String, Integer> getTotalStats() {
		return totalStats;
	}

	/**
	 * @return the sentAppts
	 */
	public ConcurrentLinkedQueue<Queue<ReminderInfo>> getSentAppts() {
		return sentAppts;
	}

	/**
	 * @param sentAppts
	 *            the sentAppts to set
	 */
	public void setSentAppts(
			ConcurrentLinkedQueue<Queue<ReminderInfo>> sentAppts) {
		this.sentAppts = sentAppts;
	}

	/**
	 * @return the cancelledAppts
	 */
	public ConcurrentLinkedQueue<Queue<CallRecord>> getCancelledAppts() {
		return cancelledAppts;
	}

	/**
	 * @param cancelledAppts
	 *            the cancelledAppts to set
	 */
	public void setCancelledAppts(
			ConcurrentLinkedQueue<Queue<CallRecord>> cancelledAppts) {
		this.cancelledAppts = cancelledAppts;
	}

	/**
	 * Constructor
	 * @throws Exception if some resources are missing
	 */
	public GoogleSyncDataHolder() throws Exception {
		epochDayStart = TimeHelper.currentEpochDay();
		maxSyncApptDay = SettingsHelper.readInt("maxSyncApptDay",
				SettingsHelper.PerformanceSettings);
		epochDayEnd = maxSyncApptDay + epochDayStart;
		maxApptsPerGoogleRequest = SettingsHelper.readInt(
				"maxApptsPerGoogleRequest", SettingsHelper.PerformanceSettings);
		doctorIdStart = SettingsHelper.readInt("reminderRangeStart",
				SettingsHelper.PerformanceSettings);
		doctorIdEnd = SettingsHelper.readInt("reminderRangeEnd",
				SettingsHelper.PerformanceSettings);
		numThreads = SettingsHelper.readInt("google_sync_threads",
				SettingsHelper.PerformanceSettings);
		totalStats.put(SENT_KEY, 0);
		totalStats.put(CANCEL_KEY, 0);
		totalStats.put(RECURRENT_CANCELED, 0);
	}

	/**
	 * @return the epochDayStart
	 */
	public int getEpochDayStart() {
		return epochDayStart;
	}

	/**
	 * @return the maxSyncApptDay
	 */
	public int getMaxSyncApptDay() {
		return maxSyncApptDay;
	}

	/**
	 * @return the maxApptsPerGoogleRequest
	 */
	public int getMaxApptsPerGoogleRequest() {
		return maxApptsPerGoogleRequest;
	}

	/**
	 * @return the epochDayEnd
	 */
	public int getEpochDayEnd() {
		return epochDayEnd;
	}

	/**
	 * @return the doctorIdStart
	 */
	public int getDoctorIdStart() {
		return doctorIdStart;
	}

	/**
	 * @return the doctorIdEnd
	 */
	public int getDoctorIdEnd() {
		return doctorIdEnd;
	}

	/**
	 * @return the numThreads
	 */
	public int getNumThreads() {
		return numThreads;
	}

	public void setDoctorIdStart(int doctorIdStart) {
		this.doctorIdStart = doctorIdStart;
	}

	public void setDoctorIdEnd(int doctorIdEnd) {
		this.doctorIdEnd = doctorIdEnd;
	}


}
