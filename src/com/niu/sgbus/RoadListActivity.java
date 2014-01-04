package com.niu.sgbus;

import com.niu.models.Buses;
import com.niu.models.ComingTime;
import com.niu.models.Road;
import com.niu.network.BaseRequestListener;
import com.niu.network.RestfulCall;
import com.niu.tools.Constants;
import com.niu.tools.Utils;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RoadListActivity extends FragmentActivity{
	private final String TAG = "RoadListActivity";
	private Road road;
	private TextView headerText;
	private ListView listView;
	private RoadListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.roadlist_layout);
		road = getIntent().getParcelableExtra("road");
		if(road == null) return;
		
		headerText = (TextView) findViewById(R.id.road_header_textview);
		headerText.setText(road.getName());
		
		adapter = new RoadListAdapter();
		listView = (ListView) findViewById(R.id.road_listview);
		listView.setCacheColorHint(R.color.white);
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showDialog(road.getRoutes().get(position).getStop());
			}
		});
	}
	
	private SildingUpDialog silingupViewContainer;
	private Button cancelBtn;
	private LinearLayout busContainer;
	private RelativeLayout timingContainer;
	private TextView busTV;
	private TextView nextBusTextView;
	private TextView subBusTextView;
	private ImageView loadingView;
	private TextView sildingHeaderTV;
	private RelativeLayout silingupView;
	private void showDialog(String stopCode) {
		if(silingupViewContainer == null){
			silingupView = (RelativeLayout) this.getLayoutInflater().inflate(R.layout.silding_up_down_layout, null);
			silingupViewContainer = new SildingUpDialog(this, silingupView);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			((RelativeLayout)listView.getParent()).addView(silingupViewContainer, params);
			
			cancelBtn = (Button) silingupView.findViewById(R.id.cancel_btn);
			cancelBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					silingupViewContainer.hide();
				}
			});
			
			busContainer = (LinearLayout) silingupView.findViewById(R.id.left_container);
			timingContainer = (RelativeLayout) silingupView.findViewById(R.id.right_container);
			loadingView = (ImageView) silingupView.findViewById(R.id.roadlist_progressbar);
			sildingHeaderTV = (TextView) silingupView.findViewById(R.id.roadlist_stop_tv);
			busTV = (TextView) silingupView.findViewById(R.id.roadlist_bus_no_textview);
			nextBusTextView = (TextView) silingupView.findViewById(R.id.roadlist_bus_first_time);
			subBusTextView = (TextView) silingupView.findViewById(R.id.roadlist_bus_sec_time);
		}else{
			silingupViewContainer.show();
		}
		
		showLoadingView(stopCode);
    }
	
	private void hideLoadingView(){
		rotation.cancel();
		loadingView.setAnimation(null);
		loadingView.setVisibility(View.GONE);
	}
	
	private void showLoadingView(String stopCode){
		if(!stopCode.equalsIgnoreCase(sildingHeaderTV.getText().toString())){
			sildingHeaderTV.setText(stopCode);
			
			busContainer.removeAllViews();
			busContainer.setVisibility(View.GONE);
			timingContainer.setVisibility(View.GONE);
			loadingView.setVisibility(View.VISIBLE);
			loadingView.startAnimation(getLoadingAnimation());
			
			requestBusService(stopCode);
		}
	}
	
	private Buses buses;
	public void requestBusService(final String stopCode){
		Bundle params = new Bundle();
		params.putString("stop", stopCode);
		params.putString("iriskey", Utils.getRandomIrisKey());
		RestfulCall.getInstance().request(Constants.APIBASE_URL + Constants.PATH_SERVICES_BY_STOP + "?", 
				params, "GET", new BaseRequestListener() {
			
			@Override
			public void onComplete(String response, Object state) {
				buses = new Buses();
				buses.parseXMLData(response);
				if(buses.size() > 0){
					RoadListActivity.this.runOnUiThread(new Runnable(){

						@Override
						public void run() {
							hideLoadingView();
							getBuServiceView();
						}});
				}else if(response.contains("Access deny")){
					Log.v(TAG, "Access deny ...");
					try {
						Thread.sleep(1000);
						requestBusService(stopCode);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private int columnCount = 3;
	private void getBuServiceView(){
		busContainer.setVisibility(View.VISIBLE);
		timingContainer.setVisibility(View.VISIBLE);
		int busCount = buses.size();
		int index = 0;
		while(index < busCount){
			if(busCount - index <= columnCount){
				busContainer.addView(getRow(index, busCount));
			}else{
				busContainer.addView(getRow(index, index + columnCount));
			}
			index += columnCount;
		}
		getNextBusTime(0);
	}
	
	private LinearLayout getRow(int start, int end){
		LinearLayout container = new LinearLayout(this);
		container.setLayoutParams(getRowParams());
		container.setOrientation(LinearLayout.HORIZONTAL);
		for(int i = start; i < end; i++){
			Button busBtn = new Button(this);
			busBtn.setLayoutParams(getButtonParams());
			busBtn.setWidth((getScreenDisplay().widthPixels / (2 * columnCount)) - 4);
			busBtn.setText(buses.get(i).getServiceNo().replaceFirst("^0+(?!$)", ""));
			busBtn.setTextAppearance(this, R.style.BusTextStyle);
			busBtn.setBackgroundResource(R.drawable.bus_no_selector);
			busBtn.setGravity(Gravity.CENTER);
			final int index = i;
			busBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					getNextBusTime(index);
				}
			});
			container.addView(busBtn);
		}
		return container;
	}
	
	private void getNextBusTime(final int index){
		Bundle params = new Bundle();
		params.putString("busstop", sildingHeaderTV.getText().toString());
		params.putString("svc", buses.get(index).getServiceNo());
		params.putString("iriskey", Utils.getRandomIrisKey());
		RestfulCall.getInstance().request(Constants.APIBASE_URL + Constants.PATH_NEXTBUS + "?", params, "GET", new BaseRequestListener() {
			
			@Override
			public void onComplete(String response, Object state) {
				buses.get(index).parseXMLData(response);
				if(!response.contains("Access deny")){
					RoadListActivity.this.runOnUiThread(new Runnable(){
						@Override
						public void run() {
							updateComingTimeView(index);
						}
					});
				}else{
					try {
						Thread.sleep(1000);
						getNextBusTime(index);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private void updateComingTimeView(final int index){
		if(silingupView == null) return;
		busTV.setText(Utils.removePreZero(buses.get(index).getServiceNo()));
		for(ComingTime time : buses.get(index).getComingTime()){
			if(Utils.removePreZero(time.getServiceNo())
					.equalsIgnoreCase(Utils.removePreZero(buses.get(index).getServiceNo()))){
				if(Integer.parseInt(time.getNextBus().replace(" ", "")) == 0){
					nextBusTextView.setTextColor(this.getResources().getColor(R.color.red));
					nextBusTextView.setTextSize(16);
					nextBusTextView.setText("Arrival");
				}else if(Integer.parseInt(time.getNextBus().replace(" ", "")) < 0){
					nextBusTextView.setTextColor(this.getResources().getColor(R.color.red));
					nextBusTextView.setTextSize(12); 
					nextBusTextView.setText("No service");
				}else{
					nextBusTextView.setTextColor(this.getResources().getColor(R.color.white));
					nextBusTextView.setTextSize(22);
					nextBusTextView.setText(time.getNextBus());
				}
				
				if(Integer.parseInt(time.getSubSequentbus().replace(" ", "")) < 0){
					subBusTextView.setTextColor(this.getResources().getColor(R.color.red));
					subBusTextView.setVisibility(View.GONE);
				}else{
					subBusTextView.setVisibility(View.VISIBLE);
					subBusTextView.setTextColor(this.getResources().getColor(R.color.white));
					subBusTextView.setText(time.getSubSequentbus());
				}
			}
		}
	}
	
	private LinearLayout.LayoutParams getButtonParams(){
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(2, 2, 2, 2);
		return params;
	}
	
	private LinearLayout.LayoutParams getRowParams(){
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		return params;
	}
	
	private DisplayMetrics metrics;
	private DisplayMetrics getScreenDisplay(){
		if(metrics == null){
			metrics = new DisplayMetrics();
			this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		}
		return metrics;
	}
	
	private Animation rotation;
	private Animation getLoadingAnimation(){
		if(rotation == null){
			rotation = AnimationUtils.loadAnimation(this, R.anim.loading_anim);
			rotation.setRepeatCount(Animation.INFINITE);
		}else{
			rotation.reset();
		}
		return rotation;
	}
	
	class RoadListAdapter extends BaseAdapter{
		private LayoutInflater inflater;
		public RoadListAdapter() {
			inflater = (LayoutInflater) RoadListActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return road.getRoutes().size();
		}

		@Override
		public Object getItem(int position) {
			return road.getRoutes().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.roadlist_item_layout, null);
				holder.roadTV = (TextView) convertView.findViewById(R.id.roadlist_road_tv);
				holder.svcTV = (TextView) convertView.findViewById(R.id.roadlist_svc_tv);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.roadTV.setText(road.getRoutes().get(position).getName());
			holder.svcTV.setText(road.getRoutes().get(position).getStop());
			return convertView;
		}
		
		class ViewHolder{
			private TextView roadTV;
			private TextView svcTV;
		}
		
	}

}
