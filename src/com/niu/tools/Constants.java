package com.niu.tools;

public class Constants {
	public static final String ALPHABETS = "ZABCDEFGHIJKLMNOPQRSTUVWXY";
	public static final String ONEMAP_TOKEN = "Y4NHiFBpvs0y+9U7wkUf72v0eCg/AAnams7LCyBJjQt64zSZlzszhvyaa/XmqXKOjHTYLA7HuxcLmOtyQKl/0w==|3Aq1GbPZzAY=";
	
	public static final String APIBASE_URL = "http://www.sbstransit.com.sg/iris_api";
	public static final String PATH_SERVICES = "/svc_list_with_dir.aspx?svc_no=sbst";
	public static final String PATH_MRT = "/mrt_list.aspx";
	public static final String PATH_SVC_STOPS = "/svc_stop_list.aspx";
	public static final String PATH_NEXTBUS = "/nextbus.aspx";
	public static final String PATH_NEARBY = "/busstop_search.aspx";
	public static final String PATH_SERVICES_BY_STOP = "/busstop_svc_list.aspx";
	
	public static final String PATH_ADDRESS = "http://www.onemap.sg/API/services.svc/basicSearch";
	public static final String PATH_BUS_STOP_BY_MRT = "/busstop_search_by_mrt.aspx";
	public static final String PATH_JOURNEY_PLANNER = "/jp.aspx";
	public static final String PATH_REQUEST_TOKEN = "/index_reg.aspx";
	public static final String PATH_SVC_FREQ = "/svc_info.aspx";
	
	public static final String[] IRIS_KEY = new String[]{"8A55A731-5940-40D5-8560-D223A819D4EC", 
		"EE912D0C-8DF4-4949-BF50-8E3F79A93EEC", 
		"74D27598-57B8-4BBF-9FA0-F24B156451D9",
		"7F711EAF-DD3C-4E88-8563-68DFA6727F2A&IP=242.181.239.255&UDID=353845052375111&OS=android"};
	
	public static final String DB_NAME = "sbs_db";
	public static final String SBS_TABLE = "sbs";
	public static final class busTableColumns{
		public final static String id = "id";
		public final static String stop = "stop";
		public final static String bus = "bus";
		public final static String lat = "lat";
		public final static String lng = "lng";
		public final static String road = "road";
		public final static String confictNum = "conflict_num";//bus number + stop number
		public final static String stopDesc = "stopdesc";
	}
	
	public static final String SVC_TABLE = "service";
	public static final class SVCTableColumns{
		public final static String id = "id";
		public final static String svc_no = "svc";
		public final static String direction = "direction";
		public final static String towardStopCode = "twd_stop_code";
		public final static String towardStopDesc = "twd_stop_desc";
		public final static String towardRoadDesc = "twd_road_desc";
	}
}
/**
 * 获取所有服务
 * http://www.sbstransit.com.sg/iris_api/svc_list_with_dir.aspx?svc_no=sbst&iriskey=8A55A731-5940-40D5-8560-D223A819D4EC
 * 获取下一个15路车的时间
 * http://www.sbstransit.com.sg/iris_api/nextbus.aspx?busstop=83299&svc=15&iriskey=8A55A731-5940-40D5-8560-D223A819D4EC 
 * 获取MRT列表
 * http://www.sbstransit.com.sg/iris_api/mrt_list.aspx?iriskey=8A55A731-5940-40D5-8560-D223A819D4EC
 * 路线Planner
 * http://www.sbstransit.com.sg/iris_api/busstop_search.aspx?lon=103.90651673333332&lat=1.3159980333333332&radius=0.6&iriskey=8A55A731-5940-40D5-8560-D223A819D4EC&road_desc=Search+End+Address
 * 获取53路车的路线
 * http://www.sbstransit.com.sg/iris_api/svc_stop_list.aspx?svc=053&dir=1&iriskey=8A55A731-5940-40D5-8560-D223A819D4EC
 * 获取巴士站的所有巴士号码
 * http://www.sbstransit.com.sg/iris_api/busstop_svc_list.aspx?stop=83299&iriskey=8A55A731-5940-40D5-8560-D223A819D4EC
 * 搜索巴士站
 * http://www.sbstransit.com.sg/iris_api/busstop_search.aspx?lon=103.84709023572913&lat=1.3566269769420882&radius=0.6&iriskey=8A55A731-5940-40D5-8560-D223A819D4EC&road_desc=BISHAN+ACTIVE+PARK
 * OneMap API 
 * http://www.onemap.sg/API/services.svc/basicSearch?token=Fi4baX936MLgTyiXnaoNMqzm7J7Ez2Vc%2BZGqTFUkTorIHwIvKD0iM4r3yj8ZCv8O1bWO4zDxyjfIWUMY5VAp8Ub%2Fl9tQHQ64%7Cmv73ZvjFcSo%3D&searchVal=bishan&otptFlds=SEARCHVAL,CATEGORY&returnGeom=1&rset=1
 * SGBUSES API
 * http://www.sbstransit.com.sg/open_api/nextbus.aspx?busstop=83149&svc=033&iriskey=7F711EAF-DD3C-4E88-8563-68DFA6727F2A&IP=242.181.239.255&UDID=353845052375111&OS=android
 * Webservice
 * public static final String URL_SMRT_WEBSVC = "http://122.248.253.165/SMRTWebServices/UserWebService.asmx";
 * 获取road搜索
 * http://www.onemap.sg/API/services.svc/basicSearch?token=Fi4baX936MLgTyiXnaoNMqzm7J7Ez2Vc%2BZGqTFUkTorIHwIvKD0iM4r3yj8ZCv8O1bWO4zDxyjfIWUMY5VAp8Ub%2Fl9tQHQ64%7Cmv73ZvjFcSo%3D&searchVal=temasek&otptFlds=SEARCHVAL,CATEGORY&returnGeom=1&rset=1
 * 根据路名搜索车站
 * http://www.sbstransit.com.sg/iris_api/busstop_search.aspx?lon=103.79667687214584&lat=1.2919369034862498&radius=0.6&iriskey=74D27598-57B8-4BBF-9FA0-F24B156451D9&road_desc=TEMASEK+CLUB
 */
