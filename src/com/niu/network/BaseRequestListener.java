package com.niu.network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import android.util.Log;

public abstract class BaseRequestListener implements RequestListener{
	
    public void onFileNotFoundException(FileNotFoundException e,
                                        final Object state) {
        Log.e("WoW-Hotel", e.getMessage());
        e.printStackTrace();
    }

    public void onIOException(IOException e, final Object state) {
        Log.e("WoW-Hotel", e.getMessage());
        e.printStackTrace();
    }

    public void onMalformedURLException(MalformedURLException e,
                                        final Object state) {
        Log.e("WoW-Hotel", e.getMessage());
        e.printStackTrace();
    }
    
    public void onIllegalStateException(IllegalStateException e, Object state){
    	Log.e("WoW-Hotel", e.getMessage());
    	e.printStackTrace();
    }
}
