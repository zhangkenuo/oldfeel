package org.dlion.schedule;

public class ScheduleInfo {
	public int _id;
	public int enable;
	public int weekDay;
	public String scheduleTime;
	public String scheduleName;
	public String ringTime;
	public String ringName;
	public String scheduleRemark;
	public String scheduleContent;

	public ScheduleInfo(int _id, int enable, int weekDay, String scheduleTime,
			String scheduleName, String ringTime, String ringName,
			String scheduleRemark, String scheduleContent) {
		super();
		this._id = _id;
		this.enable = enable;
		this.weekDay = weekDay;
		this.scheduleTime = scheduleTime;
		this.scheduleName = scheduleName;
		this.ringTime = ringTime;
		this.ringName = ringName;
		this.scheduleRemark = scheduleRemark;
		this.scheduleContent = scheduleContent;
	}
}
