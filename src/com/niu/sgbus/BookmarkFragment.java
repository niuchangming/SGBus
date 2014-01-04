package com.niu.sgbus;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.niu.models.ComingTime;
import com.niu.network.BaseRequestListener;
import com.niu.network.RestfulCall;
import com.niu.tools.Constants;
import com.niu.tools.DataBaseHelper;
import com.niu.tools.Utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BookmarkFragment extends Fragment{
	private final String TAG = "BookmarkFragment";
	private DataBaseHelper dbHelper;
	private LayoutInflater inflater;
	private List<Bookmark> bookmarks;
	private ListView listView;
	private BookmarkBusAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bookmarks = new ArrayList<Bookmark>();
		getBookMark();
		searchEditTracking();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.list_fragment_layout, container, false);
		this.inflater = inflater;
		adapter = new BookmarkBusAdapter();
		listView = (ListView) rootView.findViewById(R.id.bus_fragment_listview);
		listView.setCacheColorHint(R.color.white);
		listView.setAdapter(adapter);
		return rootView;
	}
	
	private int textLength = 0;
	private List<Bookmark> copyBookmarks;
	private void searchEditTracking(){
		copyBookmarks = new ArrayList<Bookmark>(bookmarks);
		((MainActivity)getActivity()).searchTextView.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(BookmarkFragment.this.getUserVisibleHint()){
					textLength = ((MainActivity)getActivity()).searchTextView.getText().length();
					bookmarks.clear();
					for(Bookmark bookmark : copyBookmarks){
						if(textLength <= bookmark.getStop().length()){
							if(((MainActivity)getActivity()).searchTextView.getText().toString()
									.equalsIgnoreCase(bookmark.getStop().subSequence(0, textLength).toString())
									|| bookmark.getBus()
									.contains(((MainActivity)getActivity()).searchTextView.getText().toString())){
								bookmarks.add(bookmark);
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
	
	private void getBookMark(){
		if(dbHelper == null){
			dbHelper = new DataBaseHelper(getActivity());
		}
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = null;
		try{
			cursor = db.query(Constants.SBS_TABLE, null, null, null, null, null, null);
			cursor.moveToFirst();
			
			Bookmark bookmark = null;
			while(!cursor.isAfterLast()){
				bookmark = new Bookmark();
				bookmark.setBus(cursor.getString(cursor.getColumnIndexOrThrow(Constants.busTableColumns.bus)));
				bookmark.setStop(cursor.getString(cursor.getColumnIndexOrThrow(Constants.busTableColumns.stop)));
				bookmark.setLat(cursor.getString(cursor.getColumnIndexOrThrow(Constants.busTableColumns.lat)));
				bookmark.setLng(cursor.getString(cursor.getColumnIndexOrThrow(Constants.busTableColumns.lng)));
				bookmark.setRoad(cursor.getString(cursor.getColumnIndexOrThrow(Constants.busTableColumns.road)));
				bookmark.setStopDesc(cursor.getString(cursor.getColumnIndexOrThrow(Constants.busTableColumns.stopDesc)));
				bookmarks.add(bookmark);
				cursor.moveToNext();
			}
		}catch(SQLiteException e){
			Log.v(TAG, "Error ---> " + e.toString());
		}finally{
			if(cursor != null &&!cursor.isClosed()){
				cursor.close();
			}
			if(db != null && db.isOpen()){
				db.close();
			}
		}
		Log.v(TAG, "There are " + bookmarks.size() + " data in the DB");
	}
	
	public void updateBookmark(){
		bookmarks.clear();
		getBookMark();
		adapter.notifyDataSetChanged();
	}
	
	class BookmarkBusAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return bookmarks.size();
		}

		@Override
		public Object getItem(int position) {
			return bookmarks.get(position);
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
				convertView = inflater.inflate(R.layout.bookmark_listitem_layout, null);
				holder.BUnSTOPTextView = (TextView) convertView.findViewById(R.id.bookmark_bus_and_stop);
				holder.stopDescTextView = (TextView) convertView.findViewById(R.id.bookmark_stopdesc);
				holder.timeBtn = (Button) convertView.findViewById(R.id.bookmark_comingtime_btn);
				holder.delBtn = (Button) convertView.findViewById(R.id.bookmark_delete_btn);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.BUnSTOPTextView.setText(bookmarks.get(position).getBus() + " - " + bookmarks.get(position).getStop());
			holder.stopDescTextView.setText(bookmarks.get(position).getStopDesc());
			holder.timeBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					((RelativeLayout)v.getParent()).findViewById(R.id.bookmark_loading_icon).setVisibility(View.VISIBLE);
					((RelativeLayout)v.getParent()).findViewById(R.id.bookmark_loading_icon).startAnimation(getLoadingAnimation());
					getComingBusTime(position);
				}
			});
			holder.delBtn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					deleteBookmark(position);
				}});
			return convertView;
		}
		
		private void getComingBusTime(final int position){
			Bundle params = new Bundle();
			params.putString("busstop", bookmarks.get(position).getStop());
			params.putString("svc", bookmarks.get(position).getBus());
			params.putString("iriskey", Utils.getRandomIrisKey());
			RestfulCall.getNewInstance().request(Constants.APIBASE_URL + Constants.PATH_NEXTBUS + "?", params, "GET", new BaseRequestListener() {
				
				@Override
				public void onComplete(String response, Object state) {
					if(response == null) return;
					if(response.contains("Acess deny")){
						try {
							Thread.sleep(1000);
							getComingBusTime(position);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}else{
						bookmarks.get(position).setComingTimes(parseXMLData(response));
						getActivity().runOnUiThread(new Runnable(){

							@Override
							public void run() {
								updateComingTime(position);
							}});
						dismissLoadingIcon(position);
					}
				}
			});
		}
		
		private void dismissLoadingIcon(final int position){
			rotation.cancel();
			final LinearLayout view = (LinearLayout) listView.getChildAt(position - listView.getFirstVisiblePosition());
			if(view != null && view.findViewById(R.id.bookmark_loading_icon).getAnimation() != null)
				view.findViewById(R.id.bookmark_loading_icon).setAnimation(null);
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					view.findViewById(R.id.bookmark_loading_icon).setVisibility(View.GONE);
				}
			});
		}
		
		private void updateComingTime(final int position){
			LinearLayout view = (LinearLayout) listView.getChildAt(position - listView.getFirstVisiblePosition());
			TextView firTextView = (TextView) view.findViewById(R.id.bookmark_bus_first_time);
			TextView secTextView = (TextView) view.findViewById(R.id.bookmark_bus_sec_time);
			setTimeTextView(position, bookmarks.get(position).getComingTimes(), firTextView, secTextView);
		}
		
		private void setTimeTextView(final int position, List<ComingTime> comingTimes, TextView firTextView, TextView secTextView){
			for(ComingTime time : comingTimes){
				if(Utils.removePreZero(time.getServiceNo())
						.equalsIgnoreCase(Utils.removePreZero(bookmarks.get(position).getBus()))){
					if(Integer.parseInt(time.getNextBus().replace(" ", "")) == 0){
						firTextView.setTextColor(getActivity().getResources().getColor(R.color.red));
						firTextView.setTextSize(16);
						firTextView.setText("Arrival");
					}else if(Integer.parseInt(time.getNextBus().replace(" ", "")) < 0){
						firTextView.setTextColor(getActivity().getResources().getColor(R.color.red));
						firTextView.setTextSize(16);
						firTextView.setText("No service");
					}else{
						firTextView.setTextColor(getActivity().getResources().getColor(R.color.title_color));
						firTextView.setTextSize(22);
						firTextView.setText(time.getNextBus());
					}
					
					if(Integer.parseInt(time.getSubSequentbus().replace(" ", "")) < 0){
						secTextView.setTextColor(getActivity().getResources().getColor(R.color.red));
						secTextView.setVisibility(View.GONE);
					}else{
						secTextView.setTextColor(getActivity().getResources().getColor(R.color.title_color));
						secTextView.setVisibility(View.VISIBLE);
						secTextView.setText(time.getSubSequentbus());
					}
				}
			}
		}
		
		private DataBaseHelper dbHelper;
		public void deleteBookmark(int position){
			if(dbHelper == null){
				dbHelper = new DataBaseHelper(getActivity());
			}
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			int i = db.delete(Constants.SBS_TABLE, Constants.busTableColumns.bus + "=? and " + Constants.busTableColumns.stop + "=?",
					new String[]{bookmarks.get(position).getBus(), bookmarks.get(position).getStop()});
			if(i != 0){
				bookmarks.remove(position);
				adapter.notifyDataSetChanged();
			}
		}
		
		public List<ComingTime> parseXMLData(String xml){
			List<ComingTime> comingTimes = new ArrayList<ComingTime>();
			Document doc = loadXml(xml);
			NodeList nodeList = doc.getElementsByTagName("result");
			Element element = null;
			for(int i = 0; i < nodeList.getLength(); i++){
				element = (Element) nodeList.item(i);
				ComingTime time = new ComingTime();
				time.setServiceNo(element.getElementsByTagName("service_no").item(0).getTextContent());
				time.setNextBus(element.getElementsByTagName("nextbus").item(0).getFirstChild().getTextContent());
				time.setSubSequentbus(element.getElementsByTagName("subsequentbus").item(0).getFirstChild().getTextContent());
				comingTimes.add(time);
			}
			return comingTimes;
	    }
	    
	    private Document loadXml(String xml) {
	        try {
	           return (DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml))));
	        } catch (SAXException e) {
	        	return null;
	        } catch (IOException e) {
	        	return null;
	        } catch (ParserConfigurationException e) {
	        	return null;
	        }
	    }
		
	    private Animation rotation;
		private Animation getLoadingAnimation(){
			if(rotation == null){
				rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.loading_anim);
				rotation.setRepeatCount(Animation.INFINITE);
			}
			return rotation;
		}
		
		class ViewHolder{
			private TextView BUnSTOPTextView;
			private TextView stopDescTextView;
			private Button timeBtn;
			private Button delBtn;
		}
	}
	
	class Bookmark{
		private String bus;
		private String stop;
		private String road;
		private String stopDesc;
		private String lat;
		private String lng;
		private List<ComingTime> comingTimes;
		public String getBus() {
			return bus;
		}
		public void setBus(String bus) {
			this.bus = bus;
		}
		public String getStop() {
			return stop;
		}
		public void setStop(String stop) {
			this.stop = stop;
		}
		public String getRoad() {
			return road;
		}
		public void setRoad(String road) {
			this.road = road;
		}
		public String getStopDesc() {
			return stopDesc;
		}
		public void setStopDesc(String stopDesc) {
			this.stopDesc = stopDesc;
		}
		public String getLat() {
			return lat;
		}
		public void setLat(String lat) {
			this.lat = lat;
		}
		public String getLng() {
			return lng;
		}
		public void setLng(String lng) {
			this.lng = lng;
		}
		public List<ComingTime> getComingTimes() {
			return comingTimes;
		}
		public void setComingTimes(List<ComingTime> comingTimes) {
			this.comingTimes = comingTimes;
		}
		
	}
}
