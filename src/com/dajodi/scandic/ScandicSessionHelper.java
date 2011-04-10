package com.dajodi.scandic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.htmlcleaner.DefaultTagProvider;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.ITagInfoProvider;
import org.htmlcleaner.TagInfo;
import org.htmlcleaner.TagNode;

import android.content.Context;

import com.dajodi.scandic.model.MemberInfo;
import com.dajodi.scandic.model.ScandicStay;

/**
 * Helper
 */
public class ScandicSessionHelper {

	

    public static String LOGGED_IN_COOKIE_NAME = "IsLoggedInUser";
    
    // brutal, but might be needed?
    public static Pattern FORM_START = Pattern.compile(".*<form.+name=[\"']aspnetForm[\"'].*");
    public static Pattern FORM_END = Pattern.compile(".*</form>.*");
    
    public static Pattern ACCOUNT_DIV_START = Pattern.compile(".*<!--\\s*AccountOverview\\s*-->.*");
    public static Pattern ACCOUNT_DIV_END = Pattern.compile(".*<!--\\s*/AccountOverview\\s*-->.*");
    
    /**
     * Determines if the logged in cookie exists.  If this is the case, there's no
     * need to re-login.
     *
     * @return
     */
    public static boolean isLoggedIn() {
    	DefaultHttpClient client = Singleton.INSTANCE.getHttpClient();
    	boolean loggedIn = false;
    	for (Cookie cookie : ((AbstractHttpClient) client).getCookieStore().getCookies()) {
            if (LOGGED_IN_COOKIE_NAME.equals(cookie.getName()) &&
            		Boolean.TRUE.toString().equalsIgnoreCase(cookie.getValue())) {
                loggedIn = true;
                break;
            }
        }
    	return loggedIn;
    }
    
    private static InputStream minimizeFormInput(InputStream formInputStream, Pattern startPattern, Pattern endPattern) throws IOException {
    	
    	long before = System.currentTimeMillis();
    	
    	StringBuilder sb = new StringBuilder();
    	
    	// read it per line
    	BufferedReader br = new BufferedReader(new InputStreamReader(formInputStream));
    	
    	boolean startMarkerFound = false;
    	
    	String line;
    	while ((line = br.readLine()) != null) {
    		if (!startMarkerFound && startPattern.matcher(line).matches()) {
    			startMarkerFound = true;
    		} 
    		
    		if (startMarkerFound) {
    			sb.append(line.trim());
    			
    			if (endPattern.matcher(line).matches()) {
    				break;
    			}
    		}
    	}
    	
    	formInputStream.close();
    	
    	InputStream in = new ByteArrayInputStream(sb.toString().getBytes());
    	Log.d("Minimize buffer took: " + (System.currentTimeMillis() - before) + "ms");
    	return in;
    }
    
    public static InputStream get(URI uri) {

    	DefaultHttpClient client = Singleton.INSTANCE.getHttpClient();
        try {

            HttpGet get = new HttpGet(uri);

            Util.gzipify(get);
            
            final HttpParams params = new BasicHttpParams();
            HttpClientParams.setRedirecting(params, false);
            get.setParams(params);

            Log.d("Executing get");

            // should give us a 302
            HttpResponse response = client.execute(get);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new ScandicHtmlException("Expected a 200, got " + response.getStatusLine());
            }

            InputStream instream = Util.ungzip(response);
            return instream;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private static Map<String, String> getFormInputFields(URI uri) throws IOException {
    	InputStream in = get(uri);
    	
    	// should we try to minimize the input stream?
    	in = minimizeFormInput(in, FORM_START, FORM_END);
    	
    	try {
    		long before = System.currentTimeMillis();
    		Map<String,String> result = Singleton.INSTANCE.getScraper().scrapeFormInputFields(in);
    		Log.d("Scrape member info took " + (System.currentTimeMillis() - before) + "ms");
    		return result;
    	} finally {
    		in.close();
    	}
    }

    private static void login(String username, String password) throws Exception {

    	DefaultHttpClient client = Singleton.INSTANCE.getHttpClient();

    	Map<String,String> inputFields = getFormInputFields(new URI("http://www.scandichotels.com/settings/Side-foot/About-us-Container/About-Us/"));

        inputFields.put("ctl00$MenuLoginStatus$txtLoyaltyUsername", username);
        inputFields.put("ctl00$MenuLoginStatus$txtLoyaltyPassword", password);
        inputFields.put("__EVENTTARGET", "ctl00$MenuLoginStatus$btnLogIn");
        inputFields.put("ctl00$MenuLoginStatus$loginPopUpPageID", "LOGIN_POPUP_MODULE");

        // to remove:
        Set<String> toRemove = new HashSet<String>();
        toRemove.add("ctl00$MenuLoginStatus$RememberUserIdPwd");
        toRemove.add("ctl00$SecondaryBodyRegion$ctl00$BE$rememberMe");
        toRemove.add("ctl00$MenuLoginStatus$RememberUserIdPwd");

        List<NameValuePair> nvps = new LinkedList<NameValuePair>();
        for (String key : inputFields.keySet()) {

            if (!toRemove.contains(key)) {
                String name = key;
                String value = inputFields.get(key);

                nvps.add(new BasicNameValuePair(name, value == null ? "" : value));

                // useful, but onl locally
//                Log.d("Entry: " + name + "=" + (value == null ? "" : value));
            }
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvps);

        // now the post
        HttpPost post = new HttpPost("https://www.scandichotels.com/templates/Booking/Units/LoginValidator.aspx");

        post.setHeader("Content-Type", "application/x-www-form-urlencoded");

        // needed, we don't want redirecting here
        final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        post.setParams(params);
        
        // not really needed, but why not
        Util.gzipify(post);
        
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        post.abort();

        if (isLoggedIn()) {
        	Log.d("Success!  Logged in via Java code!");
        } else if (response.getStatusLine().getStatusCode() == 302 &&
                response.getFirstHeader("Location").getValue().contains("Login-Error")) {
            throw new InvalidLoginException();

        } else {
        	throw new RuntimeException("Could not login!");
        }
    }
    
    public static void clearSession() {
    	Singleton.INSTANCE.getHttpClient().getCookieStore().clear();
    }

    public static MemberInfo fetchInfo(String username, String password) throws Exception {
    	DefaultHttpClient client = Singleton.INSTANCE.getHttpClient();

    	if (!isLoggedIn()) {
    		// clear the cookies just to be safe
    		client.getCookieStore().clear();
    		login(username, password);
    	}

        URI uri = new URI("https://www.scandichotels.com/Frequent-Guest-Programme/");

        HttpGet get = new HttpGet(uri);
        Util.gzipify(get);

        // no redirect please
        final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(params, false);
        get.setParams(params);

        HttpResponse response = client.execute(get);

        response = checkSessionStillValid(uri, get, response, username, password);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new ScandicHtmlException("Non-200 response returned for frequent guest page");
        }

        InputStream instream = Util.ungzip(response);
        
    	// should we try to minimize the input stream?
        instream = minimizeFormInput(instream, ACCOUNT_DIV_START, ACCOUNT_DIV_END);

        try {
	        long before = System.currentTimeMillis();
	        MemberInfo memberInfo = Singleton.INSTANCE.getScraper().scrapeMemberInfo(instream);
	        Log.d("Scrape member info took " + (System.currentTimeMillis() - before) + "ms");
	        
	        return memberInfo;
        } finally {
        	instream.close();
        }

    }

    private static HttpResponse checkSessionStillValid(URI uri, HttpGet get, HttpResponse response, String username, String password) throws Exception {
        DefaultHttpClient client = Singleton.INSTANCE.getHttpClient();
        if (response.getStatusLine().getStatusCode() == 302 &&
                response.getFirstHeader("Location").getValue().toLowerCase().contains("sessionexpired")) {
            // session seems to have expired...
            get.abort();

            Log.d("Session seems to have expired, clearing cookies, relogging in");
            client.getCookieStore().clear();
            login(username, password);

            Log.d("Trying to get secure page again");
            get = new HttpGet(uri);
            Util.gzipify(get);
            response = client.execute(get);
        }
        // check the headers, we can assume a few exist for cached requests (save issuing a request for 300k)
        else if (!containsNoCacheHeaders(response)) {
            get.abort();

            Log.d("Session seems to have been hijacked, clearing cookies, relogging in");
            client.getCookieStore().clear();
            login(username, password);

            Log.d("Trying to get secure page again");
            get = new HttpGet(uri);
            Util.gzipify(get);
            response = client.execute(get);
        }

        return response;

    }

    public static boolean containsNoCacheHeaders(HttpResponse response) {
        Header[] cacheControlHeaders = response.getHeaders("Cache-Control");
        Header[] pragmaHeaders = response.getHeaders("Pragma");

        boolean foundCacheControlNoCache = false;
        boolean foundPragmaNoCache = false;

        for (Header header : cacheControlHeaders) {
            if ("no-cache".equals(header.getValue().toLowerCase())) {
                foundCacheControlNoCache = true;
                break;
            }
        }

        for (Header header : pragmaHeaders) {
            if ("no-cache".equals(header.getValue().toLowerCase())) {
                foundPragmaNoCache = true;
                break;
            }
        }
        return foundCacheControlNoCache && foundPragmaNoCache;
    }

    
    
}
