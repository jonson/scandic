package com.dajodi.scandic;

import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import com.dajodi.scandic.model.MemberInfo;

/**
 * Helper
 */
public class ScandicSessionHelper {

	
	public static String SESSION_ID_COOKIE_NAME = "ASP.NET_SessionId";
    public static String LOGGED_IN_COOKIE_NAME = "IsLoggedInUser";

    
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

   

    private static void login(String username, String password) throws Exception {

    	DefaultHttpClient client = Singleton.INSTANCE.getHttpClient();
    	client.getCookieStore().clear();

    	HttpHead head = new HttpHead("https://www.scandichotels.com/Frequent-Guest-Programme/");
    	
    	// only for user-agent
    	Util.gzipify(head);
    	
    	HttpResponse response = client.execute(head);
    	
    	if (response.getStatusLine().getStatusCode() != 200) {
    		throw new ScandicHtmlException("HEAD request to FG page did not return a 200, instead " + response.getStatusLine().getStatusCode());
    	}
    	head.abort();
    	
    	boolean found = false;
    	// assume this cookie exists
    	for (Cookie cookie : client.getCookieStore().getCookies()) {
    		if (SESSION_ID_COOKIE_NAME.equals(cookie.getName())) {
    			found = true;
    			break;
    		}
    	}
    	if (!found) {
    		throw new ScandicHtmlException("Session id cookie not valid from head request, dying");
    	}
    	
    	
    	List<NameValuePair> nvps = new LinkedList<NameValuePair>();
    	nvps.add(new BasicNameValuePair("ctl00$MenuLoginStatus$txtLoyaltyUsername", username));
    	nvps.add(new BasicNameValuePair("ctl00$MenuLoginStatus$txtLoyaltyPassword", password));
    	nvps.add(new BasicNameValuePair("ctl00$MenuLoginStatus$loginPopUpID", "LOGIN_POPUP_MODULE"));
    	nvps.add(new BasicNameValuePair("ctl00$MenuLoginStatus$loginPopUpPageID", "LOGIN_POPUP_MODULE"));
    	nvps.add(new BasicNameValuePair("__PREVIOUSPAGE", ""));
    	nvps.add(new BasicNameValuePair("__EVENTTARGET", "ctl00$MenuLoginStatus$btnLogIn"));

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
        response = client.execute(post);
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
//        instream = minimizeFormInput(instream, ACCOUNT_DIV_START, ACCOUNT_DIV_END);

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
