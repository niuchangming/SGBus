package com.niu.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ComingTime implements Parcelable{
	private String serviceNo;
	private String nextBus;
	private String subSequentbus;
	
	public ComingTime() {}
	public ComingTime(Parcel source) {
		serviceNo = source.readString();
		nextBus = source.readString();
		subSequentbus = source.readString();
	}

	public String getServiceNo() {
		return serviceNo;
	}
	public void setServiceNo(String serviceNo) {
		this.serviceNo = serviceNo;
	}
	public String getNextBus() {
		return nextBus;
	}
	public void setNextBus(String nextBus) {
		this.nextBus = nextBus;
	}
	public String getSubSequentbus() {
		return subSequentbus;
	}
	public void setSubSequentbus(String subSequentbus) {
		this.subSequentbus = subSequentbus;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(serviceNo);
		dest.writeString(nextBus);
		dest.writeString(subSequentbus);
	}
	
	public static final Parcelable.Creator<ComingTime> CREATOR = new Parcelable.Creator<ComingTime>() {

		@Override
		public ComingTime createFromParcel(Parcel source) {
			return new ComingTime(source);
		}

		@Override
		public ComingTime[] newArray(int size) {
			return new ComingTime[size];
		}
	};

}
