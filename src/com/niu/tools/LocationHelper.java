package com.niu.tools;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.niu.sgbus.SGBusDialog;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

public class LocationHelper{
	private final String TAG = "LocationHelper";
	public static Location location;
	private Address address;
	private LocationManager locationManager;
	private boolean isNetworkEnabled;
	private boolean isGPSEnabled;
	private String providerUsed;
	private static LocationHelper instance = null;
	private LocationChangedListener listener;
	private Context context;
	
	public synchronized static LocationHelper getInstance(Context context){
		if(instance == null)
			instance = new LocationHelper(context);
		return instance;
	}
	
	private LocationListener locationListener = new LocationListener(){

		@Override
		public void onLocationChanged(Location location) {
			Log.v(TAG, "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude() + ", Listener: " + listener);
			LocationHelper.location = location;
			if(listener != null && LocationHelper.location != null)
				listener.changeCompleted();
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.v(TAG, "Provider disabled");
			Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			context.startActivity(intent);
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.v(TAG, "Provider enabled");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.v(TAG, "Status chanhed");
		}};
		
	public LocationHelper(Context context) {
		this.context = context;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		init();
	}

	public void init(){
		isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		if(isNetworkEnabled){
			providerUsed = LocationManager.NETWORK_PROVIDER;
		}else if(isGPSEnabled){
			providerUsed = LocationManager.GPS_PROVIDER;
		}else{
			providerUsed = getBestProvider();
		}
		
		if(providerUsed == null){
			showDialog();
			return;
		}
		
		updateLocation();
//		location = locationManager.getLastKnownLocation(providerUsed);
		
		Log.v(TAG, "Location provider ---> " + providerUsed);
		address();
	}
	
	private void showDialog() {
        FragmentTransaction ft = ((FragmentActivity)context).getFragmentManager().beginTransaction();
        SGBusDialog dialogFrag = SGBusDialog.getInstance("Location Fail", 
        		"Failed to get your current location.\nPlease, make sure your GPS on.");
        dialogFrag.show(ft, "dialog");
    }
	
	public void address(){
		Geocoder geo = new Geocoder(context, Locale.getDefault());
		if(location != null){
			List<Address> addresses;
			try {
				addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				if (addresses != null && addresses.size() > 0) {
					if(addresses.get(0) != null){
						this.address = addresses.get(0);
					}
		        }
			} catch (IOException e) {
				Log.v(TAG, "Address error ---> " + e.toString());
			}
		}
	}
	
	public Address getAddress(){
		return this.address;
	}
	
	public LocationManager getLocationManager(){
		return this.locationManager;
	}
	
	public LocationListener getLocationListener(){
		return this.locationListener;
	}
	
	public int getDistanceByGivenCoord(double latitude, double longtitude){
		Location dest = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		dest.setLatitude(latitude);
		dest.setLongitude(longtitude);
		location.distanceTo(dest);
		return (int)location.distanceTo(dest);
	}
	
	public String getBestProvider(){ 
	    Criteria criteria = new Criteria(); 
	    criteria.setPowerRequirement(Criteria.NO_REQUIREMENT); 
	    criteria.setAccuracy(Criteria.NO_REQUIREMENT); 
	    String bestProvider = locationManager.getBestProvider(criteria, true); 
	    return bestProvider; 
	}
	
	public void updateLocation(){
		locationManager.requestLocationUpdates(providerUsed, 1000, 0, locationListener);
	}
	
	public void removeLocationListener(){
		locationManager.removeUpdates(this.locationListener);
		Log.v(TAG, "Remove location listener...");
	}
	
	public void clear(){
		if(this.locationListener != null){
			removeLocationListener();
		}
		if(location != null){
			location = null;
		}
		if(instance != null){
			instance = null;
		}
	}
	
	public LocationChangedListener getListener() {
		return listener;
	}

	public void setListener(LocationChangedListener listener) {
		this.listener = listener;
	}

	public interface LocationChangedListener{
		void changeCompleted();
	}
}
