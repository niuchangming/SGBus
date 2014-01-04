package com.niu.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Route implements Parcelable{
	private String name;
	private String stop;
	
	public Route() {}
	public Route(Parcel source) {
		name = source.readString();
		stop = source.readString();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStop() {
		return stop;
	}
	public void setStop(String stop) {
		this.stop = stop;
	}
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(stop);
	}
	
	public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {

		@Override
		public Route createFromParcel(Parcel source) {
			return new Route(source);
		}

		@Override
		public Route[] newArray(int size) {
			return new Route[size];
		}
	};

}
