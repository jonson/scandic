package com.dajodi.scandic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.dajodi.scandic.model.MemberInfo;
import com.dajodi.scandic.model.PersistedData;
import com.dajodi.scandic.user.UsernamePassword;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class Util {

	private static String JSON_FILENAME = "memberinfo.json";
	private static String USERNAME_KEY = "user";
	private static String PASSWORD_KEY = "pass";
	
	public static int UNKNOWN_NIGHTS = -2;
	public static int UNKNOWN_POINTS = -3;
	public static int NO_NIGHTS = -4;
    private static final Pattern NIGHTS_PATTERN = Pattern.compile("(\\d+).*");

	
	/**
	 * Reads the username/password from.
	 * 
	 * @param context
	 * @return
	 */
	public static UsernamePassword read(Context context) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    
        String username = prefs.getString(USERNAME_KEY, null);
        String password = prefs.getString(PASSWORD_KEY, null);
        
        if (username == null || password == null || (password.length() == 0) ) {
        	return null;
        }
         
        return new UsernamePassword(username, password);
	}
	
	public static void write(Context context, String username, String password) {
		
		if (username == null || password == null || (password.length() == 0) ) {
			return;
		}
		
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString(USERNAME_KEY, username);
		editor.putString(PASSWORD_KEY, password);
		editor.commit();
	}
	
	public static void gzipify(HttpRequest request) {
		request.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13");
		request.addHeader("Accept-Encoding", "gzip");
	}
	
	public static InputStream ungzip(HttpResponse response) {
		try {
			InputStream instream = response.getEntity().getContent();
			Header contentEncoding = response.getFirstHeader("Content-Encoding");
			
			if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
			    instream = new GZIPInputStream(instream);
			}
			return instream;
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException)e;
			}
			throw new RuntimeException(e);
		}
	}
	
	public static void writeMemberInfo(Context context, MemberInfo memberInfo) throws IOException {

		PersistedData data = new PersistedData();
		data.setMemberInfo(memberInfo);
		
		File f = context.getFilesDir();
		Writer writer = new BufferedWriter( new FileWriter( new File( f, JSON_FILENAME) ) );
		try {
		     new JSONSerializer().deepSerialize(data, writer);
		     writer.flush();
		 } finally {
		     writer.close();
		 }
	}
	
	public static MemberInfo readMemberInfo(Context context) throws FileNotFoundException {
		
		File f = new File(context.getFilesDir(), JSON_FILENAME);
		if (!f.exists()) {
			return null;
		}
		
		Reader reader = new BufferedReader(new FileReader(f));
		PersistedData data = new JSONDeserializer<PersistedData>().deserialize(reader, PersistedData.class);
		
		return data == null ? null : data.getMemberInfo();
	}
	
	public static boolean usernameValid(String username) {
		if (username == null || username.length() == 0) {
			return false;
		}
		return true;
	}
	
	public static boolean passwordValid(String password) {
		if (password == null || password.length() == 0) {
			return false;
		}
		return true;
	}
	
	public static void printMemory() {
		Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long totalFreeMemory = (freeMemory + (maxMemory - allocatedMemory)) / 1024;
        Log.d("free memory: " + freeMemory / 1024);
        Log.d("allocated memory: " + allocatedMemory / 1024);
        Log.d("max memory: " + maxMemory /1024);
        Log.d("total free memory: " + totalFreeMemory
           );
	}
	
    public static String trimIfNonNull(String string) {
        if (string == null) {
            return string;
        }

        return string.trim();
    }
    
    public static int daysBetween(Date before, Date after) {

    	if (before == null || after == null) {
    		return UNKNOWN_NIGHTS;
    	}

        if (before.after(after)) {
            return UNKNOWN_NIGHTS;
        }

        // Creates two calendars instances
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(before);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(after);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        long diffDays = UNKNOWN_NIGHTS;
        if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
        	diffDays = Math.abs(day2 - day1);
        }

        // Calculate difference in days
        return (int) diffDays;
    }
    
    public static Date[] parseDates(String dateString) {

        String[] split = dateString.split("-");

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

        Date[] dates;
        Date fromDate = null;
        Date toDate = null;
        try {
            fromDate = split.length > 0 ? format.parse(split[0].trim()) : null;
            toDate = split.length > 1 ?  format.parse(split[1].trim()) : null;
            dates = new Date[]{fromDate, toDate};
        } catch (ParseException e) {
            // hmmmm...
            dates = new Date[] {fromDate != null ? fromDate : null, null};
        }

        return dates;
    }
    
    public static int parseNumNights(String nights) {
        Matcher matcher = NIGHTS_PATTERN.matcher(nights);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }
    
    public static int parseInt(String str, int defaultValue) {
    	try {
    		return Integer.parseInt(str);
    	} catch (Exception e) {
    		return defaultValue;
    	}
    }
	
}
