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

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class RouteStop implements Parcelable{
	private String stopSn;
	private String stopCode;
	private String stopDesc;
	private String roadDesc;
	private String longitude;
	private String latitude;
	private String diversion;
	private String delay;
	private List<ComingTime> comingTimes;
	
	public RouteStop() {}
	public RouteStop(Parcel source) {
		stopSn = source.readString();
		stopCode = source.readString();
		stopDesc = source.readString();
		roadDesc = source.readString();
		longitude = source.readString();
		latitude = source.readString();
		diversion = source.readString();
		delay = source.readString();
		this.comingTimes = new ArrayList<ComingTime>();
		source.readList(comingTimes, getClass().getClassLoader());
	}

	public String getStopSn() {
		return stopSn;
	}
	public void setStopSn(String stopSn) {
		this.stopSn = stopSn;
	}
	public String getStopCode() {
		return stopCode;
	}
	public void setStopCode(String stopCode) {
		this.stopCode = stopCode;
	}
	public String getStopDesc() {
		return stopDesc;
	}
	public void setStopDesc(String stopDesc) {
		this.stopDesc = stopDesc;
	}
	public String getRoadDesc() {
		return roadDesc;
	}
	public void setRoadDesc(String roadDesc) {
		this.roadDesc = roadDesc;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getDiversion() {
		return diversion;
	}
	public void setDiversion(String diversion) {
		this.diversion = diversion;
	}
	public String getDelay() {
		return delay;
	}
	public void setDelay(String delay) {
		this.delay = delay;
	}
	public List<ComingTime> getComingTimes() {
		return comingTimes;
	}
	public void setComingTimes(List<ComingTime> comingTimes) {
		this.comingTimes = comingTimes;
	}
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(stopSn);
		dest.writeString(stopCode);
		dest.writeString(stopDesc);
		dest.writeString(roadDesc);
		dest.writeString(longitude);
		dest.writeString(latitude);
		dest.writeString(diversion);
		dest.writeString(delay);
		dest.writeList(comingTimes);
	}
	
	public static final Parcelable.Creator<RouteStop> CREATOR = new Parcelable.Creator<RouteStop>() {

		@Override
		public RouteStop createFromParcel(Parcel source) {
			return new RouteStop(source);
		}

		@Override
		public RouteStop[] newArray(int size) {
			return new RouteStop[size];
		}
	};
	
	public void parseXMLData(String xml){
		if(comingTimes == null){
			comingTimes = new ArrayList<ComingTime>();
		}
		Document doc = loadXml(xml);
		if(doc.getElementsByTagName("result") == null) return;
		NodeList nodeList = doc.getElementsByTagName("result");
		Element element = null;
		for(int i = 0; i < nodeList.getLength(); i++){
			element = (Element) nodeList.item(i);
			getComingTime(element);
		}
    }
    
    public void getComingTime(Element element){	
		ComingTime time = new ComingTime();
		time.setServiceNo(element.getElementsByTagName("service_no").item(0).getTextContent());
		time.setNextBus(element.getElementsByTagName("nextbus").item(0).getFirstChild().getTextContent());
		time.setSubSequentbus(element.getElementsByTagName("subsequentbus").item(0).getFirstChild().getTextContent());
		comingTimes.add(time);
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
