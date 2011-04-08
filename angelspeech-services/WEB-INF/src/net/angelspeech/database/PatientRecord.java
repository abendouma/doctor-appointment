package net.angelspeech.database;

import net.angelspeech.database.GenericRecord;

/**
 * This class is an object mapping of the "patients" SQL table
 *
 * reminderType definition: 0 = no appt reminder
 *							1 = appt reminder via telephone only
 *							2 = appt reminder via email only
 *                          3 = appt reminder via both email and phone
 *						other = invalid. *
 * isRestricted definition	0 = not restricted
 *							1 = restricted from making appts
 *							2 = pending new patient	 *  *
 */
public class PatientRecord extends GenericRecord
{
	public String patientId;
	public String doctorId;
	public String isActive;
	public String isRestricted;
	public String firstName;
	public String middleName;
	public String lastName;
	public String ssn;
	public String street;
	public String city;
	public String state;
	public String zip;
	public String phone;
	public String phone2;
	public String email;
	public String reminderType;
	public String smsCarrier;
	public String smsReminderTime;
	public String language;
	public String ivrIndex;
	public String latestActivityDay;
	public String createdDay;
	public String checkInNotes;
	public PatientRecord () throws Exception
	{
		super ("patients", "patientId", PatientRecord.class);
	}

	public boolean isGooglePatient() {
		return "GOOGLE EVENT".equalsIgnoreCase(firstName + " " + lastName);
	}
	
	public boolean isSMSActive() {
		return phone2 != null && smsCarrier != null && phone2.length() == 10 && smsCarrier.length() > 4;
	}
}
