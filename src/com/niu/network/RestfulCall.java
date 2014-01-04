package com.niu.network;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Iterator;
import com.niu.tools.Constants;
import android.os.Bundle;
import android.util.Log;
 
public class RestfulCall {
	private final static String TAG = "RestfulCall";
	
	private static RestfulCall instance = null;
	
	public synchronized static RestfulCall getInstance(){
		if(instance == null){
			instance = new RestfulCall();
		}
		return instance;
	}
	
	public static RestfulCall getNewInstance(){
		return new RestfulCall();
	}
	
    public void request(String hostUrl, Bundle params, final String method, final RequestListener listener){
    	synchronized (this) {
    		final String url = getParamString(hostUrl, params);
        	Log.v(TAG, "Request url ---> " + url);
        	new Thread() {
                @Override 
                public void run() {
                	try {
    					String resp = request(url, method);
    					listener.onComplete(resp, null);
    				} catch (IllegalStateException e) {
    					listener.onIllegalStateException(e, null);
    				} catch (IOException e) {
    					listener.onIOException(e, null);
    				}
                }
            }.start();
		}
    }
    
    
    
    private String request(String url, String method) throws IllegalStateException, SocketTimeoutException, IOException{
         HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
         conn.setConnectTimeout(1000*10);
         String response = read(conn.getInputStream());
         return response;
    }
    
    public String getParamString(String hostUrl, Bundle params){
	    StringBuilder sb = new StringBuilder(hostUrl);
	    String key = null;
	    Iterator<String> iterator = params.keySet().iterator();
	    while(iterator.hasNext()){
		    key = iterator.next();
		    sb.append(key + "=" + params.getString(key)).append("&");
	    }
	    return sb.toString().substring(0, sb.toString().length() - 1);
    }
    
    public String read(InputStream in) throws IOException{
    	StringBuilder sb = new StringBuilder();
    	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
           sb.append(line);
           
        }
        bufferedReader.close();
        in.close();
        return sb.toString();
    }
    
}
