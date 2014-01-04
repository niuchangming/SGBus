package com.niu.tools;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper{
	final static String TAG = "DataBaseHelper";
	public static final int DB_VERSION = 1;

	public DataBaseHelper(Context context) {
		super(context, Constants.DB_NAME, null, DB_VERSION);
	}
	
	public DataBaseHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
		
	String sbsSQL = "create table if not exists " + Constants.SBS_TABLE + " ("+
			Constants.busTableColumns.id + " integer primary key autoincrement, "
			+ Constants.busTableColumns.stop+" varchar(10), " 
			+ Constants.busTableColumns.bus+" varchar(10), " 
			+ Constants.busTableColumns.lat + " double, " 
			+ Constants.busTableColumns.lng + " double, " 
			+ Constants.busTableColumns.road + " varchar(100), "
			+ Constants.busTableColumns.confictNum + " varchar(16), "
			+ Constants.busTableColumns.stopDesc + " varchar(100),"
			+ "UNIQUE ("+ Constants.busTableColumns.confictNum + ") ON CONFLICT REPLACE" + "); ";
	
	String allSrvSQL = "create table if not exists " + Constants.SVC_TABLE + " ("+
			Constants.SVCTableColumns.id + " integer primary key autoincrement, "
			+ Constants.SVCTableColumns.svc_no+" varchar(5), " 
			+ Constants.SVCTableColumns.direction+" integer(1), " 
			+ Constants.SVCTableColumns.towardStopCode + " varchar(10), " 
			+ Constants.SVCTableColumns.towardStopDesc + " text, " 
			+ Constants.SVCTableColumns.towardRoadDesc + " text(100));";
	
	@Override
	public synchronized void onCreate(SQLiteDatabase db) {
		try{
			db.execSQL(sbsSQL);
			db.execSQL(allSrvSQL);
		}catch(SQLException e){
			Log.v(TAG, "Error ---> " + e.toString());
		}
	}

	@Override
	public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + Constants.SBS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + Constants.SVC_TABLE);
		onCreate(db);
	}
	
	public void reCreateTable(String table){
		synchronized(this){
			SQLiteDatabase db = this.getWritableDatabase();
			String dropTable = "DROP TABLE IF EXISTS " + table + ";";
			try{
				db.execSQL(dropTable);
				db.execSQL(sbsSQL);
			}catch(SQLException e){
				System.out.println(e.toString());
			}finally{
				if(db.isOpen()){
					db.close();
				}
			}
		}
	}
}