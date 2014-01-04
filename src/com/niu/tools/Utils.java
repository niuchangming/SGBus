package com.niu.tools;

import java.util.Random;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {
	
	public static boolean isConnected(Context context){
		ConnectivityManager conManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conManager.getActiveNetworkInfo();

        if (networkInfo != null ){ 
            return networkInfo.isAvailable();
        }
        return false; 
	}
	
	public static String removePreZero(String str){
		return str.replaceFirst("^0+(?!$)", "");
	}
	
	public static String getRandomIrisKey(){
		Random rand = new Random();
		int randomNum = rand.nextInt(Constants.IRIS_KEY.length);
		return Constants.IRIS_KEY[randomNum];
	}
}
