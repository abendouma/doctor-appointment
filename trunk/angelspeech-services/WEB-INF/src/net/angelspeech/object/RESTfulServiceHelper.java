package net.angelspeech.object;

import java.security.SecureRandom;
import java.util.Random;
import java.math.BigInteger;
import net.angelspeech.database.DoctorRecord;

import org.apache.log4j.Logger;


/**
 * This class contains helper functions for RESTful web service
 */
 
public class RESTfulServiceHelper
{
	static private Logger logger = Logger.getLogger(RESTfulServiceHelper.class);	

  	/**
  	* This method returns a secure 32 digit key to authenticate the REST service client
  	* using SHA1PRNG algorithm
  	*/
  	static public String create32DigitKey() throws Exception
  	{
	  	
    	Random r = SecureRandom.getInstance("SHA1PRNG");
		
    	//generate a big secure random number and use 32 digits as key
	  	String number = new BigInteger(130, r)+"";
	  	String myKey = number.substring(0,32);
	  	logger.info("New myKey is ..."+ myKey +" and the length is .." + myKey.length() );
    	return myKey;
  	}
  	
    /**
  	* This method validate the REST clinet key against the one
  	* on doctor's record, return a boolean result
  	*/	
  	
  	static public boolean isRESTKeyValid(String doctorId, String key) throws Exception
  	{

	  	//Received key need to be 32 digits
	  	boolean isKey32Digit = (key.length() == 32);
	  	if (isKey32Digit == false){
	  		logger.error("Rejected REST client key "+ key + " for  doctorId ..."+ doctorId + " due to length to be "+ key.length());
  			return false;
	  	}
	  	//Load doctor record from db, the key on record must be 32 digit too
  		DoctorRecord doctorRecord = new DoctorRecord();
		doctorRecord.readById(doctorId);
		isKey32Digit = (doctorRecord.RESTkey.length() == 32);
		if (isKey32Digit == false){
	  		logger.error("Rejected REST client key "+ key + " for  doctorId ..."+ doctorId + " due to key length on record to be "+ doctorRecord.RESTkey.length());
  			return false;
	  	}
		if ((doctorRecord.RESTkey).equals(key)){
			logger.info("Passed REST key validation for  doctorId ..."+ doctorId );
			return true;
		}else{
			logger.error("Rejected REST client key "+ key + " for  doctorId ..."+ doctorId + " due to mismatch");
			return false;
		}

  	}
  	
  	/**
	* This method update the RESTkey on a doctor record
	* To remove the key on record, use key = ""
	*/
	public static void updateRESTkey(String doctorId, String key) throws Exception{

		DoctorRecord doctor=new DoctorRecord();
		doctor.readById(doctorId);
		doctor.RESTkey=key;
		doctor.writeById(doctorId);					
		return;
	} 

}
