package com.niu.models;

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

import com.niu.tools.Constants;
import com.niu.tools.DataBaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class BuServices extends ArrayList<BuService> implements Parcelable{
	private final String TAG = "BuServices";
	private static final long serialVersionUID = 663585476779879099L;
	public BuServices() {}

    public BuServices(Parcel in) {
        this();
        this.clear();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
        	BuService buSer = new BuService();
        	buSer.setServiceNo(in.readString());
        	buSer.setDirection(in.readString());
        	buSer.setTowardStopCode(in.readString());
        	buSer.setTowardStopDesc(in.readString());
        	buSer.setTowardRoadDesc(in.readString());
        	
        	List<RouteStop> routeStops = new ArrayList<RouteStop>();
    		in.readList(routeStops, getClass().getClassLoader());
        	buSer.setRouteStops(routeStops);
            this.add(buSer);
        }
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		int size = this.size();
        // We have to write the list size, we need him recreating the list
        dest.writeInt(size);

        for (int i = 0; i < size; i++) {
        	BuService buSer = this.get(i);
        	dest.writeString(buSer.getServiceNo());
            dest.writeString(buSer.getDirection());
            dest.writeString(buSer.getTowardStopCode());
            dest.writeString(buSer.getTowardStopDesc());
            dest.writeString(buSer.getTowardRoadDesc());
            dest.writeList(buSer.getRouteStops());
        }
	}
	
	public static final Parcelable.Creator<BuServices> CREATOR = new Parcelable.Creator<BuServices>() {
        public BuServices createFromParcel(Parcel in) {
            return new BuServices(in);
        }

        public BuServices[] newArray(int size) {
            return new BuServices[size];
        }
    };
    
    public void parseXMLData(String xml){
    	this.clear();
		Document doc = loadXml(xml);
		if(doc.getElementsByTagName("service") == null) return;
		NodeList nodeList = doc.getElementsByTagName("service");
		Element element = null;
		for(int i = 0; i < nodeList.getLength(); i++){
			element = (Element) nodeList.item(i);
			getBusStop(element);
		}
    }
    
    public void getBusStop(Element element){	
		BuService buSer = new BuService();
		buSer.setServiceNo(element.getElementsByTagName("service_no").item(0).getTextContent());
		buSer.setDirection(element.getElementsByTagName("direction").item(0).getTextContent());
		buSer.setTowardStopCode(element.getElementsByTagName("towards_stop_code").item(0).getTextContent());
		buSer.setTowardStopDesc(element.getElementsByTagName("towards_stop_desc").item(0).getTextContent());
		buSer.setTowardRoadDesc(element.getElementsByTagName("towards_road_desc").item(0).getTextContent());
		this.add(buSer);
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
    
    public void saveSvc2DB(Context context){
    	DataBaseHelper dbHelper = new DataBaseHelper(context);
    	if(this.size() > 0){
    		SQLiteDatabase db = dbHelper.getWritableDatabase();
    		for(BuService svc : this){
    			ContentValues values = new ContentValues();
    			values.put(Constants.SVCTableColumns.svc_no, svc.getServiceNo());
    			values.put(Constants.SVCTableColumns.direction, svc.getDirection());
    			values.put(Constants.SVCTableColumns.towardStopCode, svc.getTowardStopCode());
    			values.put(Constants.SVCTableColumns.towardStopDesc, svc.getTowardStopDesc());
    			values.put(Constants.SVCTableColumns.towardRoadDesc, svc.getTowardRoadDesc());
    			
    			synchronized(this){
    				try{
    					db.insert(Constants.SVC_TABLE, null, values);
    				}catch(android.database.sqlite.SQLiteConstraintException e){
    					Log.v(TAG, "Insert DB Error: " + e.toString());
    				}catch(SQLiteException e){
    					Log.v(TAG, "Insert DB Error: " + e.toString());
    				}
    			}
    		}
    		
    		if(db != null && db.isOpen()){
    			db.close();
    		}
    	}
    }
    
    public void getSvcFromDB(Context context){
    	DataBaseHelper dbHelper = new DataBaseHelper(context);
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	Cursor cursor = null;
    	try{
    		cursor = db.query(Constants.SVC_TABLE, null, null, null, null, null, null);
    		cursor.moveToFirst();
    		BuService svc = null;
    		while(!cursor.isAfterLast()){
    			svc = new BuService();
    			svc.setServiceNo(cursor.getString(cursor.getColumnIndexOrThrow(Constants.SVCTableColumns.svc_no)));
    			svc.setDirection(cursor.getString(cursor.getColumnIndexOrThrow(Constants.SVCTableColumns.direction)));
    			svc.setTowardStopCode(cursor.getString(cursor.getColumnIndexOrThrow(Constants.SVCTableColumns.towardStopCode)));
    			svc.setTowardStopDesc(cursor.getString(cursor.getColumnIndexOrThrow(Constants.SVCTableColumns.towardStopDesc)));
    			svc.setTowardRoadDesc(cursor.getString(cursor.getColumnIndexOrThrow(Constants.SVCTableColumns.towardRoadDesc)));
    			this.add(svc);
    			cursor.moveToNext();
    		}
    	}catch(SQLiteException e){
    		Log.v(TAG, "Error ---> " + e.toString());
    	}finally{
    		if(cursor != null && !cursor.isClosed()){
    			cursor.close();
    		}
    		if(db != null && db.isOpen()){
    			db.close();
    		}
    	}
    }
}



























