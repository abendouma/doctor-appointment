package net.angelspeech.object;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.servlet.ServletContext;

import net.angelspeech.database.DoctorRecord;
import net.angelspeech.database.EmailTemplateRecord;
import net.angelspeech.database.PatientRecord;
import net.angelspeech.database.SuperuserRecord;
import net.angelspeech.database.ApptProfileRecord;
import net.angelspeech.database.MyNetworkData;
import net.angelspeech.database.SqlQuery;

import net.angelspeech.object.CallRecordLog;
import net.angelspeech.object.TimeHelper;
import net.angelspeech.object.PatientHelper;
import net.angelspeech.object.PrepaidReminderInfo;
import net.angelspeech.object.CallBackInfo;
import net.angelspeech.object.SettingsHelper;
import net.angelspeech.object.emailcustomization.Template;
import net.angelspeech.object.emailcustomization.TemplateExpander;
import net.angelspeech.object.ApptSearchHelper;
import net.angelspeech.object.PdfExportHelper;


import org.apache.log4j.Logger;

public class NotifyHelper
{
	static private Logger logger = Logger.getLogger (NotifyHelper.class);

	/**
	 * Notify a doctor about the creation of his account.
	 *
	 * @param doctorRecord	The doctor record for the added doctor.
	 * @param messages	The server messages object to which the success message is added.
	 */
	static public void doctorAdd (
		DoctorRecord doctorRecord,
		MessagesInline messages
	) throws Exception
	{
		String emailResult = EmailHelper.send (
			SettingsHelper.readString ("newDocAccount.from", SettingsHelper.EmailSettings),
			doctorRecord.email,
			TextExpansion.expandDoctor ("newDocAccount.subject", doctorRecord),
			TextExpansion.expandDoctor ("newDocAccount.body", doctorRecord),
			false
		);
		messages.addGenericMessage (emailResult);
	}

	/**
	 * Notify a doctor about the deletion of his account.
	 *
	 * @param doctorRecord	The doctor record for the deleted doctor.
	 * @param messages	The server messages object to which the success message is added.
	 */
	static public void doctorDelete (
		DoctorRecord doctorRecord,
		MessagesInline messages
	) throws Exception
	{
		String emailResult = EmailHelper.send (
			SettingsHelper.readString ("closeDocAccount.from", SettingsHelper.EmailSettings),
			doctorRecord.email,
			TextExpansion.expandDoctor ("closeDocAccount.subject", doctorRecord),
			TextExpansion.expandDoctor ("closeDocAccount.body", doctorRecord),
			false
		);
		messages.addGenericMessage (emailResult);
		// send a email copy to admin
		EmailHelper.send (
			SettingsHelper.readString ("closeDocAccount.from", SettingsHelper.EmailSettings),
			SettingsHelper.readString ("copy.admin.toAddress", SettingsHelper.EmailSettings),
			TextExpansion.expandDoctor ("closeDocAccount.subject", doctorRecord),
			TextExpansion.expandDoctor ("closeDocAccount.body", doctorRecord),
			false
		);
	}

	/**
	 * Notify a doctor about the modification of his account.
	 *
	 * @param doctorRecord	The doctor record for the modified doctor.
	 * @param messages	The server messages object to which the success message is added.
	 */
	static public void doctorEdit (
		DoctorRecord doctorRecord,
		MessagesInline messages
	) throws Exception
	{
		String emailResult = EmailHelper.send (
			SettingsHelper.readString ("adminModifiedDoctorAccount.from", SettingsHelper.EmailSettings),
			doctorRecord.email,
			TextExpansion.expandDoctor ("adminModifiedDoctorAccount.subject", doctorRecord),
			TextExpansion.expandDoctor ("adminModifiedDoctorAccount.body", doctorRecord),
			false
		);
		messages.addGenericMessage (emailResult);
	}
	/**
	 * Notify a doctor about the modification of his dataSync info.
	 *
	 * @param doctorRecord	The doctor record for the modified doctor.
	 * @param messages	The server messages object to which the success message is added.
	 */
	static public void doctorDataSyncEdit (
		DoctorRecord doctorRecord,
		MessagesInline messages
	) throws Exception
	{
		String emailResult = EmailHelper.send (
			SettingsHelper.readString ("dataSyncInfoModified.from", SettingsHelper.EmailSettings),
			doctorRecord.email,
			TextExpansion.expandDoctor ("dataSyncInfoModified.subject", doctorRecord),
			TextExpansion.expandDoctor ("dataSyncInfoModified.body", doctorRecord),
			false
		);
		messages.addGenericMessage (emailResult);
	}

	/**
	 * Notify a doctor (and copy admin) about the GdataSync Reset/Restart result.
	 *
	 * @param doctorRecord	The doctor record for the modified doctor.
	 * @param startRestartResult: message that report result of reset and restart in Gsync.
	 */
	static public void doctorGsyncStartRestartResult (
		DoctorRecord doctorRecord,
		String resetOrRestart,
		String startRestartResult
	) throws Exception
	{
		String emailResult = EmailHelper.send (
			SettingsHelper.readString ("doctorGsyncStartRestartResult.from", SettingsHelper.EmailSettings),
			doctorRecord.email,
			TextExpansion.expandDoctor ("doctorGsyncStartRestartResult.subject", doctorRecord)+ " "+resetOrRestart,
			TextExpansion.expandDoctor ("doctorGsyncStartRestartResult.body", doctorRecord)
			+ "\n"
			+ "\n"
			+ "\n"
			+ "---- Your Google Calendar Sync "+resetOrRestart+" Result ----\n"
			+ "\n"
			+ startRestartResult,
			false
		);
		// send a email copy to admin
		EmailHelper.send (
			SettingsHelper.readString ("doctorGsyncStartRestartResult.from", SettingsHelper.EmailSettings),
			SettingsHelper.readString ("copy.admin.toAddress", SettingsHelper.EmailSettings),
			TextExpansion.expandDoctor ("doctorGsyncStartRestartResult.subject", doctorRecord) + " "+resetOrRestart,
			TextExpansion.expandDoctor ("doctorGsyncStartRestartResult.body", doctorRecord)
			+ "\n"
			+ "\n"
			+ "\n"
			+ "---- Your Google Calendar Sync "+resetOrRestart+" Result ----\n"
			+ "\n"
			+ startRestartResult,
			false
		);
	}

	/**
	 * Notify a doctor about the modification of his account profile.
	 * This method overload the same name method below
	 * @param doctorRecord	The doctor record for the modified doctor.
	 * @param messages	The server messages object to which the success message is added.
	 */
	static public void doctorProfileEdit (
		DoctorRecord doctorRecordNew,
		DoctorRecord doctorRecordOld,
		MessagesInline messages
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;

		messageFrom = SettingsHelper.readString ("userProfile.from", SettingsHelper.EmailSettings);
		messageSubject = TextExpansion.expandDoctor ("userProfile.subject", doctorRecordNew);
		messageBody = TextExpansion.expandDoctor ("userProfile.body", doctorRecordNew);

		String emailResult = EmailHelper.send (messageFrom, doctorRecordOld.email, messageSubject, messageBody, false);
		if ((doctorRecordOld.email).equals (doctorRecordNew.email) == false) {
			EmailHelper.send (messageFrom, doctorRecordNew.email, messageSubject, messageBody, false);
		}
		// If paypal email changes, send extra notice to both addr.
		if ((doctorRecordOld.paypalEmail).equals (doctorRecordNew.paypalEmail) == false) {
			EmailHelper.send (messageFrom, doctorRecordOld.paypalEmail, messageSubject, messageBody, false);
			EmailHelper.send (messageFrom, doctorRecordNew.paypalEmail, messageSubject, messageBody, false);
		}
		messages.addGenericMessage (emailResult);
	}
	/**
	 * Notify a doctor about the modification of his account profile.
	 *
	 * @param doctorRecord	The doctor record for the modified doctor.
	 * @param messages	The server messages object to which the success message is added.
	 */
	static public void doctorProfileEdit (
		DoctorRecord doctorRecord,
		String emailOld,
		String emailNew,
		MessagesInline messages
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;

		messageFrom = SettingsHelper.readString ("userProfile.from", SettingsHelper.EmailSettings);
		messageSubject = TextExpansion.expandDoctor ("userProfile.subject", doctorRecord);
		messageBody = TextExpansion.expandDoctor ("userProfile.body", doctorRecord);

		String emailResult = EmailHelper.send (messageFrom, emailOld, messageSubject, messageBody, false);
		if (emailOld.equals (emailNew) == false) {
			EmailHelper.send (messageFrom, emailNew, messageSubject, messageBody, false);
		}
		messages.addGenericMessage (emailResult);
	}

	/**
	 * Notify a doctor about his account password.
	 *
	 * @param doctorRecord	The doctor record for the doctor.
	 * @param messages	The server messages object to which the success message is added.
	 */
	static public void doctorForgotPassword (
		DoctorRecord doctorRecord,
		MessagesInline messages
	) throws Exception
	{
		String emailFrom = SettingsHelper.readString ("settings.reminder.from", SettingsHelper.EmailSettings);
		String emailSubject = TextExpansion.expandDoctor ("settings.reminder.subject", doctorRecord);
		String emailBody = TextExpansion.expandDoctor ("settings.reminder.body", doctorRecord);

		String emailResult = EmailHelper.send (
			emailFrom,
			doctorRecord.email,
			emailSubject,
			emailBody,
			false
		);
		messages.addGenericMessage (emailResult);

		// send email copy to admin
		EmailHelper.send (
			emailFrom,
			SettingsHelper.readString ("copy.admin.toAddress", SettingsHelper.EmailSettings),
			emailSubject,
			emailBody,
			false
		);
	}

	/**
	 * Notify a doctor about invalid google login info.
	 *
	 * @param doctorRecord	The doctor record.
	 */
	static public void badApptProfile (
		DoctorRecord doctorRecord,
		ApptProfileRecord apptProfileRecord
	) throws Exception
	{
		//translate the slot
		String requiredStartSlot = ApptSearchHelper.getRequiredStartSlot(apptProfileRecord.startAtSlot);

		if (doctorRecord.calledAs.equalsIgnoreCase("none")){
				doctorRecord.calledAs = "";
				doctorRecord.firstName = "";
				doctorRecord.lastName =	doctorRecord.businessName;
		}

		String emailResult = EmailHelper.send (
						"myReminder@angelspeech.com",
						doctorRecord.email,
						"RE: Appointment control failed due to invalid profile setting!",
						"Hi "+ doctorRecord.calledAs + doctorRecord.lastName + ",\n\n" +
						"This is to notify you that your appointment profile " + "\n\n" +
						apptProfileRecord.name + "\n\n" +
						"has invalid setting for appointment starting time restriction. \n\n"+
						"--> "+ requiredStartSlot+ "\n\n" +
						"exceeds 60 min and can not be set as starting time. \n\n"+
						"You are advised to correct this setting in Account --> Appt profile page \n" +
						"as soon as possible to avoid more failures caused by this setting."+
						"\n\n\n" +
						"Have a good day! \n\n\n" +
						"AngelSpeech Support Staff \n" +
						"support@angelspeech.com",
						false
					);

					// send a email copy to admin
					EmailHelper.send (
						"myReminder@angelspeech.com",
						SettingsHelper.readString ("copy.admin.toAddress", SettingsHelper.EmailSettings),
						"RE: Appointment control failed due to invalid profile setting!",
						"Hi "+ doctorRecord.calledAs + doctorRecord.lastName + ",\n\n" +
						"This is to notify you that your appointment profile " + "\n\n" +
						apptProfileRecord.name + "\n\n" +
						"has invalid setting for appointment starting time restriction. \n\n"+
						"--> "+ requiredStartSlot + "\n\n" +
						"exceeds 60 min and can not be set as starting time. \n\n"+
						"You are advised to correct this setting in Account --> Appt profile page \n" +
						"as soon as possible to avoid more failures caused by this setting."+
						"\n\n\n" +
						"Have a good day! \n\n\n" +
						"AngelSpeech Support Staff \n" +
						"support@angelspeech.com",
						false
					);
	}


	/**
	 * Notify a doctor about invalid google login info.
	 *
	 * @param doctorRecord	The doctor record.
	 */
	static public void badGoogleLogin (
		DoctorRecord doctorRecord
	) throws Exception
	{
		String emailResult = EmailHelper.send (
			SettingsHelper.readString ("badGoogleLogin.from", SettingsHelper.EmailSettings),
			doctorRecord.email,
			TextExpansion.expandDoctor ("badGoogleLogin.subject", doctorRecord),
			TextExpansion.expandDoctor ("badGoogleLogin.body", doctorRecord),
			false
		);
		// send a email copy to admin
		EmailHelper.send (
			SettingsHelper.readString ("badGoogleLogin.from", SettingsHelper.EmailSettings),
			SettingsHelper.readString ("copy.admin.toAddress", SettingsHelper.EmailSettings),
			TextExpansion.expandDoctor ("badGoogleLogin.subject", doctorRecord),
			TextExpansion.expandDoctor ("badGoogleLogin.body", doctorRecord),
			false
		);
	}

	/*
	 * Notify a doctor about invalid google calendar info.
	 *
	 * @param doctorRecord	The doctor record.
	 */
	static public void googleCalendarNotExist (
		DoctorRecord doctorRecord
	) throws Exception
	{
		String emailResult = EmailHelper.send (
			SettingsHelper.readString ("googleCalendarNotExist.from", SettingsHelper.EmailSettings),
			doctorRecord.email,
			TextExpansion.expandDoctor ("googleCalendarNotExist.subject", doctorRecord),
			TextExpansion.expandDoctor ("googleCalendarNotExist.body", doctorRecord),
			false
		);
		// send a email copy to admin
		EmailHelper.send (
			SettingsHelper.readString ("googleCalendarNotExist.from", SettingsHelper.EmailSettings),
			SettingsHelper.readString ("copy.admin.toAddress", SettingsHelper.EmailSettings),
			TextExpansion.expandDoctor ("googleCalendarNotExist.subject", doctorRecord),
			TextExpansion.expandDoctor ("googleCalendarNotExist.body", doctorRecord),
			false
		);
	}

	/**
	 * Notify a superuser about the creation of his account.
	 *
	 * @param superuserRecord	The superuser record for the added superuser.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void superuserAdd (
		SuperuserRecord superuserRecord,
		MessagesInline messages
	) throws Exception
	{
		String emailFrom = SettingsHelper.readString ("newSuperAccount.from", SettingsHelper.EmailSettings);
		String emailSubject = TextExpansion.expandSuperuser ("newSuperAccount.subject", superuserRecord);
		String emailBody = TextExpansion.expandSuperuser ("newSuperAccount.body", superuserRecord);

		String emailResult = EmailHelper.send (
			emailFrom,
			superuserRecord.email,
			emailSubject,
			emailBody,
			false
		);

		// send a email copy to admin
		EmailHelper.send (
			emailFrom,
			SettingsHelper.readString ("copy.admin.toAddress", SettingsHelper.EmailSettings),
			emailSubject,
			emailBody,
			false
		);
		messages.addGenericMessage (emailResult);
	}

	/**
	 * Notify a superuser about the deletion of his account.
	 *
	 * @param superuserRecord	The superuser record for the deleted superuser.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void superuserDelete (
		SuperuserRecord superuserRecord,
		MessagesInline messages
	) throws Exception
	{
		String emailFrom = SettingsHelper.readString ("adminDeleteSuperuserAccount.from", SettingsHelper.EmailSettings);
		String emailSubject = TextExpansion.expandSuperuser ("adminDeleteSuperuserAccount.subject", superuserRecord);
		String emailBody = TextExpansion.expandSuperuser ("adminDeleteSuperuserAccount.body", superuserRecord);

		String emailResult = EmailHelper.send (
			emailFrom,
			superuserRecord.email,
			emailSubject,
			emailBody,
			false
		);
		messages.addGenericMessage (emailResult);

		// send email copy to admin
		EmailHelper.send (
			emailFrom,
			SettingsHelper.readString ("copy.admin.toAddress", SettingsHelper.EmailSettings),
			emailSubject,
			emailBody,
			false
		);
	}

	/**
	 * Notify a superuser about the modification of his account.
	 *
	 * @param superuserRecord	The superuser record for the modified superuser.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void superuserEdit (
		SuperuserRecord superuserRecord,
		MessagesInline messages
	) throws Exception
	{
		String emailFrom = SettingsHelper.readString ("adminModifiedSuperuserAccount.from", SettingsHelper.EmailSettings);
		String emailSubject = TextExpansion.expandSuperuser ("adminModifiedSuperuserAccount.subject", superuserRecord);
		String emailBody = TextExpansion.expandSuperuser ("adminModifiedSuperuserAccount.body", superuserRecord);

		String emailResult = EmailHelper.send (
			emailFrom,
			superuserRecord.email,
			emailSubject,
			emailBody,
			false
		);
		messages.addGenericMessage (emailResult);

		// send email copy to admin
		EmailHelper.send (
			emailFrom,
			SettingsHelper.readString ("copy.admin.toAddress", SettingsHelper.EmailSettings),
			emailSubject,
			emailBody,
			false
		);
	}

	/**
	 * Notify a superuser about the modification of his account profile.
	 *
	 * @param superuserRecord	The doctor record for the modified doctor.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void superuserProfileEdit (
		SuperuserRecord superuserRecord,
		String emailOld,
		String emailNew,
		MessagesInline messages
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;

		messageFrom = SettingsHelper.readString ("superuserProfile.from", SettingsHelper.EmailSettings);
		messageSubject = TextExpansion.expandSuperuser ("superuserProfile.subject", superuserRecord);
		messageBody = TextExpansion.expandSuperuser ("superuserProfile.body", superuserRecord);

		// If email address is changed then send copy to both new and old email addr
		String emailResult = EmailHelper.send (messageFrom, emailOld, messageSubject, messageBody, false);
		if (emailOld.equals (emailNew) == false) {
			EmailHelper.send (messageFrom, emailNew, messageSubject, messageBody, false);
		}
		messages.addGenericMessage (emailResult);

		// send email copy to admin
		EmailHelper.send (
			messageFrom,
			SettingsHelper.readString ("copy.admin.toAddress", SettingsHelper.EmailSettings),
			messageSubject,
			messageBody,
			false
		);
	}

	/**
	 * Notify a superuser about his account password.
	 *
	 * @param superuserRecord	The superuser record for the superuser.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void superuserForgotPassword (
		SuperuserRecord superuserRecord,
		MessagesInline messages
	) throws Exception
	{
		String emailFrom = SettingsHelper.readString ("settings.reminder.from", SettingsHelper.EmailSettings);
		String emailSubject = TextExpansion.expandSuperuser ("settings.reminder.subject", superuserRecord);
		String emailBody = TextExpansion.expandSuperuser ("settings.reminder.body", superuserRecord);

		String emailResult = EmailHelper.send (
			emailFrom,
			superuserRecord.email,
			emailSubject,
			emailBody,
			false
		);
		messages.addGenericMessage (emailResult);

		// send email copy to admin
		EmailHelper.send (
			emailFrom,
			SettingsHelper.readString ("copy.admin.toAddress", SettingsHelper.EmailSettings),
			emailSubject,
			emailBody,
			false
		);
	}

	/**
	 * Notify a patient and doctor to confirm signup is received.
	 *
	 * @param patientRecord	The patient record for the added patient.
	 * @param doctorRecord	The doctor record for the added patient.
	 * @param messages	The server messages object to which the success message is added.
	 */
	static public void patientSignup (
		PatientRecord patientRecord,
		DoctorRecord doctorRecord,
		MessagesInline messages
	) throws Exception
	{
		//convert checkinNotes %% to line break
		patientRecord.checkInNotes= formatList(patientRecord.checkInNotes, false);
		String myDoNotContactLink = PatientHelper.getOpenSchedulerApptUrl(patientRecord.patientId, "0", "DoNotContact");
		String includedLinks =
				"\n\n DO NOT WANT TO RECEIVE APPOINTMENT REMINDER? CLICK THIS LINK. \n\n" 
				+ myDoNotContactLink + "\n\n";
				
		//send email notification to doctor
		String emailResult = EmailHelper.send (
			SettingsHelper.readString ("toDoctor.newSignupReceived.from", SettingsHelper.EmailSettings),
			doctorRecord.email,
			TextExpansion.expandDoctorPatient ("toDoctor.newSignupReceived.subject", doctorRecord, patientRecord),
			TextExpansion.expandDoctorPatient ("toDoctor.newSignupReceived.body", doctorRecord, patientRecord)
			+ includedLinks,
			false
		);
		messages.addGenericMessage (emailResult);

		//send email notification to patient
		emailResult = EmailHelper.send (
				SettingsHelper.readString ("toPatient.newSignupReceived.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandDoctor ("toPatient.newSignupReceived.subject", doctorRecord),
				TextExpansion.expandDoctorPatient ("toPatient.newSignupReceived.body", doctorRecord, patientRecord)
				+ includedLinks,
				false
			);
		messages.addGenericMessage (emailResult);
	}

	/**
	 * Notify a patient and doctor about the status of signup.
	 *
	 * @param patientRecord	The patient record for the added patient.
	 * @param doctorRecord	The doctor record for the added patient.
	 * @param messages	The server messages object to which the success message is added.
	 * @param replyMessage	The reply message by doctor to be sent.
	 * @param reviewDecision The decision indicator (1= approved, 0=declined).
	 */
	static public void newPatientReview (
		PatientRecord patientRecord,
		DoctorRecord doctorRecord,
		MessagesInline messages,
		String replyMessage,
		String reviewDecision
	) throws Exception
	{
		String emailResult;
		//convert checkinNotes %% to line break
		patientRecord.checkInNotes= formatList(patientRecord.checkInNotes, false);
		//send email notification to doctor
		String emailSubject, emailBody;
		if (reviewDecision.equals("0")){
			emailSubject = TextExpansion.expandDoctorPatient ("toDoctor.newSignupDeclined.subject", doctorRecord, patientRecord);
			emailBody = TextExpansion.expandDoctorPatient ("toDoctor.newSignupDeclined.body", doctorRecord, patientRecord)
							+ "\n"+ replyMessage;
			emailResult = EmailHelper.send (
				SettingsHelper.readString ("toDoctor.newSignupDeclined.from", SettingsHelper.EmailSettings),
				doctorRecord.email,
				emailSubject,
				emailBody,
				false
			);
		}else{
			emailSubject = TextExpansion.expandDoctorPatient ("toDoctor.newSignupApproved.subject", doctorRecord, patientRecord);
			emailBody = TextExpansion.expandDoctorPatient ("toDoctor.newSignupApproved.body", doctorRecord, patientRecord)
							+ "\n"+ replyMessage;
			emailResult = EmailHelper.send (
				SettingsHelper.readString ("toDoctor.newSignupApproved.from", SettingsHelper.EmailSettings),
				doctorRecord.email,
				emailSubject,
				emailBody,
				false
			);
		}
		messages.addGenericMessage (emailResult);

		//send email notification to patient
		if (reviewDecision.equals("0")){
			emailSubject = TextExpansion.expandDoctor ("toPatient.newSignupDeclined.subject", doctorRecord);
			emailBody = TextExpansion.expandDoctorPatient ("toPatient.newSignupDeclined.body", doctorRecord, patientRecord)
							+ "\n"+ replyMessage;
			emailResult = EmailHelper.send (
					SettingsHelper.readString ("toPatient.newSignupDeclined.from", SettingsHelper.EmailSettings),
					patientRecord.email,
					emailSubject,
					emailBody,
					false
				);
		}else{
			emailSubject = TextExpansion.expandDoctor ("toPatient.newSignupApproved.subject", doctorRecord);
			emailBody = TextExpansion.expandDoctorPatient ("toPatient.newSignupApproved.body", doctorRecord, patientRecord)
							+ "\n"+ replyMessage;

			emailResult = EmailHelper.send (
					SettingsHelper.readString ("toPatient.newSignupApproved.from", SettingsHelper.EmailSettings),
					patientRecord.email,
					emailSubject,
					emailBody,
					false
				);
		}
		messages.addGenericMessage (emailResult);

	}


	/**
	 * Notify a patient about the creation of his account.
	 *
	 * @param patientRecord	The patient record for the added patient.
	 * @param doctorRecord	The doctor record for the added patient.
	 * @param messages	The server messages object to which the success message is added.
	 */
	static public void patientAdd (
		PatientRecord patientRecord,
		DoctorRecord doctorRecord,
		MessagesInline messages
	) throws Exception
	{
		String emailBody="newPatientRecord.body";
		String myDoNotContactLink = PatientHelper.getOpenSchedulerApptUrl(patientRecord.patientId, "0", "DoNotContact");
		String includedLinks =
				"\n\n DO NOT WANT TO RECEIVE APPOINTMENT REMINDER? CLICK THIS LINK. \n\n" 
				+ myDoNotContactLink + "\n\n";
				
		if (doctorRecord.hasWebAppt.equals("0")){
			emailBody="newPatientRecord.body.noWebInfo";
		}
		if (
			(patientRecord.email.equals ("") == false) &&
			(patientRecord.reminderType.equals ("2") || patientRecord.reminderType.equals ("3"))
		) {
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("newPatientRecord.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandDoctor ("newPatientRecord.subject", doctorRecord),
				TextExpansion.expandDoctorPatient (emailBody, doctorRecord, patientRecord)
				+ includedLinks,
				false
			);
			messages.addGenericMessage (emailResult);
		} else {
			messages.addGenericMessage ("error.email.noaddr");
		}
	}



	/**
	 * Notify a patient about the creation of his account.
	 * (This method is overloaded if additinal param is provided)
	 * @param patientRecord	The patient record for the added patient.
	 * @param doctorRecord	The doctor record for the added patient.
	 */
	static public void patientAdd (
		PatientRecord patientRecord,
		DoctorRecord doctorRecord
	) throws Exception
	{
		String myDoNotContactLink = PatientHelper.getOpenSchedulerApptUrl(patientRecord.patientId, "0", "DoNotContact");
		String includedLinks =
				"\n\n DO NOT WANT TO RECEIVE APPOINTMENT REMINDER? CLICK THIS LINK. \n\n" 
				+ myDoNotContactLink + "\n\n";
				
		String emailBody="newPatientRecord.body";
		if (doctorRecord.hasWebAppt.equals("0")){
			emailBody="newPatientRecord.body.noWebInfo";
		}
		if (
			(patientRecord.email.equals ("") == false) &&
			(patientRecord.reminderType.equals ("2") || patientRecord.reminderType.equals ("3"))
		) {
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("newPatientRecord.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandDoctor ("newPatientRecord.subject", doctorRecord),
				TextExpansion.expandDoctorPatient (emailBody, doctorRecord, patientRecord) 
				+ includedLinks,
				false
			);
		}
	}

	/**
	 * Notify a patient about the deletion of his account.
	 *
	 * @param patientRecord	The patient record for the deleted patient.
	 * @param doctorRecord	The doctor record for the deleted patient.
	 * @param messages	The server messages object to which the success message is added.
	 */
	static public void patientDelete (
		PatientRecord patientRecord,
		DoctorRecord doctorRecord,
		MessagesInline messages
	) throws Exception
	{
		if (
			(patientRecord.email.equals ("") == false) &&
			(patientRecord.reminderType.equals ("2") || patientRecord.reminderType.equals ("3"))
		) {
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("deletePatientRecord.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandDoctor ("deletePatientRecord.subject", doctorRecord),
				TextExpansion.expandDoctorPatient ("deletePatientRecord.body", doctorRecord, patientRecord),
				false
			);
			messages.addGenericMessage (emailResult);
		} else {
			messages.addGenericMessage ("error.email.noaddr");
		}
	}

	/**
	 * Notify a patient about the modification of his account.
	 *
	 * @param patientRecord	The patient record for the deleted patient.
	 * @param doctorRecord	The doctor record for the deleted patient.
	 * @param messages	The server messages object to which the success message is added.
	 */
	static public void patientEdit (
		PatientRecord patientRecord,
		DoctorRecord doctorRecord,
		MessagesInline messages
	) throws Exception
	{
		String emailBody="updatePatientRecord.body";
		if (doctorRecord.hasWebAppt.equals("0")){
			emailBody="updatePatientRecord.body.noWebInfo";
		}

		if (
			(patientRecord.email.equals ("") == false) &&
			(patientRecord.reminderType.equals ("2") || patientRecord.reminderType.equals ("3"))
		) {
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("updatePatientRecord.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandDoctor ("updatePatientRecord.subject", doctorRecord),
				TextExpansion.expandDoctorPatient (emailBody, doctorRecord, patientRecord),
				false
			);
			messages.addGenericMessage (emailResult);
		} else {
			messages.addGenericMessage ("error.email.noaddr");
		}
	}

	/**
	 * Notify a patient about the addition of an appointment.
	 *
	 * @param appointmentInfo	The appointment record for the added appointment.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void appointmentAdd (
		AppointmentInfo appointmentInfo,
		MessagesInline messages
	) throws Exception
	{
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		String patientPrefers;

		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);
		patientPrefers = patientRecord.reminderType;

		String emailBodyProperty="apptAdded.body";
		if (doctorRecord.hasWebAppt.equals("0")){
			emailBodyProperty="apptAdded.body.noWebInfo";
		}

		if (
			(patientRecord.email.equals ("") == false) &&
			(patientPrefers.equals ("2") || patientPrefers.equals ("3"))
		) {
			String subject, bodyText;
			EmailTemplateRecord etr = new EmailTemplateRecord();
			if("1".equals(doctorRecord.customizeEmail) && etr.readByFilter(new String[][]{{"doctorId", doctorRecord.doctorId},{"templateName", Template.ApptConfirmationEmail.toString()}})) {
				subject = TemplateExpander.expand(etr.subject, patientRecord, doctorRecord, appointmentInfo);
				bodyText = TemplateExpander.expand(etr.templateContent, patientRecord, doctorRecord, appointmentInfo);
			} else {
				subject = TextExpansion.expandDoctor ("apptAdded.subject", doctorRecord);
				bodyText = TextExpansion.expandAppointment (emailBodyProperty, doctorRecord, patientRecord, appointmentInfo);
			}
			//Get included link for open scheduler user
			String openSchedulerLinks;
			if (doctorRecord.newPatient.equals("4")){
				String myRecordConfirmationLink = PatientHelper.getOpenSchedulerApptUrl(appointmentInfo.patientId, appointmentInfo.apptId, "ConfirmAppt");
				String myApptCancellationLink = PatientHelper.getOpenSchedulerApptUrl(appointmentInfo.patientId, appointmentInfo.apptId, "CancelAppt");
				MyNetworkData myNetworkData = MyNetworkData.getInstance();
				String myExistingApptLink = (myNetworkData.myNewPatientSignupURL+"?officePhone="+doctorRecord.bizPhone).replace("index2", "index");

				openSchedulerLinks = 
				"\n\n TO CANCEL YOUR APPOINTMENT, CLICK THIS LINK.  \n\n"
				+ myApptCancellationLink + "\n\n"
				+ "\n\n TO ADD A NEW APPOINTMENT, CLICK THIS LINK. \n\n"
				+ myExistingApptLink + "\n\n"
				+ "\n\n TO RESCHEDULE, SIMPLY CANCEL YOUR APPOINTMENT AND ADD A NEW ONE. \n\n";
			}else{
				openSchedulerLinks=""; //nothing if no such service
			}
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptAdded.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				subject,
				bodyText
				+ openSchedulerLinks
				+ patientSMSSignupAd(doctorRecord, patientRecord)
				+ newPatientSignupAd(doctorRecord),
				false
			);
			messages.addGenericMessage (emailResult);
		}
	}

	/**
	 * Notify a patient about the addition of an appointment.
	 * This method is called without inline message object
	 * @param appointmentInfo	The appointment record for the added appointment.
	 *
	 */
	static public void appointmentAdd (
		AppointmentInfo appointmentInfo
	) throws Exception
	{
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		String patientPrefers;

		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);
		patientPrefers = patientRecord.reminderType;

		String emailBodyProperty="apptAdded.body";
		if (doctorRecord.hasWebAppt.equals("0")){
			emailBodyProperty="apptAdded.body.noWebInfo";
		}

		if (
			(patientRecord.email.equals ("") == false) &&
			(patientPrefers.equals ("2") || patientPrefers.equals ("3"))
		) {
			String subject, bodyText;
			EmailTemplateRecord etr = new EmailTemplateRecord();
			if("1".equals(doctorRecord.customizeEmail) && etr.readByFilter(new String[][]{{"doctorId", doctorRecord.doctorId},{"templateName", Template.ApptConfirmationEmail.toString()}})) {
				subject = TemplateExpander.expand(etr.subject, patientRecord, doctorRecord, appointmentInfo);
				bodyText = TemplateExpander.expand(etr.templateContent, patientRecord, doctorRecord, appointmentInfo);
			} else {
				subject = TextExpansion.expandDoctor ("apptAdded.subject", doctorRecord);
				bodyText = TextExpansion.expandAppointment (emailBodyProperty, doctorRecord, patientRecord, appointmentInfo);
			}
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptAdded.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				subject,
				bodyText
				+ patientSMSSignupAd(doctorRecord, patientRecord)
				+ newPatientSignupAd(doctorRecord),
				false
			);
		}
	}

	/**
	 * Notify a patient about the addition of recurrent appointments.
	 *
	 * @param appointmentInfo	The appointment records [] for the added appointments.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void recurrentAppointmentAdd(
		AppointmentInfo[] appointmentInfo,
		MessagesInline messages
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		String patientPrefers;

		for (int i=0; i<appointmentInfo.length; i++){
			if ((appointmentInfo[i].apptId != null)) {
				patientRecord.readById (appointmentInfo[i].patientId);
				doctorRecord.readById (patientRecord.doctorId);
				//logger.info("find doctor and patient record and break");
				break;
			}
		}

		patientPrefers = patientRecord.reminderType;
		String emailBody="recurrentApptAdded.body";
		if (doctorRecord.hasWebAppt.equals("0")){
			emailBody="recurrentApptAdded.body.noWebInfo";
		}
		if (
			(patientRecord.email.equals ("") == false) &&
			(patientPrefers.equals ("2") || patientPrefers.equals ("3"))
		) {
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("recurrentApptAdded.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandDoctor ("recurrentApptAdded.subject", doctorRecord),
				TextExpansion.expandRecurrentAppointments (emailBody, doctorRecord, patientRecord, appointmentInfo)
				+ patientSMSSignupAd(doctorRecord, patientRecord)
				+ newPatientSignupAd(doctorRecord),
				false
			);
			messages.addGenericMessage (emailResult);
		}
	}

	/**
	 * Notify a patient about the cancellation of recurrent appointments.
	 *
	 * @param appointmentInfo	The appointment records [] for the cancelled appointments.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void recurrentAppointmentCancel(
		AppointmentInfo[] appointmentInfo,
		MessagesInline messages
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		String patientPrefers;
		int callStartTime;

		for (int i=0; i<appointmentInfo.length; i++){
			if ((appointmentInfo[i].apptId != null)) {
				patientRecord.readById (appointmentInfo[i].patientId);
				doctorRecord.readById (patientRecord.doctorId);
				//logger.info("find doctor and patient record and break");
				break;
			}
		}
		// save call log info for each cancelled recurrent appt.
		CallRecordLog [] callRecordLog = new CallRecordLog [appointmentInfo.length];
		for (int i=0; i<appointmentInfo.length; i++){
			callRecordLog[i] = new CallRecordLog();
			callStartTime=TimeHelper.currentEpochSecond ();
			String logResult= callRecordLog[i].logCallRecord(
								patientRecord.doctorId,						//doctorId
								"cancelRecurrent",							//callType
								String.valueOf(callStartTime),				//callStartTime
								"none",										//patientPhone
								String.valueOf(appointmentInfo[i].patientId),	//patientId
								"none",										//transferredToPhone
								String.valueOf(appointmentInfo[i].epochDay),	//apptEpochDay
								String.valueOf(appointmentInfo[i].rangeStart),	//apptStartSecond
								"none",										//errorEventLog
								"success"									//callResult
			);
			logger.debug("Call record logging for cancel recurrent appts is ..."+ logResult+"\n");
		}

		patientPrefers = patientRecord.reminderType;
		String emailBody="recurrentApptCancelled.body.noWebInfo";

		if (
			(patientRecord.email.equals ("") == false) &&
			(patientPrefers.equals ("2") || patientPrefers.equals ("3"))
		) {
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("recurrentApptCancelled.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandDoctor ("recurrentApptCancelled.subject", doctorRecord),
				TextExpansion.expandRecurrentAppointments (emailBody, doctorRecord, patientRecord, appointmentInfo)
				+ patientSMSSignupAd(doctorRecord, patientRecord)
				+ newPatientSignupAd(doctorRecord),
				false
			);
			messages.addGenericMessage (emailResult);
		}
	}

	/**
	 * Notify a doctor about an appointment made by patient via self service.
	 *
	 * @param appointmentInfo	The appointment record for the new appointment.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void appointmentAddedByPatient(
		AppointmentInfo appointmentInfo
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		String patientPrefers;
		
		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);
		String patientOrClient = DoctorHelper.patientOrCustomer(patientRecord.doctorId);
		String emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptAddedByPatient.from", SettingsHelper.EmailSettings),
				doctorRecord.email,
				TextExpansion.expandAppointment ("apptAddedByPatient.subject", doctorRecord, patientRecord, appointmentInfo).replace("Temp_Record", "A new "+patientOrClient),
				TextExpansion.expandAppointment ("apptAddedByPatient.body", doctorRecord, patientRecord, appointmentInfo),
				false
		);
	}
	
	/**    
	 * Request a patient to fill a customized question form.
	 * It contains the link that patient can click to get 
	 * question form.
	 * @param appointmentInfo	The appointment record for the appointment.
	 */
	static public void requestPatientQuestionForm (
		AppointmentInfo appointmentInfo,
		ApptProfileRecord apptProfileRecord
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();

		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);

		// Get myOpenSchedulerLink from myNetworkData
   		String myQuestionFormLink = DoctorHelper.getApptFormUrl(doctorRecord.doctorId, appointmentInfo.apptId);
		
		if (patientRecord.email.equals ("") == false){
			String from = SettingsHelper.readString ("patient.apptRequiresForm.from", SettingsHelper.EmailSettings);
			String subject = TextExpansion.expandAppointment ("patient.apptRequiresForm.subject", doctorRecord, patientRecord, appointmentInfo);
			String body = TextExpansion.expandAppointment ("patient.apptRequiresForm.body", doctorRecord, patientRecord, appointmentInfo);
			body = body.replace("APPT_PROFILE_NAME", apptProfileRecord.name);
			body = body.replace("QUESTION_FORM_LINK", myQuestionFormLink);
			String emailResult = EmailHelper.send (
				from,
				patientRecord.email,
				subject,
				body,
				false
			);
		}

	}
	
	
	
	/**
	 * Notify a doctor about the cancellation of an appointment.
	 *
	 * @param appointmentInfo	The appointment record for the cancelled appointment.
	 * @param messages		The server messages object to which the success message is added.
	 * The message object is null when method is called By ANGELA instead of GUI
	 */
	static public void appointmentCancelByPatient(
		AppointmentInfo appointmentInfo,
		MessagesInline messages
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		String patientPrefers;

		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);
		String emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptCancelledByPatient.from", SettingsHelper.EmailSettings),
				doctorRecord.email,
				TextExpansion.expandAppointment ("apptCancelledByPatient.subject", doctorRecord, patientRecord, appointmentInfo),
				TextExpansion.expandAppointment ("apptCancelledByPatient.body", doctorRecord, patientRecord, appointmentInfo),
				false
		);
		if (messages != null){
			messages.addGenericMessage (emailResult);
		}

		// copy email to patient
		if (
			(patientRecord.email.equals ("") == false) &&
			(patientRecord.reminderType.equals ("2") || patientRecord.reminderType.equals ("3"))
			)	{
			emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptCancelledByPatient.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				"COPY: "+ TextExpansion.expandAppointment ("apptCancelledByPatient.subject", doctorRecord, patientRecord, appointmentInfo),
				TextExpansion.expandAppointment ("apptCancelledByPatient.body", doctorRecord, patientRecord, appointmentInfo),
				false
			);

		}

		
		// save the call log info for GUI call, speech call save log in CancelApptOfDialogFormat
		if (messages != null){
			int callStartTime=TimeHelper.currentEpochSecond ();
			CallRecordLog callRecordLog = new CallRecordLog();
			String logResult= callRecordLog.logCallRecord(
								patientRecord.doctorId,						//doctorId
								"cancelByPatient",							//callType
								String.valueOf(callStartTime),				//callStartTime
								"none",										//patientPhone
								String.valueOf(appointmentInfo.patientId),	//patientId
								"none",										//transferredToPhone
								String.valueOf(appointmentInfo.epochDay),	//apptEpochDay
								String.valueOf(appointmentInfo.rangeStart),	//apptStartSecond
								"none",										//errorEventLog
								"success"									//callResult
							);
			logger.debug("Call record logging for cancel appt by patient is ..."+ logResult+"\n");
		}
	}

	/**
	 * Notify a patient about the cancellation of an appointment.
	 *
	 * @param appointmentInfo	The appointment record for the cancelled appointment.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void appointmentCancel (
		AppointmentInfo appointmentInfo,
		MessagesInline messages
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		String patientPrefers;
		String callResult="";
		String errorEventLog="Doctor_cancel_appointment";
		int callStartTime=0;

		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);
		// if timeZone based call time is not permitted then send alert and not call
		boolean callingTimeOK = TimeHelper.isCallingTimeAllowed(doctorRecord.timeZone);
		if (callingTimeOK == false){
			// make a cancelNoCall Record and notification
			messages.addGenericMessage ("failed.callingTimeNotAllowed");
			NotifyHelper.appointmentCancelNoCall(appointmentInfo, messages);
			return;
		}
		if (
			(patientRecord.email.equals ("") == false) &&
			(patientRecord.reminderType.equals ("2") || patientRecord.reminderType.equals ("3"))
		) {
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptCancelled.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandAppointment ("apptCancelled.subject", doctorRecord, patientRecord, appointmentInfo),
				TextExpansion.expandAppointment ("apptCancelled.body", doctorRecord, patientRecord, appointmentInfo),
				false
			);
			messages.addGenericMessage (emailResult);
		}
		if (
			(patientRecord.phone.equals ("") == false) && callingTimeOK &&
			(patientRecord.reminderType.equals ("1") || patientRecord.reminderType.equals ("3"))
		) {
			callStartTime=TimeHelper.currentEpochSecond ();
			callResult= PhoneHelper.callForCancelAppt (patientRecord, doctorRecord,appointmentInfo);
			logger.debug("Cancel appt call result is ..."+ callResult+"\n");
			//boolean success = (callResult != null) && callResult.substring(0,7).equalsIgnoreCase ("success");
			boolean success = PhoneHelper.isCallSuccessful(callResult);
			if (success){
				messages.addGenericMessage ("success.phoneCall");
				callResult = "success";			}else{
				messages.addGenericMessage ("failed.phoneCall");
				callResult ="unanswered";			}
		}
		// save the call log info.
		CallRecordLog callRecordLog = new CallRecordLog();
		String logResult= callRecordLog.logCallRecord(
								patientRecord.doctorId,						//doctorId
								"cancelCall",								//callType
								String.valueOf(callStartTime),				//callStartTime
								patientRecord.phone,						//patientPhone
								String.valueOf(appointmentInfo.patientId),	//patientId
								"none",										//transferredToPhone
								String.valueOf(appointmentInfo.epochDay),	//apptEpochDay
								String.valueOf(appointmentInfo.rangeStart),	//apptStartSecond
								errorEventLog,								//errorEventLog
								callResult									//callResult
							);
		logger.debug("Call record logging for cancel appt with call is ..."+ callResult+"\n");
	}

	/**
	 * Notify a patient about the cancellation of an appointment.
	 * This method will NOT send phone call based on admin's request
	 * @param appointmentInfo	The appointment record for the cancelled appointment.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void appointmentCancelNoCall (
		AppointmentInfo appointmentInfo,
		MessagesInline messages
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		String patientPrefers;
		String callResult="";
		String errorEventLog="Doctor_cancel_appointment";
		int callStartTime=0;

		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);
		if (
			(patientRecord.email.equals ("") == false) &&
			(patientRecord.reminderType.equals ("2") || patientRecord.reminderType.equals ("3"))
		) {
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptCancelled.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandAppointment ("apptCancelled.subject", doctorRecord, patientRecord, appointmentInfo),
				TextExpansion.expandAppointment ("apptCancelled.body", doctorRecord, patientRecord, appointmentInfo),
				false
			);
			messages.addGenericMessage (emailResult);
		}

		// save the call log info.
		callStartTime=TimeHelper.currentEpochSecond ();
		CallRecordLog callRecordLog = new CallRecordLog();
		String logResult= callRecordLog.logCallRecord(
								patientRecord.doctorId,						//doctorId
								"cancelNoCall",								//callType
								String.valueOf(callStartTime),				//callStartTime
								"none",										//patientPhone
								String.valueOf(appointmentInfo.patientId),	//patientId
								"none",										//transferredToPhone
								String.valueOf(appointmentInfo.epochDay),	//apptEpochDay
								String.valueOf(appointmentInfo.rangeStart),	//apptStartSecond
								"none",										//errorEventLog
								"success"									//callResult
							);
		logger.debug("Call record logging for cancel appt no call is ..."+ logResult+"\n");

	}

	/**
	 * Notify a patient about the cancellation of an unpaid appointment.
	 * @param appointmentInfo	The appointment record for the cancelled appointment.
	 */
	static public void cancelUnpaidAppt (
		AppointmentInfo appointmentInfo
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();

		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);
		if (patientRecord.email.equals ("") == false){
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("unpaidApptCancelled.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandAppointment ("unpaidApptCancelled.subject", doctorRecord, patientRecord, appointmentInfo),
				TextExpansion.expandAppointment ("unpaidApptCancelled.body", doctorRecord, patientRecord, appointmentInfo),
				false
			);
		}
	}

	/**
	 * Notify a patient about the cancellation of an emergency appointment.
	 * This method will NOT send phone call based on admin's request
	 * @param emergencyInfo.	The emergency appointment record for the cancelled appointment.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void EmergencyApptCancelNoCall (
		EmergencyInfo emergencyInfo,
		MessagesInline messages
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		String patientPrefers;
		String callResult="";
		String errorEventLog="Doctor_cancel_appointment";
		int callStartTime=0;

		patientRecord.readById (emergencyInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);
		if (
			(patientRecord.email.equals ("") == false) &&
			(patientRecord.reminderType.equals ("2") || patientRecord.reminderType.equals ("3"))
		) {
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptCancelled.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandAppointment ("apptCancelled.subject", doctorRecord, patientRecord, emergencyInfo),
				TextExpansion.expandAppointment ("apptCancelled.body", doctorRecord, patientRecord, emergencyInfo),
				false
			);
			messages.addGenericMessage (emailResult);
		}

		// save the call log info.
		callStartTime=TimeHelper.currentEpochSecond ();
		CallRecordLog callRecordLog = new CallRecordLog();
		String logResult= callRecordLog.logCallRecord(
								patientRecord.doctorId,						//doctorId
								"cancelNoCall",								//callType
								String.valueOf(callStartTime),				//callStartTime
								"none",										//patientPhone
								String.valueOf(emergencyInfo.patientId),	//patientId
								"none",										//transferredToPhone
								String.valueOf(emergencyInfo.epochDay),		//apptEpochDay
								String.valueOf(emergencyInfo.rangeStart),	//apptStartSecond
								"none",										//errorEventLog
								"success"									//callResult
							);
		logger.debug("Call record logging for cancel appt no call is ..."+ logResult+"\n");

	}

	/**
	 * Notify a patient about the rescheduling of an appointment.
	 *
	 * @param appointmentInfo	The appointment record for the modified appointment.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void appointmentEdit(
		int oldEpochday,
		int oldRangeStart,
		AppointmentInfo appointmentInfo,
		MessagesInline messages
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);
		if (
			(patientRecord.email.equals ("") == false) &&
			(patientRecord.reminderType.equals ("2") || patientRecord.reminderType.equals ("3"))
		) {
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptEdited.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandDoctor ("apptEdited.subject", doctorRecord),
				TextExpansion.expandAppointment ("apptEdited.body", doctorRecord, patientRecord, appointmentInfo)
				+ patientSMSSignupAd(doctorRecord, patientRecord)
				+ newPatientSignupAd(doctorRecord),
				false
			);
			messages.addGenericMessage (emailResult);
		}
		// save the call log info for every rescheduled appt.
		int callStartTime=TimeHelper.currentEpochSecond ();
		CallRecordLog callRecordLog = new CallRecordLog();
		String logResult= callRecordLog.logCallRecord(
							patientRecord.doctorId,						//doctorId
							"changedByDoctor",							//callType
							String.valueOf(callStartTime),				//callStartTime
							"none",										//patientPhone
							String.valueOf(appointmentInfo.patientId),	//patientId
							"none",										//transferredToPhone
							String.valueOf(oldEpochday),				//old apptEpochDay
							String.valueOf(oldRangeStart),				//old apptStartSecond
							"none",										//errorEventLog
							"success"									//callResult
						);
		logger.debug("Call record logging for reschedule appt by doctor no call is ..."+ logResult+"\n");

	}

	/**
	 * Notify the doctor AND patient about the rescheduling of an appointment by patient.
	 *
	 * @param appointmentInfo	The appointment record for the cancelled appointment.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void appointmentChangedByPatient (
		int oldEpochday,
		int oldRangeStart,
		AppointmentInfo appointmentInfo,
		MessagesInline messages
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		String patientPrefers;

		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);
		// email to doctor
		String emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptChangededByPatient.from", SettingsHelper.EmailSettings),
				doctorRecord.email,
				TextExpansion.expandAppointment ("apptChangededByPatient.subject", doctorRecord, patientRecord, appointmentInfo),
				TextExpansion.expandAppointment ("apptChangededByPatient.body", doctorRecord, patientRecord, appointmentInfo),
				false
		);
		// email to patient
		if (
			(patientRecord.email.equals ("") == false) &&
			(patientRecord.reminderType.equals ("2") || patientRecord.reminderType.equals ("3"))
			)	{
			emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptEdited.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandAppointment ("apptEdited.subject", doctorRecord, patientRecord, appointmentInfo),
				TextExpansion.expandAppointment ("apptEdited.body", doctorRecord, patientRecord, appointmentInfo),
				false
			);
			messages.addGenericMessage (emailResult);
		}
		// save the call log info.
		int callStartTime=TimeHelper.currentEpochSecond ();
		CallRecordLog callRecordLog = new CallRecordLog();
		String logResult= callRecordLog.logCallRecord(
								patientRecord.doctorId,						//doctorId
								"changedByPatient",							//callType
								String.valueOf(callStartTime),				//callStartTime
								"none",										//patientPhone
								String.valueOf(appointmentInfo.patientId),	//patientId
								"none",										//transferredToPhone
								String.valueOf(oldEpochday),				//apptEpochDay
								String.valueOf(oldRangeStart),				//apptStartSecond
								"none",										//errorEventLog
								"success"									//callResult
							);
		logger.debug("Call record logging for reschedule appt by patient is ..."+ logResult+"\n");

	}


	/**
	* This method send patient a email confirm
	* new sms setting
	*/
	static public String doctorEmailTemplateModified (DoctorRecord doctorRecord, String templateName) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		if (doctorRecord.email.equals ("")){
			return "error.email.noaddr";
		}
		messageFrom = SettingsHelper.readString ("doctorEmailTemplateModified.from", SettingsHelper.EmailSettings);

		messageBody = TextExpansion.expandDoctor("doctorEmailTemplateModified.body", doctorRecord);
		String url = DoctorHelper.getTemplateCustomizationUrl(doctorRecord.doctorId);
		messageBody = MessageFormat.format(messageBody, templateName, url);

		messageSubject = TextExpansion.expandDoctor("doctorEmailTemplateModified.subject", doctorRecord);
		messageSubject = MessageFormat.format(messageSubject, templateName);


		String emailResult = EmailHelper.send (
						messageFrom,
						doctorRecord.email,
						messageSubject,
						messageBody,
						false
		);
		return emailResult;
	}

	/**
	* This method send patient a email confirm
	* new sms setting
	*/
	static public String patientSMSSubscribed (PatientRecord patientRecord) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		DoctorRecord doctorRecord = new DoctorRecord ();

		doctorRecord.readById (patientRecord.doctorId);
		if (patientRecord.email.equals ("") == true){
			return "error.email.noaddr";
		}
		messageFrom = SettingsHelper.readString ("patientSMSSubscribed.from", SettingsHelper.EmailSettings);
		messageSubject = SettingsHelper.readString ("patientSMSSubscribed.subject", SettingsHelper.EmailSettings);
		messageBody = TextExpansion.expandDoctorPatient("patientSMSSubscribed.body", doctorRecord, patientRecord)
						+ patientSMSSignupAd(doctorRecord, patientRecord)
						+ newPatientSignupAd(doctorRecord);

		String emailResult = EmailHelper.send (
						messageFrom,
						patientRecord.email,
						messageSubject,
						messageBody,
						false
		);
		return emailResult;
	}

	/**
	* This method send patient a email confirm
	* removal of new sms setting
	*/
	static public String patientSMSUnsubscribed (PatientRecord patientRecord) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		DoctorRecord doctorRecord = new DoctorRecord ();

		doctorRecord.readById (patientRecord.doctorId);
		if (patientRecord.email.equals ("") == true){
			return "error.email.noaddr";
		}
		messageFrom = SettingsHelper.readString ("patientSMSUnsubscribed.from", SettingsHelper.EmailSettings);
		messageSubject = SettingsHelper.readString ("patientSMSUnsubscribed.subject", SettingsHelper.EmailSettings);
		messageBody = TextExpansion.expandDoctorPatient("patientSMSUnsubscribed.body", doctorRecord, patientRecord)
					+ patientSMSSignupAd(doctorRecord, patientRecord)
					+ newPatientSignupAd(doctorRecord);

		String emailResult = EmailHelper.send (
						messageFrom,
						patientRecord.email,
						messageSubject,
						messageBody,
						false
		);
		return emailResult;
	}
	/**
 	* This method sends patient follow up (callBack) email.
 	*/
	static public String sendPatientFollowupReminder(CallBackInfo callBackInfo)throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		String result="notsent";

		if(callBackInfo.patientRecord.email.length()== 0){
			result = "error.email.noaddr";
			return result;
		}
		messageFrom = SettingsHelper.readString ("shell.emailCallBack.from", SettingsHelper.EmailSettings);

		EmailTemplateRecord etr = new EmailTemplateRecord();
		if("1".equals(callBackInfo.doctorRecord.customizeEmail) && etr.readByFilter(new String[][]{{"doctorId", callBackInfo.doctorRecord.doctorId},{"templateName", Template.ApptFollowUpEmail.toString()}})) {
			messageSubject = TemplateExpander.expand(etr.subject, callBackInfo.patientRecord, callBackInfo.doctorRecord, callBackInfo.apptProfileRecord);
			messageBody = TemplateExpander.expand(etr.templateContent, callBackInfo.patientRecord, callBackInfo.doctorRecord, callBackInfo.apptProfileRecord)
			+ patientSMSSignupAd(callBackInfo.doctorRecord, callBackInfo.patientRecord)
			+ newPatientSignupAd(callBackInfo.doctorRecord);
		} else {
			//Use standard email template
			messageSubject = TextExpansion.expandDoctor (
				"shell.emailCallBack.subject",
				callBackInfo.doctorRecord
			);
			messageBody = TextExpansion.expandCallBack (
				"shell.emailCallBack.body",
				callBackInfo
			)
			+ patientSMSSignupAd(callBackInfo.doctorRecord, callBackInfo.patientRecord)
			+ newPatientSignupAd(callBackInfo.doctorRecord);
		}

		result = EmailHelper.send (
			messageFrom,
			callBackInfo.patientRecord.email,
			messageSubject,
			messageBody,
			false
		);

		return result;
	}

	/**
 	* This method sends patient appt reminder email.
 	*/
	static public String sendPatientEmailReminder(ReminderInfo reminderInfo)throws Exception
	{
			String messageFrom, messageSubject, messageBody;
			String result="notsent";

			// send email only when patient email addr is present
			if(reminderInfo.patientRecord.email.length()== 0){
				result = "error.email.noaddr";
				return result;
			}
			messageFrom = SettingsHelper.readString ("shell.emailReminder.from", SettingsHelper.EmailSettings);
			EmailTemplateRecord etr = new EmailTemplateRecord();
			if("1".equals(reminderInfo.doctorRecord.customizeEmail) && etr.readByFilter(new String[][]{{"doctorId", reminderInfo.doctorRecord.doctorId},{"templateName", Template.ApptReminderEmail.toString()}})) {
				messageSubject = TemplateExpander.expand(etr.subject, reminderInfo.patientRecord, reminderInfo.doctorRecord, reminderInfo.appointmentInfo);
				messageBody = TemplateExpander.expand(etr.templateContent, reminderInfo.patientRecord, reminderInfo.doctorRecord, reminderInfo.appointmentInfo);
			} else {
				messageSubject = TextExpansion.expandDoctorPatient ("shell.emailReminder.subject", reminderInfo.doctorRecord, reminderInfo.patientRecord);
				messageBody = TextExpansion.expandAppointment ("shell.emailReminder.body", reminderInfo);
			}
			messageBody += patientSMSSignupAd(reminderInfo.doctorRecord, reminderInfo.patientRecord) + newPatientSignupAd(reminderInfo.doctorRecord);

			result = EmailHelper.send (
				messageFrom,
				reminderInfo.patientRecord.email,
				messageSubject,
				messageBody,
				false
			);
			return result;
	}

	/**
 	* This method sends prepaid appt reminder email.
 	*/
	static public String sendPrepaidEmailReminder(PrepaidReminderInfo prepaidReminderInfo)throws Exception
	{
			String messageFrom, messageSubject, messageBody;
			String result="notsent";

			// send email only when patient email addr is present
			if(prepaidReminderInfo.importCallRecord.email.length()== 0){
				result = "error.email.noaddr";
				return result;
			}
			messageFrom = SettingsHelper.readString ("shell.emailReminder.from", SettingsHelper.EmailSettings);
			EmailTemplateRecord etr = new EmailTemplateRecord();
			if("1".equals(prepaidReminderInfo.doctorRecord.customizeEmail) && etr.readByFilter(new String[][]{{"doctorId", prepaidReminderInfo.doctorRecord.doctorId},{"templateName", Template.ApptReminderEmail.toString()}})) {
				messageSubject = TemplateExpander.expand(etr.subject, prepaidReminderInfo.importCallRecord, prepaidReminderInfo.doctorRecord);
				messageBody = TemplateExpander.expand(etr.templateContent, prepaidReminderInfo.importCallRecord, prepaidReminderInfo.doctorRecord);
			} else {
				messageSubject = TextExpansion.expandDoctorPatient ("shell.emailReminder.subject", prepaidReminderInfo.doctorRecord, prepaidReminderInfo.importCallRecord);
				messageBody = TextExpansion.expandImportReminderInfo ("shell.emailReminder.body", prepaidReminderInfo);
			}
			
			result = EmailHelper.send (
				messageFrom,
				prepaidReminderInfo.importCallRecord.email,
				messageSubject,
				messageBody,
				false
			);
			return result;
	}

	
	
	/**
 	* This method sends admin an alert email in case of java exception
 	* that cause SPEECH function termination or failure
 	* NOTE: this method will not throw exception as it is called
 	* to report exception.
 	*/
	static public void alertFromANGELA(
		String methodName,
		String alertMessage
	)
	{
	try{
		int timeOfAlert = TimeHelper.currentEpochSecond ();
		SimpleDateFormat myFormat = TimeHelper.loggerOutputFormat;
		String s_timeOfAlert = TimeHelper.daySecondsToString(timeOfAlert, "America/Chicago", myFormat);
		String serverPairNames = SettingsHelper.readString("reminder.serverPairNames", SettingsHelper.ReminderSettings);
		//Send an alert report email to support admin
		EmailHelper.send (
					"myReminder@angelspeech.com",
					SettingsHelper.readString ("copy.support.toAddress", SettingsHelper.EmailSettings),
					"RE: Admin ALERT from ANGELA AngelaOnNetDBMgr ",
					"Alerting Message from server " + serverPairNames + ".\n\n" +
					"Alert received on " + s_timeOfAlert + ".\n" +
					"Alert message: \n\n" +
					alertMessage +
					"\n\n\n",
					false
				);
		return;
	} catch (Exception ex) {
       	logger.error("catch Exception sendAdminAlertEmail()...\n" + ex.toString());
		return;
    }
	}
	
	/**
 	* This method sends admin an alert email in case of java exception
 	* that cause cron job function termination or failure
 	* NOTE: this method will not throw exception as it is called
 	* to report exception.
 	*/
	static public void sendAdminAlertEmail(
		String cronJobName,
		String alertMessage
	)
	{
		try{
			int timeOfAlert = TimeHelper.currentEpochSecond ();
			SimpleDateFormat myFormat = TimeHelper.loggerOutputFormat;
			String s_timeOfAlert = TimeHelper.daySecondsToString(timeOfAlert, "America/Chicago", myFormat);
			String serverPairNames = SettingsHelper.readString("reminder.serverPairNames", SettingsHelper.ReminderSettings);
			//Send an alert report email to support admin
			EmailHelper.send (
					"myReminder@angelspeech.com",
					SettingsHelper.readString ("copy.support.toAddress", SettingsHelper.EmailSettings),
					"RE: Admin ALERT by cron job for "+ cronJobName,
					"Alerting Message from server " + serverPairNames + ".\n\n" +
					"Alert received on " + s_timeOfAlert + ".\n" +
					"Alert message: \n\n" +
					alertMessage +
					"\n\n\n",
					false
				);
			return;
		} catch (Exception ex) {
       		logger.error("catch Exception sendAdminAlertEmail()...\n" + ex.toString());
			return;
    	}
	}
		
	/**
 	* This method sends admin an generic alert email with an admin function message
 	* i.e., prepaid admin credit, other user profile changes
 	* 
 	*/
	static public void adminInfoEmail(
		String emailAdminTitle,
		String emailAdminMessage
	)
	{
		try{
			int timeOfAlert = TimeHelper.currentEpochSecond ();
			SimpleDateFormat myFormat = TimeHelper.loggerOutputFormat;
			String s_timeOfAlert = TimeHelper.daySecondsToString(timeOfAlert, "America/Chicago", myFormat);

			//Send an alert report email to support admin
			EmailHelper.send (
					"myReminder@angelspeech.com",
					SettingsHelper.readString ("copy.support.toAddress", SettingsHelper.EmailSettings),
					emailAdminTitle,
					"Info email received on " + s_timeOfAlert + ".\n" +
					emailAdminMessage +
					"\n\n\n",
					false
				);
			return;
		} catch (Exception ex) {
       		logger.error("catch Exception sendAdminAlertEmail()...\n" + ex.toString());
			return;
    	}
	}

	/**
	*	When an exception occurs, this code snippet will print the stack trace
	*	of the exception to a string. This way, the exception stack trace at
	*	runtime can be recorded in log file.
	*/

	public static String getStackTrace(Throwable t)
   	{
       	StringWriter sw = new StringWriter();
      	PrintWriter pw = new PrintWriter(sw, true);
       	t.printStackTrace(pw);
       	pw.flush();
       	sw.flush();
       	return sw.toString();
   	}
	/**
	 * This method is used to convert string with delimiter %% to a string with line breaks.
	 * HTML string should use <br> tags for line breaking, and string for email body should use \n
	 *
	 * @param dbList insurance list from database
	 * @param toHtml - boolean indicator for HTML/plain text line breaks.
	 * @return Insurance List string with line breaks.
	 */
	public static String formatList(String dbList, boolean toHtml)
	{
		String lineBreak = "\n";
		if(toHtml) {
			lineBreak = "<br>";
		}
		dbList = dbList.replaceAll("%%", lineBreak);
		return dbList;
	}

	/**
	* This method create an advertisement message for patient
	* record to encourage patient to signup for
	* Text message reminder
	*/
	public static String patientSMSSignupAd(
		DoctorRecord doctorRecord,
		PatientRecord patientRecord
	) throws Exception
	{
		String adMessage="";
		String mySignupLink = PatientHelper.getSMSUrl(patientRecord.patientId);
		if (doctorRecord.smsService.equals ("1")){
			//create an ad that will append to patient email
			adMessage = "\n\n WANT TEXT MESSAGE APPOINTMENT REMINDER?"+ "\n\n"
						+ "Just click on this link to signup! " + "\n\n"
						+ mySignupLink;

		}
		logger.debug("adMessage is ..."+adMessage);
		return adMessage;
	}
	/**
	* This method create an advertisement message from doctor record to
	* encourage new patient or customer signup.
	*/
	public static String newPatientSignupAd(DoctorRecord doctorRecord)
	{
		String adMessage="";
		String patientOrCustomer="";
		String patientOrCustomer_Title="";
		String myName="";

		// Chose term according to business owner's title
		if (doctorRecord.calledAs.equals("Dr.")==true){
			patientOrCustomer = "patient";
			patientOrCustomer_Title = "PATIENTS";
		}else{
			patientOrCustomer = "customer";
			patientOrCustomer_Title = "CUSTOMERS";
		}
		if (doctorRecord.calledAs.equals("none")==true){
			myName=doctorRecord.businessName;
		}else{
			myName=doctorRecord.calledAs + " " +doctorRecord.firstName + " "+doctorRecord.lastName + " ";
		}
		// Get mySignupLink from myNetworkData
   		MyNetworkData myNetworkData = MyNetworkData.getInstance();
   		String mySignupLink = myNetworkData.myNewPatientSignupURL+"?officePhone="+doctorRecord.bizPhone;
		if ((doctorRecord.newPatient.equals ("1"))
			|| (doctorRecord.newPatient.equals ("2"))){
			//create an ad that will append to patient email
			adMessage = "\n\n\n WE WELCOME NEW "+patientOrCustomer_Title+"!"+ "\n\n"
						+ "Please copy and forward this message to share our new " +patientOrCustomer+" signup link " +"\n"
						+ "with your trusted friends and family if they wish to get  "+"\n"
						+ "a new "+patientOrCustomer+ " appointment with " + myName + "\n\n"
						+mySignupLink;

		}
		logger.debug("adMessage is ..."+adMessage);
		return adMessage;
	}

	/**    
	 * Open scheduler visitor email
	 * Notify a public visitor. This email contain links that
	 * the visitor can be use to confirm his record, reschedule and cancel the appt.
	 * @param appointmentInfo	The appointment record for the appointment.
	 */
	static public void apptAddOpenEmail (
		AppointmentInfo appointmentInfo,
		MessagesInline messages,
		boolean firstAppt
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();

		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);

   		String myRecordConfirmationLink = PatientHelper.getOpenSchedulerApptUrl(appointmentInfo.patientId, appointmentInfo.apptId, "ConfirmAppt");
		String myApptCancellationLink = PatientHelper.getOpenSchedulerApptUrl(appointmentInfo.patientId, appointmentInfo.apptId, "CancelAppt");
		String myDoNotContactLink = PatientHelper.getOpenSchedulerApptUrl(appointmentInfo.patientId, "0", "DoNotContact");
		
		MyNetworkData myNetworkData = MyNetworkData.getInstance();
		String myExistingApptLink = (myNetworkData.myNewPatientSignupURL+"?officePhone="+doctorRecord.bizPhone).replace("index2", "index");

		String includedLinks ="";
		// include links for open scheduler user depend on if appt is first
		if (firstAppt){
			includedLinks = 
				"\n\n DO NOT WANT TO RECEIVE APPOINTMENT REMINDER? CLICK THIS LINK. \n\n" 
				+ myDoNotContactLink + "\n\n"
				+ "\n\n TO CONFIRM YOUR APPOINTMENT, CLICK THIS LINK. \n\n"				
				+ myRecordConfirmationLink + "\n\n"
				+ "\n\n TO CANCEL YOUR APPOINTMENT, CLICK THIS LINK. \n\n"
				+ myApptCancellationLink + "\n\n";
		}else{
			includedLinks = 
				"\n\n DO NOT WANT TO RECEIVE APPOINTMENT REMINDER? CLICK THIS LINK. \n\n" 
				+ myDoNotContactLink + "\n\n"			
				+ "\n\n TO CANCEL YOUR APPOINTMENT, CLICK THIS LINK.  \n\n"
				+ myApptCancellationLink + "\n\n"
				+ "\n\n TO ADD A NEW APPOINTMENT, CLICK THIS LINK. \n\n"
				+ myExistingApptLink + "\n\n"
				+ "\n\n TO RESCHEDULE, SIMPLY CANCEL YOUR APPOINTMENT AND ADD A NEW ONE. \n\n";
		}
		
		if (patientRecord.email.equals ("") == false){
			String emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptAddByNewVisitor.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandAppointment ("apptAddByNewVisitor.subject", doctorRecord, patientRecord, appointmentInfo),
				TextExpansion.expandAppointment ("apptAddByNewVisitor.body", doctorRecord, patientRecord, appointmentInfo)
				+ includedLinks,
				false
			);
			//messages.addGenericMessage (emailResult);
		}

	}

	/** 
	 * open scheduler visitor email
	 * Notify a doctor and patient about the cancellation of an appointment.
	 * on open scheduler
	 * @param appointmentInfo	The appointment record for the cancelled appointment.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void apptCancelOpenEmail(
		AppointmentInfo appointmentInfo
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		String patientPrefers;
		// email notification to doctor
		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);
		String emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptCancelledByPatient.from", SettingsHelper.EmailSettings),
				doctorRecord.email,
				TextExpansion.expandAppointment ("apptCancelledByPatient.subject", doctorRecord, patientRecord, appointmentInfo),
				TextExpansion.expandAppointment ("apptCancelledByPatient.body", doctorRecord, patientRecord, appointmentInfo),
				false
		);
		// copy email to patient
			emailResult = EmailHelper.send (
				SettingsHelper.readString ("apptCancelledByPatient.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				"COPY: "+ TextExpansion.expandAppointment ("apptCancelledByPatient.subject", doctorRecord, patientRecord, appointmentInfo),
				TextExpansion.expandAppointment ("apptCancelledByPatient.body", doctorRecord, patientRecord, appointmentInfo),
				false
			);


		// save the call log info.
		int callStartTime=TimeHelper.currentEpochSecond ();
		CallRecordLog callRecordLog = new CallRecordLog();
		String logResult= callRecordLog.logCallRecord(
								patientRecord.doctorId,						//doctorId
								"cancelByPatient",							//callType
								String.valueOf(callStartTime),				//callStartTime
								"none",										//patientPhone
								String.valueOf(appointmentInfo.patientId),	//patientId
								"none",										//transferredToPhone
								String.valueOf(appointmentInfo.epochDay),	//apptEpochDay
								String.valueOf(appointmentInfo.rangeStart),	//apptStartSecond
								"none",										//errorEventLog
								"success"									//callResult
							);
		logger.debug("Call record logging for cancel appt by patient is ..."+ logResult+"\n");
	}

	/** 
	 * open scheduler visitor email
	 * Notify a doctor and patient about the cancellation of an unconfirmed appointment.
	 * on open scheduler
	 * @param appointmentInfo	The appointment record for the cancelled appointment.
	 * @param messages		The server messages object to which the success message is added.
	 */
	static public void unconfirmedApptCancelled(
		AppointmentInfo appointmentInfo
	) throws Exception
	{
		String messageFrom, messageSubject, messageBody;
		PatientRecord patientRecord = new PatientRecord ();
		DoctorRecord doctorRecord = new DoctorRecord ();
		String patientPrefers;
		// email notification to patient
		patientRecord.readById (appointmentInfo.patientId);
		doctorRecord.readById (patientRecord.doctorId);
		String emailResult = EmailHelper.send (
				SettingsHelper.readString ("shell.unconfirmedApptCancelled.from", SettingsHelper.EmailSettings),
				patientRecord.email,
				TextExpansion.expandAppointment ("shell.unconfirmedApptCancelled.subject", doctorRecord, patientRecord, appointmentInfo),
				TextExpansion.expandAppointment ("shell.unconfirmedApptCancelled.body", doctorRecord, patientRecord, appointmentInfo),
				false
		);
		// copy email to doctor
			emailResult = EmailHelper.send (
				SettingsHelper.readString ("shell.unconfirmedApptCancelled.from", SettingsHelper.EmailSettings),
				doctorRecord.email,
				"COPY: "+ TextExpansion.expandAppointment ("shell.unconfirmedApptCancelled.subject", doctorRecord, patientRecord, appointmentInfo),
				TextExpansion.expandAppointment ("shell.unconfirmedApptCancelled.body", doctorRecord, patientRecord, appointmentInfo),
				false
			);

		// will not save the call log info. because patient record is also removed
	}	
	
	
/**
* This method send pending patient review decision
* by doctor. Patient has previously used customized
* form to answer doctor's question form
* FIXME: Patient answer need to be associate to
* apptProfile for new patient/customer appt
*/

	static public void patientCustomizedSignup  (
			ServletContext context,
			PatientRecord patientRecord,
			DoctorRecord doctorRecord,
			List data,
			MessagesInline messages
		) throws Exception
		{
			//send email notification to doctor and patient
			String emailResult = "";

			//get mail recipients and doctor data
			String fileName = patientRecord.firstName+"_"+patientRecord.lastName+"_form.pdf";
			String patientName=patientRecord.firstName+" "+patientRecord.lastName;
			String doctorName=doctorRecord.firstName+" "+doctorRecord.lastName;

			logger.debug("fetching appt profile name");
			String newPatientProfileName = DoctorHelper.getDoctorNewPatientProfileName(doctorRecord.doctorId);

			//generate PDF
			logger.debug("generating PDF file for patient answer form");
			ByteArrayOutputStream outputStream=	new ByteArrayOutputStream();
			//If black and white PDF format is prefered by doctor
			boolean blackMode=false;
			if("1".equals(doctorRecord.defaultPDF)){
				blackMode=true;
			}

			PdfExportHelper.generatePatientFormPDF(
							context,
							data,
							patientName,
							newPatientProfileName,
							doctorName,
							blackMode,
							outputStream
			);
			outputStream.flush();
			outputStream.close();

			//mail to doctor using configurable email subject and body
			String addrFrom = SettingsHelper.readString ("toDoctor.newCustomizedSignupReceived.from", SettingsHelper.EmailSettings);
			String subject =TextExpansion.expandDoctorPatient ("toDoctor.newCustomizedSignupReceived.subject", doctorRecord, patientRecord);
			subject=subject.replace("APPT_PROFILE_NAME", newPatientProfileName);
			String body =TextExpansion.expandDoctorPatient ("toDoctor.newCustomizedSignupReceived.body", doctorRecord, patientRecord);
			body=body.replace("APPT_PROFILE_NAME", newPatientProfileName);
			logger.debug("sending e-mail to patient");
			emailResult=EmailHelper.send(
							addrFrom,
							patientRecord.email,
							subject,
							body,
							outputStream.toByteArray(),
							fileName,
							false
			);

			//mail to patient using configurable email subject and body
			addrFrom = SettingsHelper.readString ("toPatient.newCustomizedSignupReceived.from", SettingsHelper.EmailSettings);
			subject =TextExpansion.expandDoctorPatient ("toPatient.newCustomizedSignupReceived.subject", doctorRecord, patientRecord);
			subject=subject.replace("APPT_PROFILE_NAME", newPatientProfileName);
			String myDoNotContactLink = PatientHelper.getOpenSchedulerApptUrl(patientRecord.patientId, "0", "DoNotContact");
			String includedLinks =
				"\n\n DO NOT WANT TO RECEIVE APPOINTMENT REMINDER? CLICK THIS LINK. \n\n" 
				+ myDoNotContactLink + "\n\n";
			body =TextExpansion.expandDoctorPatient ("toPatient.newCustomizedSignupReceived.body", doctorRecord, patientRecord);
			body=body.replace("APPT_PROFILE_NAME", newPatientProfileName) + includedLinks;
			logger.debug("sending e-mail to doctor");
			emailResult=EmailHelper.send(
							addrFrom,
							doctorRecord.email,
							subject,
							body,
							outputStream.toByteArray(),
							fileName,
							false
			);
			messages.addGenericMessage (emailResult);

		}

/**
* This method send email to both patient and doctor 
* a PDF form that contains patient's answer to 
* doctor's questions
*/

	static public void patientSubmitApptForm  (
			ServletContext context,
			PatientRecord patientRecord,
			DoctorRecord doctorRecord,
			ApptProfileRecord apptProfileRecord,
			String apptInfo,
			List data
		) throws Exception
		{
			//send email notification to doctor and patient
			String emailResult = "";

			//get mail recipients and doctor data
			String fileName = patientRecord.firstName+"_"+patientRecord.lastName+"_form.pdf";
			String patientName=patientRecord.firstName+" "+patientRecord.lastName;
			String doctorName=doctorRecord.firstName+" "+doctorRecord.lastName;

			//generate PDF
			logger.debug("generating PDF file for patient answer form");
			ByteArrayOutputStream outputStream=	new ByteArrayOutputStream();
			//If black and white PDF format is prefered by doctor
			boolean blackMode=false;
			if("1".equals(doctorRecord.defaultPDF)){
				blackMode=true;
			}

			PdfExportHelper.generatePatientAppointmentFormPDF(
							context,
							data,
							patientName,
							apptProfileRecord.name,
							doctorName,
							apptInfo,
							blackMode,
							outputStream
			);
			outputStream.flush();
			outputStream.close();

			//mail to doctor using configurable email subject and body
			String addrFrom = SettingsHelper.readString ("patientSubmitApptForm.from", SettingsHelper.EmailSettings);
			String subject = TextExpansion.expandDoctorPatient ("patientSubmitApptForm.subject", doctorRecord, patientRecord);
			String body = TextExpansion.expandDoctorPatient ("patientSubmitApptForm.body", doctorRecord, patientRecord);
			body = body.replace("APPT_PROFILE_NAME", apptProfileRecord.name);			
			body = body.replace("APPT_INFO", apptInfo);	
			logger.debug("sending e-mail to patient");
			emailResult=EmailHelper.send(
							addrFrom,
							patientRecord.email,
							subject,
							body,
							outputStream.toByteArray(),
							fileName,
							false
			);
			//Send same email and PDF to doctor
			logger.debug("sending e-mail to doctor");
			emailResult=EmailHelper.send(
							addrFrom,
							doctorRecord.email,
							subject,
							body,
							outputStream.toByteArray(),
							fileName,
							false
			);

		}


	/**
	 * Notify a patient and doctor about the status of custom signup.
	 *
	 * @param patientRecord	The patient record for the added patient.
	 * @param doctorRecord	The doctor record for the added patient.
	 * @param messages	The server messages object to which the success message is added.
	 * @param replyMessage	The reply message by doctor to be sent.
	 * @param reviewDecision The decision indicator (1= approved, 0=declined).
	 */
	static public void newCustomPatientReview (
		ServletContext context,
		PatientRecord patientRecord,
		DoctorRecord doctorRecord,
		List data,
		MessagesInline messages,
		String replyMessage,
		String reviewDecision
	) throws Exception
	{
		String patientName=patientRecord.firstName+" "+patientRecord.lastName;
		String fileName = patientRecord.firstName+"_"+patientRecord.lastName+"_form.pdf";
		String doctorName=doctorRecord.firstName+" "+doctorRecord.lastName;

		String newPatientProfileName = DoctorHelper.getDoctorNewPatientProfileName(doctorRecord.doctorId);

		//generate PDF using doctor preferred PDF format
		logger.debug("generating PDF file");
		boolean blackMode=false;
		if("1".equals(doctorRecord.defaultPDF)){
			blackMode=true;
		}

		ByteArrayOutputStream outputStream=	new ByteArrayOutputStream();
		PdfExportHelper.generatePatientFormPDF(
				context,
				data,
				patientName,
				newPatientProfileName,
				doctorName,
				blackMode,
				outputStream
		);

		outputStream.flush();
		outputStream.close();

		String emailResult;
		//send email notification to doctor
		String emailSubject, emailBody;
		if (reviewDecision.equals("0")){
			//email for decline signup
			emailSubject = TextExpansion.expandDoctorPatient ("toDoctor.newCustomizedSignupDeclined.subject", doctorRecord, patientRecord);
			emailBody = TextExpansion.expandDoctorPatient ("toDoctor.newCustomizedSignupDeclined.body", doctorRecord, patientRecord)
							+ "\n"+ replyMessage;
			emailBody=emailBody.replace("APPT_PROFILE_NAME", newPatientProfileName);
			emailResult = EmailHelper.send (
				SettingsHelper.readString ("toDoctor.newCustomizedSignupDeclined.from", SettingsHelper.EmailSettings),
				doctorRecord.email,
				emailSubject,
				emailBody,
				outputStream.toByteArray(),
				fileName,
				false
			);
		}else{
			//email to doctor for approval
			emailSubject = TextExpansion.expandDoctorPatient ("toDoctor.newCustomizedSignupApproved.subject", doctorRecord, patientRecord);
			emailBody = TextExpansion.expandDoctorPatient ("toDoctor.newCustomizedSignupApproved.body", doctorRecord, patientRecord)
							+ "\n"+ replyMessage;
			emailBody=emailBody.replace("APPT_PROFILE_NAME", newPatientProfileName);				
			emailResult = EmailHelper.send (
				SettingsHelper.readString ("toDoctor.newCustomizedSignupApproved.from", SettingsHelper.EmailSettings),
				doctorRecord.email,
				emailSubject,
				emailBody,
				outputStream.toByteArray(),
				fileName,
				false
			);
		}
		messages.addGenericMessage (emailResult);

		//send email notification to patient
		if (reviewDecision.equals("0")){
			// about signup decline
			emailSubject = TextExpansion.expandDoctor ("toPatient.newCustomizedSignupDeclined.subject", doctorRecord);
			emailBody = TextExpansion.expandDoctorPatient ("toPatient.newCustomizedSignupDeclined.body", doctorRecord, patientRecord)
							+ "\n"+ replyMessage;
			emailBody=emailBody.replace("APPT_PROFILE_NAME", newPatientProfileName);
			emailResult = EmailHelper.send (
					SettingsHelper.readString ("toPatient.newCustomizedSignupDeclined.from", SettingsHelper.EmailSettings),
					patientRecord.email,
					emailSubject,
					emailBody,
					outputStream.toByteArray(),
					fileName,
					false
				);
		}else{
			// about signup approval
			emailSubject = TextExpansion.expandDoctor ("toPatient.newCustomizedSignupApproved.subject", doctorRecord);
			emailBody = TextExpansion.expandDoctorPatient ("toPatient.newCustomizedSignupApproved.body", doctorRecord, patientRecord)
							+ "\n"+ replyMessage;

			emailResult = EmailHelper.send (
					SettingsHelper.readString ("toPatient.newCustomizedSignupApproved.from", SettingsHelper.EmailSettings),
					patientRecord.email,
					emailSubject,
					emailBody,
					outputStream.toByteArray(),
					fileName,
					false
				);
		}
		messages.addGenericMessage (emailResult);

	}
	
	/*
	 * Email report to a doctor with call record .
	 * import summary.
	 * @param doctorRecord	The doctor record.
	 * @param importFromFile The excel/csv file name.
	 * @param importSummary  summary of the import (multi-lines strings).
	 */
	static public void excelImportSummary (
		DoctorRecord doctorRecord,
		String importFileName,
		String successCount,
		String importSummary
	) throws Exception
	{
		String emailResult = EmailHelper.send (
			SettingsHelper.readString ("doctorUploadExcelResult.from", SettingsHelper.EmailSettings),
			doctorRecord.email,
			TextExpansion.expandDoctor ("doctorUploadExcelResult.subject", doctorRecord)
			.replace("EXCEL_FILE_NAME", importFileName)
			.replace("SUCCESS_COUNT", successCount),
			TextExpansion.expandDoctor ("doctorUploadExcelResult.body", doctorRecord)
			.replace("EXCEL_FILE_NAME", importFileName)
			.replace("SUCCESS_COUNT", successCount)
			.replace("EXCEL_IMPORT_SUMMARY",importSummary),
			false
		);
		// send a email copy to admin
		EmailHelper.send (
			SettingsHelper.readString ("doctorUploadExcelResult.from", SettingsHelper.EmailSettings),
			SettingsHelper.readString ("copy.admin.toAddress", SettingsHelper.EmailSettings),
			TextExpansion.expandDoctor ("doctorUploadExcelResult.subject", doctorRecord)
			.replace("EXCEL_FILE_NAME", importFileName)
			.replace("SUCCESS_COUNT", successCount),
			TextExpansion.expandDoctor ("doctorUploadExcelResult.body", doctorRecord)
			.replace("EXCEL_FILE_NAME", importFileName)
			.replace("SUCCESS_COUNT", successCount)
			.replace("EXCEL_IMPORT_SUMMARY",importSummary),
			false
		);
	}	

}
