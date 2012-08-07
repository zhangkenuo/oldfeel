package org.dlion.footsince;

public class FootInfo {
	public int _id;
	public int latitude;
	public int longitude;
	public String footName;
	public String date;

	public FootInfo() {
		super();
	}

	public FootInfo(int _id, int latitude, int longitude, String footName,
			String date) {
		super();
		this._id = _id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.footName = footName;
		this.date = date;
	}

}
