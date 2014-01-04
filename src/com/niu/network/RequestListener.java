package com.niu.network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;


public interface RequestListener {

    public void onComplete(String response, Object state);

    public void onIOException(IOException e, Object state);

    public void onFileNotFoundException(FileNotFoundException e, Object state);

    public void onIllegalStateException(IllegalStateException e, Object state);
}
