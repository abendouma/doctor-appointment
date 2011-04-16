package net.angelspeech.service.dto.sync;

/**
 * @author Quang
 *
 */
public class LocationSyncInfo {
	public int apptLocationId;
	public String businessName;
	public String street;
	public String city;
	public String state;
	public String zip;
	public String bizPhone;
	public String mapURL;
	
	
	public LocationSyncInfo(int apptLocationId, String businessName,
			String street, String city, String state, String zip,
			String bizPhone, String mapURL) {
		this.apptLocationId = apptLocationId;
		this.businessName = businessName;
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.bizPhone = bizPhone;
		this.mapURL = mapURL;
	}
	
	public LocationSyncInfo() {
	}
}
