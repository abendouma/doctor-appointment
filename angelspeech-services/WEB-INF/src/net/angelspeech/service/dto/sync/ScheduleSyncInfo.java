package net.angelspeech.service.dto.sync;

import java.util.List;

/**
 * @author Quang
 *
 */
public class ScheduleSyncInfo {
	public int doctorId;
	public int dayStart;
	public List<DayInfoDto> dayInfos;
	
	public ScheduleSyncInfo() {
	}

	public ScheduleSyncInfo(int doctorId, int dayStart,
			List<DayInfoDto> dayInfos) {
		this.doctorId = doctorId;
		this.dayStart = dayStart;
		this.dayInfos = dayInfos;
	}
}
