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

public class BuService implements Parcelable{
	private String serviceNo;
	private String direction;
	private String towardStopCode;
	private String towardStopDesc;
	private String towardRoadDesc;
	private List<RouteStop> routeStops;
	
	public BuService() {
		routeStops = new ArrayList<RouteStop>();
	}
 	public BuService(Parcel source) {
 		serviceNo = source.readString();
 		direction = source.readString();
 		towardStopCode = source.readString();
 		towardStopDesc = source.readString();
 		towardRoadDesc = source.readString();
 		this.routeStops = new ArrayList<RouteStop>();
		source.readList(routeStops, getClass().getClassLoader());
 	}

	public String getServiceNo() {
		return serviceNo;
	}
	public void setServiceNo(String serviceNo) {
		this.serviceNo = serviceNo;
	}
	public String getTowardStopCode() {
		return towardStopCode;
	}
	public void setTowardStopCode(String towardStopCode) {
		this.towardStopCode = towardStopCode;
	}
	public String getTowardStopDesc() {
		return towardStopDesc;
	}
	public void setTowardStopDesc(String towardStopDesc) {
		this.towardStopDesc = towardStopDesc;
	}
	public String getTowardRoadDesc() {
		return towardRoadDesc;
	}
	public void setTowardRoadDesc(String towardRoadDesc) {
		this.towardRoadDesc = towardRoadDesc;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public List<RouteStop> getRouteStops() {
		return routeStops;
	}
	public void setRouteStops(List<RouteStop> routeStops) {
		this.routeStops = routeStops;
	}
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(serviceNo);
		dest.writeString(direction);
		dest.writeString(towardStopCode);
		dest.writeString(towardStopDesc);
		dest.writeString(towardRoadDesc);
		dest.writeList(routeStops);
	}
	
	public static final Parcelable.Creator<BuService> CREATOR = new Parcelable.Creator<BuService>() {
        public BuService createFromParcel(Parcel in) {
            return new BuService(in);
        }

        public BuService[] newArray(int size) {
            return new BuService[size];
        }
    };
    
    public void parseXMLData(String xml){
		Document doc = loadXml(xml);
		if(doc.getElementsByTagName("stop") == null) return;
		NodeList nodeList = doc.getElementsByTagName("stop");
		if(nodeList == null) return;
		Element element = null;
		for(int i = 0; i < nodeList.getLength(); i++){
			element = (Element) nodeList.item(i);
			getRouteStop(element);
		}
    }
    
    public void getRouteStop(Element element){	
    	RouteStop routeStop = new RouteStop();
    	routeStop.setStopSn(element.getElementsByTagName("stop_sn").item(0).getTextContent());
    	routeStop.setStopCode(element.getElementsByTagName("stop_code").item(0).getTextContent());
    	routeStop.setStopDesc(element.getElementsByTagName("stop_desc").item(0).getTextContent());
    	routeStop.setRoadDesc(element.getElementsByTagName("road_desc").item(0).getTextContent());
    	routeStop.setLongitude(element.getElementsByTagName("longitude").item(0).getTextContent());
    	routeStop.setLatitude(element.getElementsByTagName("latitude").item(0).getTextContent());
    	routeStop.setDiversion(element.getElementsByTagName("diversion").item(0).getTextContent());
    	routeStop.setDelay(element.getElementsByTagName("delay").item(0).getTextContent());
		this.routeStops.add(routeStop);
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
