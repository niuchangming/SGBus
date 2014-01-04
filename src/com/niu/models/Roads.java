package com.niu.models;

import java.io.BufferedInputStream;
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
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Roads extends ArrayList<Road> implements Parcelable {
	private static final long serialVersionUID = 933585696779453096L;
	private final String TAG = "Roads";
	public Roads() {}

    public Roads(Parcel in) {
        this();
        this.clear();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
        	Road road = new Road();
        	road.setName(in.readString());
    		in.readList(road.getRoutes(), getClass().getClassLoader());
            this.add(road);
        }
    }
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		int size = this.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++) {
        	Road road = this.get(i);
        	dest.writeString(road.getName());
            dest.writeList(road.getRoutes());
        }
	}
	
	public static final Parcelable.Creator<Roads> CREATOR = new Parcelable.Creator<Roads>() {
        public Roads createFromParcel(Parcel in) {
            return new Roads(in);
        }

        public Roads[] newArray(int size) {
            return new Roads[size];
        }
    };
    
    public void getRoadInfo(Context context){
		AssetManager am = context.getAssets();
		try {
			BufferedInputStream in = new BufferedInputStream(am.open("road.txt"));
			byte[] contents = new byte[1024];
            int bytesRead=0;
            String str;
            StringBuffer content = new StringBuffer();
            while( (bytesRead = in.read(contents)) != -1){ 
                str = new String(contents, 0, bytesRead);
                content.append(str);
            }
            parseRoadContent(content.toString(), context);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void parseRoadContent(String content, final Context context){
		Document doc = loadXml(content);
		NodeList nodeList = doc.getElementsByTagName("roads");
		Element element = null;
		for(int i = 0; i < nodeList.getLength(); i++){
			element = (Element) nodeList.item(i);
			this.add(getRoads(element));
		}
	}
	
	private Road getRoads(Element e) {
		Road roads = new Road(); 
		roads.setName(e.getElementsByTagName("name").item(0).getTextContent());
		
		NodeList subNodeList = e.getElementsByTagName("road");
		Element element = null;
		List<Route> roadList = new ArrayList<Route>();
		Route road;
		for(int i = 0; i < subNodeList.getLength(); i++){
			element = (Element) subNodeList.item(i);
			road = new Route();
			road.setName(element.getElementsByTagName("name").item(0).getTextContent());
			road.setStop(element.getElementsByTagName("stop_no").item(0).getTextContent());
			roadList.add(road);
		}
		roads.setRoutes(roadList);
		return roads;
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
	
}
