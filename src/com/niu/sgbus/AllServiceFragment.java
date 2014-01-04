package com.niu.sgbus;

import java.util.ArrayList;
import java.util.List;

import com.niu.models.BuService;
import com.niu.models.BuServices;
import com.niu.network.BaseRequestListener;
import com.niu.network.RestfulCall;
import com.niu.tools.Constants;
import com.niu.tools.Utils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AllServiceFragment extends Fragment{
	private final String TAG = "AllServiceFragment";
	private BuServices buServices;
	private ListView serviceListView;
	private ServiceBusAdapter adapter;
	private LayoutInflater inflater;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState == null){
			requestAllBusStop();
		}else{
			buServices = (BuServices) savedInstanceState.get("all_services");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.list_fragment_layout, container, false);
		this.inflater = inflater;
		adapter = new ServiceBusAdapter();
		serviceListView = (ListView) rootView.findViewById(R.id.bus_fragment_listview);
		serviceListView.setAdapter(adapter);
		serviceListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), ServiceRouteAvtivity.class);
				intent.putExtra("service", buServices.get(position));
				getActivity().startActivity(intent);
			}
		});
		return rootView;
	}

	private void init(){
		buServices = new BuServices();
	}
	
	private void requestAllBusStop(){
		buServices.getSvcFromDB(getActivity());
		if(buServices.size() == 0){
			getAllBusStopFromInternet();
		}else{
			adapter.notifyDataSetChanged();
			searchEditTracking();
		}
	}
	
	private void getAllBusStopFromInternet(){
		Bundle params = new Bundle();
		params.putString("iriskey", Utils.getRandomIrisKey());
		RestfulCall.getInstance().request(Constants.APIBASE_URL + Constants.PATH_SERVICES + "&", params, "GET", new BaseRequestListener() {
			
			@Override
			public void onComplete(final String response, Object state) {
				if(getActivity() == null) return;
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if(!response.contains("Access deny")){
							buServices.parseXMLData(response);
							adapter.notifyDataSetChanged();
							searchEditTracking();
							saveBuSvc2DB();
						}else{
							try {
								Thread.sleep(1000);
								getAllBusStopFromInternet();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				});
			} 
		});
	}
	
	private void saveBuSvc2DB(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				buServices.saveSvc2DB(getActivity());
			}}).start();
	}
	
	private int textLength = 0;
	private List<BuService> copyStops;
	private void searchEditTracking(){
		copyStops = new ArrayList<BuService>(buServices);
		((MainActivity)getActivity()).searchTextView.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(AllServiceFragment.this.getUserVisibleHint()){
					textLength = ((MainActivity)getActivity()).searchTextView.getText().length();
					buServices.clear();
					for(BuService stop : copyStops){
						if(textLength <= stop.getServiceNo().length()){
							if(((MainActivity)getActivity()).searchTextView.getText().toString()
									.equalsIgnoreCase((String)stop.getServiceNo().subSequence(0, textLength))
									|| stop.getServiceNo().contains(((MainActivity)getActivity()).searchTextView.getText().toString())){
								buServices.add(stop);
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(buServices != null)
			outState.putParcelable("all_services", buServices);
		super.onSaveInstanceState(outState);
		
	}
	
	class ServiceBusAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return buServices.size();
		}

		@Override
		public Object getItem(int position) {
			return buServices.get(position);
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
				convertView = inflater.inflate(R.layout.all_ser_listitem_layout, null);
				holder.serNoTextView = (TextView) convertView.findViewById(R.id.all_ser_bus_no);
				holder.towardStopTextView = (TextView) convertView.findViewById(R.id.all_ser_toward_stop);
				holder.roadTextView = (TextView) convertView.findViewById(R.id.all_ser_road_desc);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.serNoTextView.setText(buServices.get(position).getServiceNo());
			holder.towardStopTextView.setText(Html.fromHtml("<b>To </b>" + buServices.get(position).getTowardStopDesc()));
			holder.roadTextView.setText(buServices.get(position).getTowardRoadDesc());
			return convertView;
		}
		
		class ViewHolder{
			private TextView serNoTextView;
			private TextView towardStopTextView;
			private TextView roadTextView;
		}
	}
}
