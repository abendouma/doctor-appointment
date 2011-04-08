package net.angelspeech.shell.gsync2;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.angelspeech.database.CallRecord;
import net.angelspeech.object.ReminderInfo;
import net.angelspeech.object.SettingsHelper;
import net.angelspeech.object.TimeHelper;

/**
 * This class is used for storing all constants and necessary data. Used in
 * Google Calendar Sync
 */
public class GoogleSyncDataHolder2 {
	private final int epochDayStart;
	private final int maxSyncApptDay;
	private final int maxApptsPerGoogleRequest;
	private final int epochDayEnd;
	private int doctorIdStart;
	private int doctorIdEnd;
	private final int numThreads;

	// public final static String SENT_KEY = "sent";

	public final static String CANCEL_KEY = "cancelled";

	public static final String RECEIVED_KEY = "received";

	public static final String RESCHEDULED_KEY = "rescheduled";

	public static final String RECURRENT_CANCELED_KEY = "recurrentCanceled";

	public static final String RECURRENT_RECEIVED_KEY = "recurrentResceived";
	
	public static final String PROCESSED_DOCTORS_KEY = "processedDoctors";
	
	public static final String RECURRENT_RESCHEDULED_KEY = "recurrentRescheduled";

	/**
	 * This field stores a queues of added appts
	 */
	private ConcurrentLinkedQueue<Queue<ReminderInfo>> receivedAppts = new ConcurrentLinkedQueue<Queue<ReminderInfo>>();

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
	 * @param doctorSyncErrorStatus
	 *            the doctorSyncErrorStatus to set
	 */
	public void setDoctorSyncErrorStatus(ConcurrentHashMap<String, Boolean> doctorSyncStatus) {
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
	public ConcurrentLinkedQueue<Queue<ReminderInfo>> getReceivedAppts() {
		return receivedAppts;
	}

	/**
	 * @param receivedAppts
	 *            the sentAppts to set
	 */
	public void setReceivedAppts(ConcurrentLinkedQueue<Queue<ReminderInfo>> receivedAppts) {
		this.receivedAppts = receivedAppts;
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
	public void setCancelledAppts(ConcurrentLinkedQueue<Queue<CallRecord>> cancelledAppts) {
		this.cancelledAppts = cancelledAppts;
	}


	/**
	 * Constructor
	 *
	 * @throws Exception
	 *             if some resources are missing
	 */
	public GoogleSyncDataHolder2() throws Exception {
		epochDayStart = TimeHelper.currentEpochDay();
		maxSyncApptDay = SettingsHelper.readInt("maxSyncApptDay", SettingsHelper.PerformanceSettings);
		epochDayEnd = maxSyncApptDay + epochDayStart;
		maxApptsPerGoogleRequest = SettingsHelper.readInt("maxApptsPerGoogleRequest",
				SettingsHelper.PerformanceSettings);
		doctorIdStart = SettingsHelper.readInt("reminderRangeStart", SettingsHelper.PerformanceSettings);
		doctorIdEnd = SettingsHelper.readInt("reminderRangeEnd", SettingsHelper.PerformanceSettings);
		numThreads = SettingsHelper.readInt("google_sync_threads", SettingsHelper.PerformanceSettings);
		totalStats.put(RECEIVED_KEY, 0);
		totalStats.put(CANCEL_KEY, 0);
		totalStats.put(RESCHEDULED_KEY, 0);
		totalStats.put(RECURRENT_CANCELED_KEY, 0);
		totalStats.put(RECURRENT_RECEIVED_KEY, 0);
		totalStats.put(RECURRENT_RESCHEDULED_KEY, 0);
		totalStats.put(PROCESSED_DOCTORS_KEY, 0);
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
