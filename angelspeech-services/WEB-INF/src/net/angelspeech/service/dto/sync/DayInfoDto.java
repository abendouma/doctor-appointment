package net.angelspeech.service.dto.sync;

/**
 * @author Quang
 *
 */
public class DayInfoDto {
	public int slotSize;
	public Object[] daysFree;
	public Object[] daysAppt;
	public Object[] daysEmrg;
	
	public DayInfoDto() {
	}

	public DayInfoDto(int slotSize, Object[] daysFree, Object[] daysAppt,
			Object[] daysEmrg) {
		this.slotSize = slotSize;
		this.daysFree = daysFree;
		this.daysAppt = daysAppt;
		this.daysEmrg = daysEmrg;
	}
	
	
}
