package com.niu.models;

import android.os.Parcel;
import android.os.Parcelable;

public class BusStop implements Parcelable{
	private String stopCode;
	private String description;
	private String road;
	private double lat;
	private double lng;
	private double distance;
	private Buses buses;
	
	public BusStop() {}
	public BusStop(Parcel source) {
		stopCode = source.readString();
		description = source.readString();
		road = source.readString();
		lat = source.readDouble();
		lng = source.readDouble();
		distance = source.readDouble();
		this.buses = source.readParcelable(getClass().getClassLoader());
	}

	public String getStopCode() {
		return stopCode;
	}
	public void setStopCode(String stopCode) {
		this.stopCode = stopCode;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getRoad() {
		return road;
	}
	public void setRoad(String road) {
		this.road = road;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public Buses getBuses() {
		return buses;
	}
	public void setBuses(Buses buses) {
		this.buses = buses;
	}
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(stopCode);
		dest.writeString(description);
		dest.writeString(road);
		dest.writeDouble(lat);
		dest.writeDouble(lng);
		dest.writeDouble(distance);
		dest.writeParcelable(buses, flags);
	}
	
	public static final Parcelable.Creator<BusStop> CREATOR = new Parcelable.Creator<BusStop>() {

		@Override
		public BusStop createFromParcel(Parcel source) {
			return new BusStop(source);
		}

		@Override
		public BusStop[] newArray(int size) {
			return new BusStop[size];
		}
	};

}
