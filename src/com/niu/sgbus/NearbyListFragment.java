package com.niu.sgbus;

import java.util.ArrayList;
import java.util.List;

import com.niu.models.BusStop;
import com.niu.models.BusStops;
import com.niu.models.Buses;
import com.niu.models.ComingTime;
import com.niu.network.BaseRequestListener;
import com.niu.network.RestfulCall;
import com.niu.tools.Constants;
import com.niu.tools.LocationHelper;
import com.niu.tools.LocationHelper.LocationChangedListener;
import com.niu.tools.Utils;
import com.niu.views.ExpandableListItemClickListener;
import com.niu.views.SlideExpandableListAdapter;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

public class NearbyListFragment extends Fragment implements ExpandableListItemClickListener, LocationChangedListener{
	private final String TAG = "NearbyListFragment";
	public BusStops busStops;
	private ListView busListView;
	private NearByBusAdapter adapter;
	private LayoutInflater inflater;
	public UpdateMarkerListener updateMarkerListener;
	private ProgressDialog proDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState == null){
			LocationHelper.getInstance(getActivity());
			startProgressDialog();
			requestNearbyBusStop();
		}else{
			busStops = (BusStops) savedInstanceState.get("bus_stop");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.list_fragment_layout, container, false);
		this.inflater = inflater;
		adapter = new NearByBusAdapter();
		SlideExpandableListAdapter expandableAdapter = new SlideExpandableListAdapter(adapter, 0, R.id.expandable);
		expandableAdapter.setItemClickListener(this);
		busListView = (ListView) rootView.findViewById(R.id.bus_fragment_listview);
		busListView.setCacheColorHint(R.color.white);
		busListView.setAdapter(expandableAdapter);
		
		return rootView;
	}

	private void init(){
		busStops = new BusStops();
	}

	private void requestNearbyBusStop(){
		Bundle params = new Bundle();
		if(LocationHelper.location == null){
			Log.v(TAG, "Location is null at the moment...");
			LocationHelper.getInstance(getActivity()).setListener(this);
		}else{
			params.putString("lon", LocationHelper.location.getLongitude() + "");
			params.putString("lat", LocationHelper.location.getLatitude() + "");
			params.putString("radius", ((MainActivity) getActivity()).radius + "");
			params.putString("iriskey", Utils.getRandomIrisKey());
			RestfulCall.getInstance().request(Constants.APIBASE_URL + Constants.PATH_NEARBY + "?", params, "GET", new BaseRequestListener() {
				
				@Override
				public void onComplete(final String response, Object state) {
					if(getActivity() == null) return;
					getActivity().runOnUiThread(new Runnable(){

						@Override
						public void run() {
							busStops.parseXMLData(response);
							if(busStops != null && busStops.size() > 0){
								adapter.notifyDataSetChanged();
								searchEditTracking();
								proDialog.dismiss();
							}else if(response.contains("Access deny")){
								try {
									Thread.sleep(1000);
									requestNearbyBusStop();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}});
					
					if(updateMarkerListener != null){
						updateMarkerListener.updateMarker();
					}
				}
			});
		}
	}
	
	private int textLength = 0;
	private List<BusStop> copyStops;
	private void searchEditTracking(){
		copyStops = new ArrayList<BusStop>(busStops);
		((MainActivity)getActivity()).searchTextView.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(NearbyListFragment.this.getUserVisibleHint()){
					textLength = ((MainActivity)getActivity()).searchTextView.getText().length();
					busStops.clear();
					for(BusStop stop : copyStops){
						if(textLength <= stop.getStopCode().length()){
							if(((MainActivity)getActivity()).searchTextView.getText().toString()
									.equalsIgnoreCase((String)stop.getStopCode().subSequence(0, textLength))){
								busStops.add(stop);
							}
						}
					}
					adapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
	}
	
	private Buses buses;
	public void requestBusService(final int position){
		Bundle params = new Bundle();
		params.putString("stop", busStops.get(position).getStopCode());
		params.putString("iriskey", Utils.getRandomIrisKey());
		RestfulCall.getInstance().request(Constants.APIBASE_URL + Constants.PATH_SERVICES_BY_STOP + "?", params, "GET", new BaseRequestListener() {
			
			@Override
			public void onComplete(String response, Object state) {
				buses = new Buses();
				buses.parseXMLData(response);
				if(buses.size() > 0){
					busStops.get(position).setBuses(buses);
					if(getActivity() == null) return;
					getActivity().runOnUiThread(new Runnable(){

						@Override
						public void run() {
							getBuServiceView(position);
						}});
				}else if(response.contains("Access deny")){
					Log.v(TAG, "Access deny ...");
					try {
						Thread.sleep(1000);
						requestBusService(position);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private int columnCount = 3;
	private void getBuServiceView(final int position){
		LinearLayout container = (LinearLayout) busListView.getChildAt(position - busListView.getFirstVisiblePosition());
		LinearLayout view = (LinearLayout) container.findViewById(R.id.nearby_buservice_container);
		
		if(view != null){
			rotation.cancel();
			if(view != null && view.findViewById(position) != null && view.findViewById(position).getAnimation() != null)
				view.findViewById(position).setAnimation(null);
			view.removeAllViews();
			getBuServiceView(position, view);
		}
	}
	
	private void getBuServiceView(final int position, final LinearLayout container){
		int busCount = busStops.get(position).getBuses().size();
		int index = 0;
		while(index < busCount){
			if(busCount - index <= columnCount){
				container.addView(getRow(position, index, busCount));
			}else{
				container.addView(getRow(position, index, index + columnCount));
			}
			index += columnCount;
		}
		getNextBusTime(0, position);
	}
	
	/**
	 * @param position ---> list view position
	 * @param index ---> bus service in the bus stop 
	 */
	private void getNextBusTime(final int index, final int position){
		Bundle params = new Bundle();
		params.putString("busstop", busStops.get(position).getStopCode());
		params.putString("svc", busStops.get(position).getBuses().get(index).getServiceNo());
		params.putString("iriskey", Utils.getRandomIrisKey());
		RestfulCall.getInstance().request(Constants.APIBASE_URL + Constants.PATH_NEXTBUS + "?", params, "GET", new BaseRequestListener() {
			
			@Override
			public void onComplete(String response, Object state) {
				busStops.get(position).getBuses().get(index).parseXMLData(response);
				if(getActivity() == null) return;
				if(!response.contains("Access deny")){
					getActivity().runOnUiThread(new Runnable(){
						@Override
						public void run() {
							updateComingTimeView(position, index);
						}
					});
				}else{
					try {
						Thread.sleep(1000);
						getNextBusTime(index, position);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private void updateComingTimeView(final int position, final int index){
		LinearLayout view = (LinearLayout) busListView.getChildAt(position - busListView.getFirstVisiblePosition());
		if(view == null) return;
		TextView nextBusTextView = (TextView) view.findViewById(R.id.nearby_bus_first_time);
		TextView subBusTextView = (TextView) view.findViewById(R.id.nearby_bus_sec_time);
		TextView busNoTextView = (TextView) view.findViewById(R.id.nearby_bus_no_textview);
		busNoTextView.setText(Utils.removePreZero(busStops.get(position).getBuses().get(index).getServiceNo()));
		for(ComingTime time : busStops.get(position).getBuses().get(index).getComingTime()){
			if(Utils.removePreZero(time.getServiceNo())
					.equalsIgnoreCase(Utils.removePreZero(busStops.get(position).getBuses().get(index).getServiceNo()))){
				if(Integer.parseInt(time.getNextBus().replace(" ", "")) == 0){
					nextBusTextView.setTextColor(getActivity().getResources().getColor(R.color.red));
					nextBusTextView.setTextSize(16);
					nextBusTextView.setText("Arrival");
				}else if(Integer.parseInt(time.getNextBus().replace(" ", "")) < 0){
					nextBusTextView.setTextColor(getActivity().getResources().getColor(R.color.red));
					nextBusTextView.setTextSize(12); 
					nextBusTextView.setText("No service");
				}else{
					nextBusTextView.setTextColor(getActivity().getResources().getColor(R.color.white));
					nextBusTextView.setTextSize(22);
					nextBusTextView.setText(time.getNextBus());
				}
				
				if(Integer.parseInt(time.getSubSequentbus().replace(" ", "")) < 0){
					subBusTextView.setTextColor(getActivity().getResources().getColor(R.color.red));
					subBusTextView.setVisibility(View.GONE);
				}else{
					subBusTextView.setVisibility(View.VISIBLE);
					subBusTextView.setTextColor(getActivity().getResources().getColor(R.color.white));
					subBusTextView.setText(time.getSubSequentbus());
				}
			}
		}
	}
	
	private LinearLayout getRow(final int position, int start, int end){
		LinearLayout container = new LinearLayout(getActivity());
		container.setLayoutParams(getRowParams());
		container.setOrientation(LinearLayout.HORIZONTAL);
		for(int i = start; i < end; i++){
			Button busBtn = new Button(getActivity());
			busBtn.setLayoutParams(getButtonParams());
			busBtn.setWidth((getScreenDisplay().widthPixels / (2 * columnCount)) - 4);
			busBtn.setText(busStops.get(position).getBuses().get(i).getServiceNo().replaceFirst("^0+(?!$)", ""));
			busBtn.setTextAppearance(getActivity(), R.style.BusTextStyle);
			busBtn.setBackgroundResource(R.drawable.bus_no_selector);
			busBtn.setGravity(Gravity.CENTER);
			final int index = i;
			busBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getNextBusTime(index, position);
				}
			});
			container.addView(busBtn);
		}
		return container;
	}
	
	private View loadingView(int position){
		ImageView view = new ImageView(getActivity());
		view.setId(position);
		view.setLayoutParams(new LayoutParams(32, 32));
		view.setBackgroundResource(R.drawable.loading);
		view.startAnimation(getLoadingAnimation());
		return view;
	}
	
	private Animation rotation;
	private Animation getLoadingAnimation(){
		if(rotation == null){
			rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.loading_anim);
			rotation.setRepeatCount(Animation.INFINITE);
		}
		return rotation;
	}
	
	private DisplayMetrics getScreenDisplay(){
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics;
	}
	
	private LinearLayout.LayoutParams getRowParams(){
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		return params;
	}
	
	private LinearLayout.LayoutParams getButtonParams(){
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(2, 2, 2, 2);
		return params;
	}
	
	private void startProgressDialog(){
		proDialog = new ProgressDialog(getActivity(), R.style.NiuDialog);
		proDialog.setMessage("Loading...");
		proDialog.setIndeterminate(true);
		proDialog.setIndeterminateDrawable(getResources().getDrawable(R.anim.progress_anim));
		proDialog.show();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(busStops != null)
			outState.putParcelable("bus_stop", busStops);
		super.onSaveInstanceState(outState);
		
	}
	
	class NearByBusAdapter extends BaseAdapter{
		
		@Override
		public int getCount() {
			return busStops.size();
		}

		@Override
		public Object getItem(int position) {
			return busStops.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.nearby_list_item_layout, null);
				holder.stopCodeTextView = (TextView) convertView.findViewById(R.id.nearby_stopcode_textview);
				holder.distanceTextView = (TextView) convertView.findViewById(R.id.nearby_distance_textview);
				holder.roadTextView = (TextView) convertView.findViewById(R.id.nearby_bustop_road);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.stopCodeTextView.setText(Html.fromHtml("<b>" + busStops.get(position).getStopCode() + "</b>" + " - " 
			+ busStops.get(position).getDescription()));
			holder.distanceTextView.setText(busStops.get(position).getDistance() + "km");
			holder.roadTextView.setText(busStops.get(position).getRoad());
			return convertView;
		}
		
		class ViewHolder{
			private TextView stopCodeTextView;
			private TextView distanceTextView;
			private TextView roadTextView;
		}
	}
	
	@Override
	public void onItemClick(View view, int position) {
		((LinearLayout)view.findViewById(R.id.nearby_buservice_container)).removeAllViews();
		if(busStops.get(position).getBuses() == null || busStops.get(position).getBuses().size() == 0){
			((LinearLayout)view.findViewById(R.id.nearby_buservice_container)).addView(loadingView(position));
			requestBusService(position);
		}else{
			getBuServiceView(position, (LinearLayout)view.findViewById(R.id.nearby_buservice_container));
		}
				
	}
	
	interface UpdateMarkerListener{
		void updateMarker();
	}

	@Override
	public void changeCompleted() {
		requestNearbyBusStop();
		LocationHelper.getInstance(getActivity()).setListener(null);
	}
	
}

















