package org.dlion.schedule;

public class ScheduleInfo {
	int _id;
	int enable;
	int weekDay;
	String lessonTime;
	String lessonName;
	String ringTime;
	String ringName;
	String classRoom;
	String teacherName;

	public ScheduleInfo(int _id, int enable, int weekDay, String lessonTime,
			String lessonName, String ringTime, String ringName,
			String classRoom, String teacherName) {
		super();
		this._id = _id;
		this.enable = enable;
		this.weekDay = weekDay;
		this.lessonTime = lessonTime;
		this.lessonName = lessonName;
		this.ringTime = ringTime;
		this.ringName = ringName;
		this.classRoom = classRoom;
		this.teacherName = teacherName;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public int getEnable() {
		return enable;
	}

	public void setEnable(int enable) {
		this.enable = enable;
	}

	public int getWeekDay() {
		return weekDay;
	}

	public void setWeekDay(int weekDay) {
		this.weekDay = weekDay;
	}

	public String getLessonTime() {
		return lessonTime;
	}

	public void setLessonTime(String lessonTime) {
		this.lessonTime = lessonTime;
	}

	public String getLessonName() {
		return lessonName;
	}

	public void setLessonName(String lessonName) {
		this.lessonName = lessonName;
	}

	public String getRingTime() {
		return ringTime;
	}

	public void setRingTime(String ringTime) {
		this.ringTime = ringTime;
	}

	public String getRingName() {
		return ringName;
	}

	public void setRingName(String ringName) {
		this.ringName = ringName;
	}

	public String getClassRoom() {
		return classRoom;
	}

	public void setClassRoom(String classRoom) {
		this.classRoom = classRoom;
	}

	public String getTeacherName() {
		return teacherName;
	}

	public void setTeacherName(String teacherName) {
		this.teacherName = teacherName;
	}
}
