package com.niu.sgbus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

public class SGBusDialog extends DialogFragment{
	private final String TAG = "SGBusDialog";
	
	public static SGBusDialog getInstance(String title, String message){
		SGBusDialog audioDialog = new SGBusDialog();
	    Bundle args = new Bundle();
	    args.putString("title", title);
	    args.putString("msg", message);
	    audioDialog.setArguments(args);
	    return audioDialog;
	} 
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	ContextThemeWrapper context = new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        return new AlertDialog.Builder(context)
                .setTitle(getArguments().getString("title"))
                .setMessage(getArguments().getString("msg"))
                .setNegativeButton("Cancel", null).create();
    }

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

}
