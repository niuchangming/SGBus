package com.niu.models;

import java.io.IOException;
import java.io.StringReader;
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

public class Buses extends ArrayList<Bus> implements Parcelable{
	private static final long serialVersionUID = 663585476779879097L;
	public Buses() {}

    public Buses(Parcel in) {
        this();
        this.clear();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
        	Bus bus = new Bus();
        	bus.setServiceNo(in.readString());
        	bus.setDirection(in.readInt());
        	bus.setDiversion(in.readString());
        	bus.setDelay(in.readString());
            this.add(bus);
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
        	Bus bus = this.get(i);
        	dest.writeString(bus.getServiceNo());
            dest.writeInt(bus.getDirection());
            dest.writeString(bus.getDiversion());
            dest.writeString(bus.getDelay());
        }
	}
	
	public static final Parcelable.Creator<Buses> CREATOR = new Parcelable.Creator<Buses>() {
        public Buses createFromParcel(Parcel in) {
            return new Buses(in);
        }

        public Buses[] newArray(int size) {
            return new Buses[size];
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
			getBus(element);
		}
    }
    
    public void getBus(Element element){	
		Bus bus = new Bus();
		bus.setServiceNo(element.getElementsByTagName("service_no").item(0).getTextContent());
		bus.setDirection(Integer.parseInt(element.getElementsByTagName("direction").item(0).getTextContent()));
		bus.setDiversion(element.getElementsByTagName("diversion").item(0).getTextContent());
		bus.setDelay(element.getElementsByTagName("delay").item(0).getTextContent());
		this.add(bus);
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







