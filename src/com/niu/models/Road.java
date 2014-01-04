package com.niu.models;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Road implements Parcelable{
	private String name;
	private List<Route> routes;
	
	public Road(){}
	public Road(Parcel source){
		name = source.readString();
 		this.routes = new ArrayList<Route>();
		source.readList(routes, getClass().getClassLoader());
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Route> getRoutes() {
		return routes;
	}
	public void setRoutes(List<Route> routes) {
		this.routes = routes;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeList(routes);
	}
	
	public static final Parcelable.Creator<Road> CREATOR = new Parcelable.Creator<Road>() {

		@Override
		public Road createFromParcel(Parcel source) {
			return new Road(source);
		}

		@Override
		public Road[] newArray(int size) {
			return new Road[size];
		}
	};
	
}























