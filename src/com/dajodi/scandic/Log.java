package com.dajodi.scandic;

public class Log {

	private static final boolean ENABLED = false;
	private static final String TAG = "Scandic";
	
	public static void d(String msg) {
		if (ENABLED) {
			android.util.Log.d(TAG, msg);
		}
	}
	
	public static void d(String msg, Throwable tr) {
		if (ENABLED) {
			android.util.Log.d(TAG, msg, tr);
		}
	}
	
	public static void w(String msg) {
		if (ENABLED) {
			android.util.Log.w(TAG, msg);
		}
	}
	
	public static void w(String msg, Throwable tr) {
		if (ENABLED) {
			android.util.Log.w(TAG, msg, tr);
		}
	}
	
	public static void i(String msg) {
		if (ENABLED) {
			android.util.Log.i(TAG, msg);
		}
	}
	
}
