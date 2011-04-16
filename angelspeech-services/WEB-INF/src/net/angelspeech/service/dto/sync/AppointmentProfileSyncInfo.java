package net.angelspeech.service.dto.sync;

/**
 * @author Quang
 *
 */
public class AppointmentProfileSyncInfo {
	public int apptProfileId;
	public int isDefault;
	public String name;
	public int duration;
	public int selectByPatient;
	public int allowMonday;
	public int allowTuesday;
	public int allowWednesday;
	public int allowThursday;
	public int allowFriday;
	public int allowSaturday;
	public int allowSunday;
	public int apptLocationId;
	public int ampmOnly;
	
	public AppointmentProfileSyncInfo(int apptProfileId, int isDefault,
			String name, int duration, int selectByPatient, int allowMonday,
			int allowTuesday, int allowWednesday, int allowThursday,
			int allowFriday, int allowSaturday, int allowSunday,
			int apptLocationId, int ampmOnly) {
		this.apptProfileId = apptProfileId;
		this.isDefault = isDefault;
		this.name = name;
		this.duration = duration;
		this.selectByPatient = selectByPatient;
		this.allowMonday = allowMonday;
		this.allowTuesday = allowTuesday;
		this.allowWednesday = allowWednesday;
		this.allowThursday = allowThursday;
		this.allowFriday = allowFriday;
		this.allowSaturday = allowSaturday;
		this.allowSunday = allowSunday;
		this.apptLocationId = apptLocationId;
		this.ampmOnly = ampmOnly;
	}
	
	public AppointmentProfileSyncInfo() {
	}
}
