package com.niu.sgbus;

import java.util.ArrayList;
import java.util.List;

import com.niu.models.BuService;
import com.niu.models.BuServices;
import com.niu.models.Bus;
import com.niu.models.Buses;
import com.niu.models.ComingTime;
import com.niu.models.Road;
import com.niu.models.Roads;
import com.niu.network.BaseRequestListener;
import com.niu.network.RestfulCall;
import com.niu.tools.Constants;
import com.niu.tools.DataBaseHelper;
import com.niu.tools.Utils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends FragmentActivity{
	private final String TAG = "SearchActivity";
	private String searchText;
	private ListView resultListView;
	private List<?> results;
	
	private TextView headerTV;
	private BaseAdapter adapter;
	private ProgressDialog proDialog;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.search_layout);
		searchText = getIntent().getStringExtra("search_text");
		headerTV = (TextView) findViewById(R.id.searchby_textview);
		
		if(searchText.length() == 0){
			Toast.makeText(this, "Search bar cannot be empty.", Toast.LENGTH_LONG).show();
			return;
		}
		headerTV.setText("Search By " + searchText);
		
		resultListView = (ListView) findViewById(R.id.search_result_listview);
		resultListView.setCacheColorHint(R.color.white);
		resultListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(results.get(position) instanceof BuService){
					Intent intent = new Intent();
					intent.setClass(SearchActivity.this, ServiceRouteAvtivity.class);
					intent.putExtra("service", (BuService)results.get(position));
					SearchActivity.this.startActivity(intent);
				}else if(results.get(position) instanceof Road){
					Intent intent = new Intent();
					intent.setClass(SearchActivity.this, RoadListActivity.class);
					intent.putExtra("road", (Road)results.get(position));
					SearchActivity.this.startActivity(intent);
				}
			}});
		
		startProgressDialog();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				goSearch();
			}
		}).start();
	}
	
	protected void goSearch() {
		if(searchText.matches("^\\d+$")){
			if(searchText.length() == 5){
				searchByStop();
			}else{
				searchByBus();
			}
		}else{
			searchByRoad();
		}
	}
	
	private void searchByStop(){
		Bundle params = new Bundle();
		params.putString("stop", searchText);
		params.putString("iriskey", Utils.getRandomIrisKey());
		RestfulCall.getInstance().request(Constants.APIBASE_URL + Constants.PATH_SERVICES_BY_STOP + "?", params, "GET", new BaseRequestListener() {
			
			@Override
			public void onComplete(String response, Object state) {
				final Buses buses = new Buses();
				buses.parseXMLData(response);
				if(buses.size() > 0){
					SearchActivity.this.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							results = buses;
							adapter = new SearchListAdapter(results);
							resultListView.setAdapter(adapter);
							proDialog.dismiss();
						}
					});
				}else if(buses.size() == 0){
					showEmptyView();
					return;
				}else if(response.contains("Access deny")){
					Log.v(TAG, "Access deny ...");
					try {
						Thread.sleep(1000);
						searchByStop();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private void searchByBus(){
		final BuServices buSvcs = new BuServices();
		DataBaseHelper dbHelper = new DataBaseHelper(this);
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(Constants.SVC_TABLE, null, 
				Constants.SVCTableColumns.svc_no + " LIKE '%"+ searchText + "%'", null, null, null, null);
		boolean isData = cursor.moveToFirst();
		if(!isData){
			if(!cursor.isClosed()){
				cursor.close();
			}
			if(db.isOpen()){
				db.close();
			}
			try {
				Thread.sleep(5 * 1000);
				searchByBus();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else{
			BuService svc = null;
			while(!cursor.isAfterLast()){
				svc = new BuService();
				svc.setServiceNo(cursor.getString(cursor.getColumnIndexOrThrow(Constants.SVCTableColumns.svc_no)));
				svc.setDirection(cursor.getString(cursor.getColumnIndexOrThrow(Constants.SVCTableColumns.direction)));
				svc.setTowardStopCode(cursor.getString(cursor.getColumnIndexOrThrow(Constants.SVCTableColumns.towardStopCode)));
				svc.setTowardStopDesc(cursor.getString(cursor.getColumnIndexOrThrow(Constants.SVCTableColumns.towardStopDesc)));
				svc.setTowardRoadDesc(cursor.getString(cursor.getColumnIndexOrThrow(Constants.SVCTableColumns.towardRoadDesc)));
				buSvcs.add(svc);
				cursor.moveToNext();
			}
			
			if(!cursor.isClosed()){
				cursor.close();
			}
			if(db.isOpen()){
				db.close();
			}
		}
		
		if(buSvcs.size() > 0){
			SearchActivity.this.runOnUiThread(new Runnable(){

				@Override
				public void run() {
					results = buSvcs;
					adapter = new SearchListAdapter(results);
					resultListView.setAdapter(adapter);
					proDialog.dismiss();
				}});
		}else{
			showEmptyView();
		}
	}

	private void searchByRoad(){
		Roads roads = new Roads();
		roads.getRoadInfo(this);
		filterByRoad(roads);
	}
	
	private void filterByRoad(Roads roads){
		final List<Road> filteredRoad = new ArrayList<Road>();
		String str = searchText.toLowerCase().replace("road", "").replace("ave", "");
		if(str.endsWith(" ")){
			str = str.substring(0, str.length() - 1);
		}
		for(Road road : roads){
			if(road.getName().toLowerCase().contains(str)){
				filteredRoad.add(road);
			}
		}
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(filteredRoad.size() == 0){
					showEmptyView();
					return;
				}
				results = filteredRoad;
				adapter = new SearchListAdapter(results);
				resultListView.setAdapter(adapter);
				proDialog.dismiss();
			}
		});
	}
	
	private void showEmptyView(){
		this.runOnUiThread(new Runnable(){

			@Override
			public void run() {
				resultListView.setVisibility(View.GONE);
				findViewById(R.id.empty_textview).setVisibility(View.VISIBLE);
				proDialog.dismiss();

			}});
	}
	
	private void startProgressDialog(){
		proDialog = new ProgressDialog(this, R.style.NiuDialog);
		proDialog.setMessage("Loading...");
		proDialog.setIndeterminate(true);
		proDialog.setIndeterminateDrawable(getResources().getDrawable(R.anim.progress_anim));
		proDialog.show();
	}
	
	class SearchListAdapter extends BaseAdapter{
		private List<?> list;
		private LayoutInflater inflater;
		
		public SearchListAdapter(List<?> list) {
			this.list = list;
			inflater = (LayoutInflater) SearchActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
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
				if(list.get(position) instanceof Road){
					convertView = inflater.inflate(R.layout.road_item_layout, null);
					holder.roadTV = (TextView) convertView.findViewById(R.id.road_textview);
				}else if(list.get(position) instanceof Bus){
					convertView = inflater.inflate(R.layout.stop_item_layout, null);
					holder.busTV = (TextView) convertView.findViewById(R.id.bus_no_textview);
					holder.timingBtn = (Button) convertView.findViewById(R.id.search_comingtime_btn);
				}else if(list.get(position) instanceof BuService){
					convertView = inflater.inflate(R.layout.all_ser_listitem_layout, null);
					holder.busTV = (TextView) convertView.findViewById(R.id.all_ser_bus_no);
					holder.towardTV = (TextView) convertView.findViewById(R.id.all_ser_toward_stop);
					holder.roadTV = (TextView) convertView.findViewById(R.id.all_ser_road_desc);
				}
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			if(list.get(position) instanceof Road){
				holder.roadTV.setText(((Road)list.get(position)).getName());
			}else if(list.get(position) instanceof Bus){
				holder.busTV.setText(((Bus)list.get(position)).getServiceNo());
				holder.timingBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						((RelativeLayout)v.getParent()).findViewById(R.id.search_loading_icon).setVisibility(View.VISIBLE);
						((RelativeLayout)v.getParent()).findViewById(R.id.search_loading_icon).startAnimation(getLoadingAnimation());
						getComingBusTime(position);
					}
				});
			}else if(list.get(position) instanceof BuService){
				holder.busTV.setText(((BuService)list.get(position)).getServiceNo());
				holder.towardTV.setText(Html.fromHtml("<b>To </b>" + ((BuService)list.get(position)).getTowardStopDesc()));
				holder.roadTV.setText(((BuService)list.get(position)).getTowardRoadDesc());
			}
			
			return convertView;
		}
		
		private void getComingBusTime(final int position){
			Bundle params = new Bundle();
			params.putString("busstop", searchText);
			params.putString("svc", ((Bus)list.get(position)).getServiceNo());
			params.putString("iriskey", Utils.getRandomIrisKey());
			RestfulCall.getNewInstance().request(Constants.APIBASE_URL + Constants.PATH_NEXTBUS + "?", params, "GET", new BaseRequestListener() {
				
				@Override
				public void onComplete(String response, Object state) {
					if(response == null) return;
					if(!response.contains("Access deny")){
						((Bus)list.get(position)).parseXMLData(response);
						SearchActivity.this.runOnUiThread(new Runnable(){

							@Override
							public void run() {
								updateComingTime(position);
							}});
						dismissLoadingIcon(position);
					}else{
						try {
							Thread.sleep(1000);
							getComingBusTime(position);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
		
		private void updateComingTime(final int position){
			LinearLayout view = (LinearLayout) resultListView.getChildAt(position - resultListView.getFirstVisiblePosition());
			TextView firTextView = (TextView) view.findViewById(R.id.search_bus_first_time);
			TextView secTextView = (TextView) view.findViewById(R.id.search_bus_sec_time);
			setTimeTextView(position, ((Bus)list.get(position)).getComingTime(), firTextView, secTextView);
		}
		
		private void setTimeTextView(final int position, List<ComingTime> comingTimes, TextView firTextView, TextView secTextView){
			for(ComingTime time : comingTimes){
				if(Utils.removePreZero(time.getServiceNo())
						.equalsIgnoreCase(Utils.removePreZero(((Bus)list.get(position)).getServiceNo()))){
					if(Integer.parseInt(time.getNextBus().replace(" ", "")) == 0){
						firTextView.setTextColor(SearchActivity.this.getResources().getColor(R.color.red));
						firTextView.setTextSize(16);
						firTextView.setText("Arrival");
					}else if(Integer.parseInt(time.getNextBus().replace(" ", "")) < 0){
						firTextView.setTextColor(SearchActivity.this.getResources().getColor(R.color.red));
						firTextView.setTextSize(16);
						firTextView.setText("No service");
					}else{
						firTextView.setTextColor(SearchActivity.this.getResources().getColor(R.color.title_color));
						firTextView.setTextSize(22);
						firTextView.setText(time.getNextBus());
					}
					
					if(Integer.parseInt(time.getSubSequentbus().replace(" ", "")) < 0){
						secTextView.setTextColor(SearchActivity.this.getResources().getColor(R.color.red));
						secTextView.setVisibility(View.GONE);
					}else{
						secTextView.setTextColor(SearchActivity.this.getResources().getColor(R.color.title_color));
						secTextView.setVisibility(View.VISIBLE);
						secTextView.setText(time.getSubSequentbus());
					}
				}
			}
		}
		
		private Animation rotation;
		private Animation getLoadingAnimation(){
			if(rotation == null){
				rotation = AnimationUtils.loadAnimation(SearchActivity.this, R.anim.loading_anim);
				rotation.setRepeatCount(Animation.INFINITE);
			}
			return rotation;
		}
		
		private void dismissLoadingIcon(final int position){
			rotation.cancel();
			final LinearLayout view = (LinearLayout) resultListView.getChildAt(position - resultListView.getFirstVisiblePosition());
			if(view != null && view.findViewById(R.id.search_loading_icon).getAnimation() != null)
				view.findViewById(R.id.search_loading_icon).setAnimation(null);
			SearchActivity.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					view.findViewById(R.id.search_loading_icon).setVisibility(View.GONE);
				}
			});
		}
		
		class ViewHolder {
			private TextView busTV;
			private TextView roadTV;
			private TextView towardTV;
			private Button timingBtn;
		}
	}

}
