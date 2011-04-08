package net.angelspeech.action.doctor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.angelspeech.database.DoctorRecord;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.MappingDispatchAction;

/**
 * This class implements the doctor page used to add a new appointment.
 */
public class TestAction extends MappingDispatchAction
{
//	static private Logger logger = Logger.getLogger(TestAction.class);

	/**
	 * Takes the user input from the "Add appointment" doctor page, checks the user input and
	 * if it is correct adds a new appointment.
	 *
	 * @return		Return an ActionForward instance
	 * @param mapping	The ActionMapping used to select this instance
	 * @param form		The optional ActionForm bean for this request
	 * @param request	The HTTP request we are processing
	 * @param response	The HTTP response we are creating
	 */
	public ActionForward execute (
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response
	) throws java.lang.Exception
	{
		String doctorId = "1";
		
		DoctorRecord doctor = new DoctorRecord();
		
		doctor.readById(doctorId);
		
		System.out.println(doctor.email);
		
		return (mapping.findForward ("success"));
	}

}
