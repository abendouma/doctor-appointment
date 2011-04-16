package net.angelspeech.service.dto.sync;

/**
 * @author Quang
 *
 */
public class DoctorSyncInfo {
	public Integer doctorId;
	public String firstName;
	public String lastName;
	public String businessName;
	public String street;
	public String city;
	public String state;
	public String zip;
	public String bizPhone;
	public Integer workMonday;
	public Integer workTuesday;
	public Integer workWednesday;
	public Integer workThursday;
	public Integer workFriday;
	public Integer workSaturday;
	public Integer workSunday;
	public Integer slotSize;
	public Integer workdayStart;
	public Integer workdayEnd;
	public Integer lunchStart;
	public Integer lunchEnd;
	public Integer hasCustomizeAppt;
	public String calledAs;
	
	public DoctorSyncInfo(Integer doctorId, String firstName, String lastName,
			String businessName, String street, String city, String state,
			String zip, String bizPhone, Integer workMonday,
			Integer workTuesday, Integer workWednesday, Integer workThursday,
			Integer workFriday, Integer workSaturday, Integer workSunday,
			Integer slotSize, Integer workdayStart, Integer workdayEnd,
			Integer lunchStart, Integer lunchEnd, Integer hasCustomizeAppt,
			String calledAs) {
		this.doctorId = doctorId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.businessName = businessName;
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.bizPhone = bizPhone;
		this.workMonday = workMonday;
		this.workTuesday = workTuesday;
		this.workWednesday = workWednesday;
		this.workThursday = workThursday;
		this.workFriday = workFriday;
		this.workSaturday = workSaturday;
		this.workSunday = workSunday;
		this.slotSize = slotSize;
		this.workdayStart = workdayStart;
		this.workdayEnd = workdayEnd;
		this.lunchStart = lunchStart;
		this.lunchEnd = lunchEnd;
		this.hasCustomizeAppt = hasCustomizeAppt;
		this.calledAs = calledAs;
	}
	
	public DoctorSyncInfo() {
	}
}
