package com.okeefm.geoscanner;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;

public class Barcode implements Parcelable{
	private String barcode;
	private Location location;
	private Time dateTime;
	private String label;
	
	public Barcode(String bc, Location loc, Time gc, String label) {
		barcode = bc;
		location = loc;
		dateTime = gc;
		label = this.label;
	}
	
	public Barcode() {
		barcode = "";
		label = "";
	}
	
	public String getBarcode() {
		return barcode;
	}
	
	public void setBarcode(String bc) {
		barcode = bc;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location loc) {
		location = loc;
	}
	
	public Time getDateTime() {
		return dateTime;
	}
	
	public void setDateTime(Time gc) {
		dateTime = gc;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		label = this.label;
	}

	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<Barcode> CREATOR
		= new Parcelable.Creator<Barcode>() {
		public Barcode createFromParcel (Parcel in) {
			Barcode barcode = new Barcode();
			barcode.setBarcode(in.readString());
			Time time = new Time();
			time.parse(in.readString());
			barcode.setDateTime(time);
			barcode.setLabel(in.readString());
			barcode.setLocation(Location.CREATOR.createFromParcel(in));
			return barcode;
		}

		public Barcode[] newArray(int arg0) {
			Barcode [] codes = new Barcode[arg0];
			for (int i = 0; i < arg0; i++) {
				codes[i] = new Barcode();
			}
			return codes;
		}

	};

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(barcode);
		dest.writeString(dateTime.toString());
		dest.writeString(label);
		location.writeToParcel(dest, flags);
	}

}
