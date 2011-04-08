package net.angelspeech.database;

import net.angelspeech.database.GenericRecord;

/**
 * This class is an object mapping of the "apptProfile" SQL table *  ampmOnly definition:
 *	0 = no restriction
 *	1 = AM only
 *	2 = PM only
 *	other = invalid.
 */
public class ApptProfileRecord extends GenericRecord
{
	public String apptProfileId;
	public String doctorId;
	public String isDefault;
	public String name;
	public String description;
	public String duration;
	public String selectByPatient;
	public String allowMonday;
	public String allowTuesday;
	public String allowWednesday;
	public String allowThursday;
	public String allowFriday;
	public String allowSaturday;
	public String allowSunday;
	public String startAtSlot;
	public String ampmOnly;
	public String hasQuestionForm;
	public String hasCallBack;
	public String callBackInDays;
	public String apptLocationId;
	public String notes;
	public String callBackNotes;
	public String prepaidFee;
	public String feeCurrency;

	public ApptProfileRecord () throws Exception
	{
		super ("apptProfile", "apptProfileId", ApptProfileRecord.class);
	}
}
