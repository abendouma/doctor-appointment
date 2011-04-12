package net.angelspeech.database;

import java.io.Serializable;

import net.angelspeech.database.GenericRecord;

/**
 * This class is an object mapping of the "doctors" SQL table
 *  accountType
 *  0 = regular scheduler user
 *  1 = prepaid user.
 *  
 *  reminderType definition:
 *	0 = no appt reminder
 *	1 = appt reminder via telephone only
 *	2 = appt reminder via email only
 *	3 = appt reminder via both email and phone
 *  4 = appt early reminder via telephone only
 *  5 = appt early reminder via email only
 *  6 = appt early reminder via both email and phone
 *	other = invalid.
 * 
 *  speechService:
 *	0 = no speech service
 *	1 = Flat Rate.
 *	2 = Pay-Per-Call
 *  3 = Shared Phone Flat-Rate
 *  4 = Shared Phone Pay-Per-Call
 *	other = invalid.
 *  hasWebAppt:
 *	boolean false = No patient web appt service.
 *	true = web patient Appt allowed.
 *
 *  newPatient:
 *  0 = should call office only
 *  1 = signup online or call office
 *  2 = signup online only
 *  3 = not accepted
 *  other = invalid value
 */
public class DoctorRecord extends GenericRecord
{
	public String doctorId;
	public String username;
	public String password;
	public String accountType;
	public String active;
	public String allowSuperuser;
	public String bcDOM;

	public String firstName;
	public String lastName;
	public String businessName;
	public String street;
	public String city;
	public String state;
	public String zip;
	public String mapURL;
	public String timeZone;
	public String bizPhone; // biz office phone
	public String emergencyPhone;
	public String transferBackPhone;
	public String voiceMail;
	public String email;

	public String workMonday;
	public String workTuesday;
	public String workWednesday;
	public String workThursday;
	public String workFriday;
	public String workSaturday;
	public String workSunday;
	public String workdayStart;
	public String workdayEnd;
	public String lunchStart;
	public String lunchEnd;
	public String slotSize;
	public String calHistory;
	public String calActive;
	public String selfServicePhone; //appt phone line
	public String reminderType;
	public String hasWebAppt;
	public String selfServiceApptMax;
	public String webAppt24HoursFreeze;
	public String customizeEmail;
	public String hasCustomizeForm;
	public String hasCustomizeAppt;
	public String hasCallBack;
	public String calledAs;
	public String speechService;
	public String cancelOnReminder;
	public String customPrompts;
	public String hasHL7Channels;
	public String hasGoogleSync;
	public String googleUsername;
	public String googlePassword;
	public String staffPassword;
	public String newPatient;
	public String noticeToNewPatient;
	public String insuranceList;
	public String paypalEmail;
	public String smsService;
	public String defaultTheme;
	public String defaultPDF;
	public String prepaidCallBalance;
	public String prepaidAlertlimit;
	public String prepaidLowBalanceAlert;	
	public String RESTkey;

	public DoctorRecord () throws Exception
	{
		super ("doctors", "doctorId", DoctorRecord.class);
	}
}
