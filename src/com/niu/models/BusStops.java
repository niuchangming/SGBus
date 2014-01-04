package com.niu.models;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class BusStops extends ArrayList<BusStop> implements Parcelable{
	private static final long serialVersionUID = 663585476779879096L;
	public BusStops() {}

    public BusStops(Parcel in) {
        this();
        this.clear();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
        	BusStop busStop = new BusStop();
        	busStop.setStopCode(in.readString());
            busStop.setDescription(in.readString());
            busStop.setRoad(in.readString());
            busStop.setLat(in.readDouble());
            busStop.setLng(in.readDouble());
            busStop.setDistance(in.readDouble());
            this.add(busStop);
        }
    }

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		int size = this.size();
        // We have to write the list size, we need him recreating the list
        dest.writeInt(size);

        for (int i = 0; i < size; i++) {
        	BusStop busStop = this.get(i);
        	dest.writeString(busStop.getStopCode());
            dest.writeString(busStop.getDescription());
            dest.writeString(busStop.getRoad());
            dest.writeDouble(busStop.getLat());
            dest.writeDouble(busStop.getLng());
            dest.writeDouble(busStop.getDistance());
        }
	}

	public static final Parcelable.Creator<BusStops> CREATOR = new Parcelable.Creator<BusStops>() {
        public BusStops createFromParcel(Parcel in) {
            return new BusStops(in);
        }

        public BusStops[] newArray(int size) {
            return new BusStops[size];
        }
    };
    
    public void parseXMLData(String xml){
    	this.clear();
		Document doc = loadXml(xml);
		if(doc.getElementsByTagName("stop") == null) return;
		NodeList nodeList = doc.getElementsByTagName("stop");
		Element element = null;
		for(int i = 0; i < nodeList.getLength(); i++){
			element = (Element) nodeList.item(i);
			getBusStop(element);
		}
    }
    
    public void getBusStop(Element element){	
		BusStop busStop = new BusStop();
		busStop.setStopCode(element.getElementsByTagName("stop_code").item(0).getTextContent());
		busStop.setDescription(element.getElementsByTagName("stop_desc").item(0).getTextContent());
		busStop.setRoad(element.getElementsByTagName("road_desc").item(0).getTextContent());
		busStop.setLat(Double.parseDouble(element.getElementsByTagName("latitude").item(0).getTextContent()));
		busStop.setLng(Double.parseDouble(element.getElementsByTagName("longitude").item(0).getTextContent()));
		busStop.setDistance(keep2Digit(Double.parseDouble(element.getElementsByTagName("distance").item(0).getTextContent())));
		this.add(busStop);
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
    
    private double keep2Digit(double f) {
        DecimalFormat df = new DecimalFormat("#.00");
        return Double.parseDouble(df.format(f));
    }
}
