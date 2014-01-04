package com.niu.sgbus;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.niu.models.BusStop;
import com.niu.models.BusStops;
import com.niu.models.RouteStop;
import com.niu.sgbus.NearbyListFragment.UpdateMarkerListener;
import com.niu.tools.LocationHelper;

public class BusMapFragment extends SupportMapFragment implements UpdateMarkerListener, OnMapClickListener{
	private final String TAG = "BusMapFragment";
	private GoogleMap map = null;
	private BusStops stops = null;
	public Button rightMapMenuBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(getActivity() instanceof MainActivity){
			rightMapMenuBtn = (Button) ((MainActivity)this.getActivity()).rightMenu.findViewById(R.id.rightmap_menu_btn);
		}else{
			rightMapMenuBtn = (Button) ((ServiceRouteAvtivity)this.getActivity()).rightMenu.findViewById(R.id.rightmap_menu_btn);
		}
		
		rightMapMenuBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				popupWindows();
			}});
		
		map = this.getMap();
		if(map != null){
			UiSettings uiSetting = map.getUiSettings();
			uiSetting.setMyLocationButtonEnabled(false);
			addClickListener2map();
		}
	}
	
	private void initMapForStop(){
		stops = ((NearbyListFragment)MainActivity.fragments.get(0)).busStops;
		((MainActivity)getActivity()).canOpenMap = true;
		if(map != null){
			getMyLocation();
			for(BusStop stop : stops){
				getStopLocation(stop);
			}
		}
	}
	
	private void addClickListener2map() {
		map.setInfoWindowAdapter(new InfoWindowAdapter() {

			@Override
			public View getInfoContents(Marker marker) {
				return null;
			}

			@Override
			public View getInfoWindow(Marker marker) {
				if(marker != null){
					Intent streetView = new Intent(android.content.Intent.ACTION_VIEW, 
							Uri.parse("google.streetview:cbll="+ marker.getPosition().latitude
									+","+marker.getPosition().longitude+"&cbp=1,99.56,,1,-5.27&mz=21"));
					startActivity(streetView);
				}
				return null;
			}});
	}
	
	private DisplayMetrics metrics;
	private DisplayMetrics getScreenDisplay(){
		if(metrics == null)
			metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics;
	}

	public void initMapForRoute(List<RouteStop> routeStops){
		if(map != null){
			getMyLocation();
			for(RouteStop stop : routeStops){
				getRouteStopLocation(stop);
			}
		}
	}
	
	public void moveToNearestRouteStop(RouteStop stop){
		if(stop != null){
			animateCameraTo(Double.parseDouble(stop.getLatitude()), Double.parseDouble(stop.getLongitude()));
		}
	}
	
	public void animateCameraTo(final double lat, final double lng){
	    CameraPosition camPosition = map.getCameraPosition();
	    if (!((Math.floor(camPosition.target.latitude * 100) / 100) == (Math.floor(lat * 100) / 100) && (Math.floor(camPosition.target.longitude * 100) / 100) == (Math.floor(lng * 100) / 100))){
	        map.getUiSettings().setScrollGesturesEnabled(false);
	        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)), new CancelableCallback(){

	            @Override
	            public void onFinish(){
	                map.getUiSettings().setScrollGesturesEnabled(true);

	            }

	            @Override
	            public void onCancel(){
	                map.getUiSettings().setAllGesturesEnabled(true);

	            }
	        });
	    }
	}

	private void getMyLocation(){
		if(LocationHelper.location == null) return;
		LatLng currentLoc = new LatLng(LocationHelper.location.getLatitude(), LocationHelper.location.getLongitude());
		if(LocationHelper.getInstance(getActivity()).getAddress() != null){
			map.addMarker(new MarkerOptions()
		     .position(currentLoc)
		     .title(LocationHelper.getInstance(getActivity()).getAddress().getLocality() == null ? 
		    		 LocationHelper.getInstance(getActivity()).getAddress().getCountryName() :
		    			 LocationHelper.getInstance(getActivity()).getAddress().getLocality())
		     .snippet(LocationHelper.getInstance(getActivity()).getAddress().getAddressLine(0))
		     .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_loc_pin)));
		}else{
			map.addMarker(new MarkerOptions()
		     .position(currentLoc)
		     .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_loc_pin)));
		}
		
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 16));
	}
	
	private void getStopLocation(BusStop stop){
		map.addMarker(new MarkerOptions()
		.position(new LatLng(stop.getLat(), stop.getLng()))
		.title(stop.getStopCode())
		.snippet(stop.getDescription())
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
	}
	
	private void getRouteStopLocation(RouteStop stop){
		map.addMarker(new MarkerOptions()
		.position(new LatLng(Double.parseDouble(stop.getLatitude()), Double.parseDouble(stop.getLongitude())))
		.title(stop.getStopCode())
		.snippet(stop.getStopDesc())
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
	}
	
	private PopupWindow popupMenu;
	private void popupWindows(){
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View menuView = inflater.inflate(R.layout.popupmenu_layout, null);
		Button normalBtn = (Button) menuView.findViewById(R.id.map_normal_type);
		Button hybridBtn = (Button) menuView.findViewById(R.id.map_hybrid_type);
		Button terrainBtn = (Button) menuView.findViewById(R.id.map_terrain_type);
		Button satelliteBtn = (Button) menuView.findViewById(R.id.map_satellite_type);
		normalBtn.setOnClickListener(NormalBtnListener);
		hybridBtn.setOnClickListener(HybridBtnListener);
		terrainBtn.setOnClickListener(TerrainBtnListener);
		satelliteBtn.setOnClickListener(SatelliteBtnListener);
		
		getScreenDisplay();
		int width = getScreenDisplay().widthPixels;
		int height = getScreenDisplay().heightPixels;
		popupMenu = new PopupWindow(menuView,  width / 3, height / 4);
		popupMenu.setBackgroundDrawable(new BitmapDrawable());
		popupMenu.setOutsideTouchable(true);
		popupMenu.showAsDropDown(rightMapMenuBtn);
	}
	
	private OnClickListener NormalBtnListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			popupMenu.dismiss();
		}
	};
	private OnClickListener HybridBtnListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			popupMenu.dismiss();
		}
	};
	private OnClickListener TerrainBtnListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			popupMenu.dismiss();
		}
	};
	private OnClickListener SatelliteBtnListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			popupMenu.dismiss();
		}
	};
	
	@Override
	public void updateMarker() {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				initMapForStop();
			}
		});
	}

	@Override
	public void onMapClick(LatLng point) {
		
	}
	
}
