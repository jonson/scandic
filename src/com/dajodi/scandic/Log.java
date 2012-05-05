/*
 * Copyright 2012 - Jon DeYoung
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
