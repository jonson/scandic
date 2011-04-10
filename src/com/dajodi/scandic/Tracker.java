package com.dajodi.scandic;

import android.content.Context;

import com.dajodi.scandic.model.UpdateSource;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public final class Tracker {
	
	
	private static final String CATEGORY_UPDATE = "Update";
	private static final String ACTION_SOURCE = "Source";
	private static final String ACTION_DURATION = "Duration";
	
	private static final String CATEGORY_ERROR = "Error";
	private static final String ACTION_LOGIN = "Login";
	private static final String ACTION_UNKNOWN = "Unknown";
	
	private static final String MAIN_ACTIVITY = "/HomeScreen";
	
	private final boolean production;
	private final GoogleAnalyticsTracker tracker;
	
	public Tracker(boolean production) {
		this.production = production;
		
		if (production) {
			tracker = GoogleAnalyticsTracker.getInstance();
		} else {
			tracker = null;
		}
	}
	
	/**
	 * Should be called in the main activity only once 
	 * 
	 * @param context
	 */
	public void startTracking(Context context) {
		// analytics tracking
		if (production) {
			this.tracker.start("UA-22046368-2", 30, context);
		}
	}
	
	public void stopTracking() {
		if (production) {
			this.tracker.stop();
		}
	}
	
	public void trackLoginError() {
		trackEvent(CATEGORY_ERROR, ACTION_LOGIN, "a", 0);
	}
	
	public void trackUnknownError() {
		trackEvent(CATEGORY_ERROR, ACTION_UNKNOWN, "a", 0);
	}
	
	public void trackUpdateDuration(int milliseconds) {
		trackEvent(CATEGORY_UPDATE, ACTION_DURATION, "a", milliseconds);
	}
	
	public void trackUpdateSource(UpdateSource source) {
		trackEvent(CATEGORY_UPDATE, ACTION_SOURCE, source.toString(), 0);
	}
	
	public void trackMainActivityView() {
		trackPageView(MAIN_ACTIVITY);
	}
	
	private void trackEvent(String category, String action, String label, int value) {
		if (production) {
			this.tracker.trackEvent(category, action, label, value);
		}
	}
	
	private void trackPageView(String page) {
		if (production) {
			this.tracker.trackPageView(page);
		}
	}

}
