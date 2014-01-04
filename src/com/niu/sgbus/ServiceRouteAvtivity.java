package com.niu.sgbus;

import java.util.ArrayList;
import java.util.List;

import com.niu.models.BuService;
import com.niu.models.ComingTime;
import com.niu.models.RouteStop;
import com.niu.network.BaseRequestListener;
import com.niu.network.RestfulCall;
import com.niu.sildingmenu.SlidingMenu;
import com.niu.sildingmenu.SlidingMenu.OnOpenedListener;
import com.niu.tools.Constants;
import com.niu.tools.DataBaseHelper;
import com.niu.tools.Utils;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ServiceRouteAvtivity extends FragmentActivity{
	private final String TAG = "ServiceRouteAvtivity";
	private ListView stopListView;
	private BuService service;
	private BusRouteAdapter adapter;
	private TextView fromToTextView;
	private EditText searchText;
	private ProgressDialog proDialog;
	private DataBaseHelper dbHelper;
	
	private BusMapFragment mapFragment;
	public SlidingMenu rightMenu;
	private Button mapBtn;
	public boolean canOpenMap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.service_route_layout);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		service = getIntent().getParcelableExtra("service");
		if(service == null) return;
		canOpenMap = false;
		
		startProgressDialog();
		
		searchText = (EditText) findViewById(R.id.search_edittext);
		fromToTextView = (TextView) findViewById(R.id.from_to_direction);
		mapBtn = (Button) findViewById(R.id.global_btn);
		stopListView = (ListView) findViewById(R.id.ser_route_listview);
		stopListView.setCacheColorHint(getResources().getColor(R.color.white));
		adapter = new BusRouteAdapter();
		stopListView.setAdapter(adapter);
		getBusRoute();
		
		initRightSildingMenu();
		mapBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(canOpenMap){
					rightMenu.showMenu();
				}
			}
		});
	}
	
	private void initRightSildingMenu() {
		rightMenu = new SlidingMenu(this);
		rightMenu.setMode(SlidingMenu.RIGHT);
		rightMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		rightMenu.setShadowWidthRes(R.dimen.shadow_width);
		rightMenu.setShadowDrawable(R.drawable.right_shadow);
		rightMenu.setBehindOffset(60);
		rightMenu.setFadeDegree(0.5f);
		rightMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		rightMenu.setMenu(R.layout.right_menu_layout);
		mapFragment = (BusMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
		rightMenu.setOnOpenedListener(new OnOpenedListener() {
			
			@Override
			public void onOpened() {
				mapFragment.moveToNearestRouteStop(service.getRouteStops().get(0));
			}
		});
	}
	
	private void getBusRoute(){
		Bundle params = new Bundle();
		params.putString("iriskey", Utils.getRandomIrisKey());
		params.putString("svc", service.getServiceNo());
		params.putString("dir", service.getDirection());
		RestfulCall.getInstance().request(Constants.APIBASE_URL + Constants.PATH_SVC_STOPS + "?", params, "GET", new BaseRequestListener() {
			
			@Override
			public void onComplete(final String response, Object state) {
				ServiceRouteAvtivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						ServiceRouteAvtivity.this.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								if(response.contains("Access deny")){
									try {
										Thread.sleep(1000);
										getBusRoute();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}else{
									service.parseXMLData(response);
									adapter.notifyDataSetChanged();
									fromToTextView.setText("From " + service.getRouteStops().get(0).getStopDesc() + "\nTo " + service.getRouteStops().get(service.getRouteStops().size()-1).getStopDesc());
									searchEditTracking();
								}
							}
						});
						
						proDialog.dismiss();
						canOpenMap = true;
						mapFragment.initMapForRoute(service.getRouteStops());
					}
				});
			} 
		});
	}
	
	private int textLength = 0;
	private List<RouteStop> copyRoutes;
	private void searchEditTracking(){
		copyRoutes = new ArrayList<RouteStop>(service.getRouteStops());
		searchText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				textLength = searchText.getText().length();
				service.getRouteStops().clear();
				for(RouteStop route : copyRoutes){
					if(textLength <= route.getStopCode().length()){
						if(searchText.getText().toString()
								.equalsIgnoreCase((String)route.getStopCode().subSequence(0, textLength))){
							service.getRouteStops().add(route);
						}
					}
				}
				adapter.notifyDataSetChanged();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
	}
	
	private void startProgressDialog(){
		proDialog = new ProgressDialog(this, R.style.NiuDialog);
		proDialog.setMessage("Loading...");
		proDialog.setIndeterminate(true);
		proDialog.setIndeterminateDrawable(getResources().getDrawable(R.anim.progress_anim));
		proDialog.show();
	}
	
	private Animation rotation;
	private Animation getLoadingAnimation(){
		if(rotation == null){
			rotation = AnimationUtils.loadAnimation(this, R.anim.loading_anim);
			rotation.setRepeatCount(Animation.INFINITE);
		}
		return rotation;
	}
	
	private void dismissLoadingIcon(final int position){
		rotation.cancel();
		final LinearLayout view = (LinearLayout) stopListView.getChildAt(position - stopListView.getFirstVisiblePosition());
		if(view != null && view.findViewById(R.id.busroute_loading_icon).getAnimation() != null)
			view.findViewById(R.id.busroute_loading_icon).setAnimation(null);
		view.findViewById(R.id.busroute_loading_icon).setVisibility(View.GONE);
	}
	
	class BusRouteAdapter extends BaseAdapter{
		private LayoutInflater inflater;
		
		public BusRouteAdapter() {
			this.inflater = (LayoutInflater) ServiceRouteAvtivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return service.getRouteStops().size();
		}

		@Override
		public Object getItem(int position) {
			return service.getRouteStops().get(position);
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
				convertView = inflater.inflate(R.layout.bus_route_item, null);
				holder.stopCodeTextView = (TextView) convertView.findViewById(R.id.busroute_ser_no);
				holder.stopDescTextView = (TextView) convertView.findViewById(R.id.busroute_stop_desc);
				holder.busBookmark = (Button) convertView.findViewById(R.id.busroute_bookmark_btn);
				holder.busComingTime = (Button) convertView.findViewById(R.id.busroute_comingtime_btn);
				holder.firComingTextView = (TextView) convertView.findViewById(R.id.busroute_bus_first_time);
				holder.secComingTextView = (TextView) convertView.findViewById(R.id.busroute_bus_sec_time);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			if(service.getRouteStops().get(position).getComingTimes() == null){
				holder.firComingTextView.setText("");
				holder.secComingTextView.setText("");
			}else{
				holder.firComingTextView.setText(service.getRouteStops().get(position).getComingTimes().get(0).getNextBus());
				holder.secComingTextView.setText(service.getRouteStops().get(position).getComingTimes().get(0).getSubSequentbus());
				setTimeTextView(service.getRouteStops().get(position).getComingTimes(), holder.firComingTextView, holder.secComingTextView);
			}
			holder.stopCodeTextView.setText(service.getRouteStops().get(position).getStopCode());
			holder.stopDescTextView.setText(service.getRouteStops().get(position).getStopDesc());
			holder.busComingTime.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					((RelativeLayout)v.getParent()).findViewById(R.id.busroute_loading_icon).setVisibility(View.VISIBLE);
					((RelativeLayout)v.getParent()).findViewById(R.id.busroute_loading_icon).startAnimation(getLoadingAnimation());
					getComingBusTime(position);
				}
			});
			holder.busBookmark.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(dbHelper == null){
						dbHelper = new DataBaseHelper(ServiceRouteAvtivity.this);
					}
					addBookmark(position);
					((BookmarkFragment)MainActivity.fragments.get(2)).updateBookmark();
				}
			});
			return convertView;
		}
		
		private void addBookmark(int position){
			ContentValues values = new ContentValues();
			values.put(Constants.busTableColumns.stop, service.getRouteStops().get(position).getStopCode());
			values.put(Constants.busTableColumns.bus, service.getServiceNo());
			values.put(Constants.busTableColumns.lat, service.getRouteStops().get(position).getLatitude());
			values.put(Constants.busTableColumns.lng, service.getRouteStops().get(position).getLongitude());
			values.put(Constants.busTableColumns.road, service.getRouteStops().get(position).getRoadDesc());
			values.put(Constants.busTableColumns.stopDesc, service.getRouteStops().get(position).getStopDesc());
			values.put(Constants.busTableColumns.confictNum, service.getServiceNo() + service.getRouteStops().get(position).getStopCode());//bus number + stop number
			
			SQLiteDatabase db;
			synchronized(this){
				db = dbHelper.getWritableDatabase();
				try{
					db.insert(Constants.SBS_TABLE, null, values);
				}catch(android.database.sqlite.SQLiteConstraintException e){
					Log.v(TAG, "Insert DB Error: " + e.toString());
				}catch(SQLiteException e){
					Log.v(TAG, "Insert DB Error: " + e.toString());
				}finally{
					if(db.isOpen()){
						db.close();
					}
				}
			}
		}
		
		private void getComingBusTime(final int position){
			Bundle params = new Bundle();
			params.putString("busstop", service.getRouteStops().get(position).getStopCode());
			params.putString("svc", service.getServiceNo());
			params.putString("iriskey", Utils.getRandomIrisKey());
			RestfulCall.getNewInstance().request(Constants.APIBASE_URL + Constants.PATH_NEXTBUS + "?", params, "GET", new BaseRequestListener() {
				
				@Override
				public void onComplete(String response, Object state) {
					if(response == null) return;
					if(response.contains("Access deny")){
						try {
							Thread.sleep(1000);
							getComingBusTime(position);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}else{
						service.getRouteStops().get(position).parseXMLData(response);
						ServiceRouteAvtivity.this.runOnUiThread(new Runnable(){
							@Override
							public void run() {
								dismissLoadingIcon(position);
								updateComingTime(position);
							}});
					}
				}
			});
		}
		
		private void updateComingTime(final int position){
			LinearLayout view = (LinearLayout) stopListView.getChildAt(position - stopListView.getFirstVisiblePosition());
			TextView firTextView = (TextView) view.findViewById(R.id.busroute_bus_first_time);
			TextView secTextView = (TextView) view.findViewById(R.id.busroute_bus_sec_time);
			setTimeTextView(service.getRouteStops().get(position).getComingTimes(), firTextView, secTextView);
		}
		
		private void setTimeTextView(List<ComingTime> comingTimes, TextView firTextView, TextView secTextView){
			for(ComingTime time : comingTimes){//java.util.ConcurrentModificationException
				if(Utils.removePreZero(time.getServiceNo())
						.equalsIgnoreCase(Utils.removePreZero(service.getServiceNo()))){
					if(Integer.parseInt(time.getNextBus().replace(" ", "")) == 0){
						firTextView.setTextColor(ServiceRouteAvtivity.this.getResources().getColor(R.color.red));
						firTextView.setTextSize(16);
						firTextView.setText("Arrival");
					}else if(Integer.parseInt(time.getNextBus().replace(" ", "")) < 0){
						firTextView.setTextColor(ServiceRouteAvtivity.this.getResources().getColor(R.color.red));
						firTextView.setTextSize(16);
						firTextView.setText("No service");
					}else{
						firTextView.setTextColor(ServiceRouteAvtivity.this.getResources().getColor(R.color.title_color));
						firTextView.setTextSize(22);
						firTextView.setText(time.getNextBus());
					}
					if(Integer.parseInt(time.getSubSequentbus().replace(" ", "")) < 0){
						secTextView.setTextColor(ServiceRouteAvtivity.this.getResources().getColor(R.color.red));
						secTextView.setVisibility(View.GONE);
					}else{
						secTextView.setTextColor(ServiceRouteAvtivity.this.getResources().getColor(R.color.title_color));
						secTextView.setVisibility(View.VISIBLE);
						secTextView.setText(time.getSubSequentbus());
					}
				}
			}
		}
		
		class ViewHolder{ 
			private TextView stopCodeTextView;
			private TextView stopDescTextView;
			private Button busBookmark;
			private Button busComingTime;
			private TextView firComingTextView;
			private TextView secComingTextView;
		}
	}
}
