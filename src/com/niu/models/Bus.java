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

public class Bus implements Parcelable{
	private String serviceNo;
	private int direction;
	private String diversion;
	private String delay;
	private List<ComingTime> comingTime;
	
	public Bus() {}
	public Bus(Parcel source) {
		serviceNo = source.readString();
		direction = source.readInt();
		diversion = source.readString();
		delay = source.readString();
		this.comingTime = new ArrayList<ComingTime>();
		source.readList(comingTime, getClass().getClassLoader());
	}

	public String getServiceNo() {
		return serviceNo;
	}
	public void setServiceNo(String serviceNo) {
		this.serviceNo = serviceNo;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
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
	public List<ComingTime> getComingTime() {
		return comingTime;
	}
	public void setComingTime(List<ComingTime> comingTime) {
		this.comingTime = comingTime;
	}
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(serviceNo);
		dest.writeInt(direction);
		dest.writeString(diversion);
		dest.writeString(delay);
		dest.writeList(comingTime);
	}
	
	public static final Parcelable.Creator<Bus> CREATOR = new Parcelable.Creator<Bus>() {

		@Override
		public Bus createFromParcel(Parcel source) {
			return new Bus(source);
		}

		@Override
		public Bus[] newArray(int size) {
			return new Bus[size];
		}
	};
	
	public void parseXMLData(String xml){
		comingTime = new ArrayList<ComingTime>();
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
		Log.v("BusListFragment", "BusListFragment ---> " + time.getNextBus());
		comingTime.add(time);
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
