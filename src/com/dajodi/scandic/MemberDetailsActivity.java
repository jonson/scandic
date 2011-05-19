package com.dajodi.scandic;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.dajodi.scandic.FetchMemberInfoTask.ProgressType;
import com.dajodi.scandic.model.MemberInfo;
import com.dajodi.scandic.model.ScandicStay;
import com.dajodi.scandic.model.UpdateSource;
import com.dajodi.scandic.user.UsernamePassword;

/**
 * Main activity for the application.
 * 
 * Contains 2 views:
 *  * Initial login view
 *  * Member details view
 *  
 *  The initial login view will only be shown on the initial load.  On successful login,
 *  the member details view will be shown.
 *  
 *  If the activity is created and there is a saved username/password, this will be used
 *  to login.
 *  
 *  TODO: handle the case of changing a password
 * 
 * @author jon
 *
 */
public class MemberDetailsActivity extends Activity {
    
	private ViewAnimator viewAnimator;
	private View loginView;
	private View detailsView;
	
	private long lastUpdated = -1;

	// only update once every 30 mins in the background, this data isn't updated that frequently
	// and is quite expensive to update
	// TODO: this could be split 3G vs WIFI
	private static long UPDATE_TIME = 30 * 60 * 1000;
	
	private boolean initialOnCreateCall = true;
	
	private Intent lastIntent = null;
	private Tracker tracker;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.main);
        
        viewAnimator = (ViewAnimator)findViewById(R.id.viewFlipper);
        loginView = createLoginView();
        detailsView = createDetailsView();
        
        viewAnimator.addView(loginView);
        viewAnimator.addView(detailsView);
        
    	restorePersistedData();
    	
    	initialOnCreateCall = false;
    	lastIntent = getIntent();
    	
    	tracker = Singleton.INSTANCE.getTracker();
    	tracker.startTracking(this);
    	
    	// add the refresh listener
    	addProgressIndicator();
    	
    }
    
    /**
     * Sets the indeterminate loading state of a refresh button added with
     * {@link ActivityHelper#addActionButtonCompatFromMenuItem(android.view.MenuItem)}
     * (where the item ID was menu_refresh).
     */
    public void setRefreshActionButtonCompatState(boolean refreshing) {
        View refreshButton = findViewById(R.id.actionBarRefreshButton);
        View refreshIndicator = findViewById(R.id.menu_refresh_progress);

        if (refreshButton != null) {
        	// invisible b/c we're using the width for layout
            refreshButton.setVisibility(refreshing ? View.INVISIBLE : View.VISIBLE);
        }
        if (refreshIndicator != null) {
            refreshIndicator.setVisibility(refreshing ? View.VISIBLE : View.GONE);
        }
    }
    
    private void addProgressIndicator() {
    	// Refresh buttons should be stateful, and allow for indeterminate progress indicators,
        // so add those.
//        int buttonWidth = getResources()
//                .getDimensionPixelSize(R.dimen.actionbar_compat_height);
//        int buttonWidthDiv3 = buttonWidth / 3;
//        ProgressBar indicator = new ProgressBar(this, null,
//                R.attr.actionbarCompatProgressIndicatorStyle);
//        RelativeLayout.LayoutParams indicatorLayoutParams = new RelativeLayout.LayoutParams(
//                buttonWidthDiv3, buttonWidthDiv3);
//        indicatorLayoutParams.setMargins(buttonWidthDiv3, buttonWidthDiv3,
//                buttonWidth - 2 * buttonWidthDiv3, 0);
//        indicator.setLayoutParams(indicatorLayoutParams);
////        indicator.setVisibility(View.GONE);
//        indicator.setId(R.id.menu_refresh_progress);
        
//        ((ViewGroup)findViewById(R.id.actionBar)).addView(indicator);
        
        ImageButton refreshButton = (ImageButton) findViewById(R.id.actionBarRefreshButton);
//        refreshButton.setClickable(true);
        refreshButton.setOnClickListener(ocl);
    }
    
    private OnClickListener ocl = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			attemptLogin();
		}
	};
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	tracker.stopTracking();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	tracker.trackMainActivityView();
    	
    	// if from settings intent
    	boolean fromSettings = lastIntent.getBooleanExtra(FetchMemberInfoTask.FROM_SETTINGS, false);
    	boolean readFromDisk = lastIntent.getBooleanExtra(FetchMemberInfoTask.READ_FROM_DISK, false);

    	if (readFromDisk) {
        	// never true on create, no worries about dupliate call
    		// re-read persisted data (normally only do this on startup)
    		restorePersistedData();
    	} else if (!fromSettings) {
    		// never update if this is from settings activity
    		UsernamePassword userpass = Util.read(this);
        	if (userpass != null) {
            	updateUserPass(userpass);
            	
            	long now = new Date().getTime();
            	Log.d("last update " + ((now - lastUpdated) / 1000 / 60) + " minutes ago");
            	if ( (now - lastUpdated) > UPDATE_TIME) {
            		performLogin(userpass.getUsername(), userpass.getPassword(), UpdateSource.AUTOMATIC);
            	}
            } 
    	}
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    	lastIntent = intent;
    	Log.d("on new intent called");
    }
    
    private void restorePersistedData() {
    	AsyncTask<Void, Void, MemberInfo> result = new FetchPersistedStateTask(this).execute();
    	try {
			MemberInfo mi = result.get();
			updateMemberInfo(mi);
			showDetailsView();
		} catch (Exception e) {
			// no problem, just dont update it
			Log.d("could not load persisted data, nothing to worry about");
		}
    }
    
    private void updateUserPass(UsernamePassword userpass) {
    	((EditText)loginView.findViewById(R.id.txtUsername)).setText(userpass.getUsername());
    	((EditText)loginView.findViewById(R.id.txtPassword)).setText(userpass.getPassword());
    	
    	// focus the button (not working)
//    	Button button = (Button) loginView.findViewById(R.id.button1);
//    	loginView.requestFocus();
    }

	private View createLoginView() {
		
		View v = getLayoutInflater().inflate(R.layout.login_details, null);
		
		// add a progress dialog to the login button
        Button button = (Button) v.findViewById(R.id.loginButton);
        
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doLogin();
			}
		});
        
        return v;
	}
	
	private void doLogin() {
		EditText usernameTxt = (EditText)loginView.findViewById(R.id.txtUsername);
		EditText passwordTxt = (EditText)loginView.findViewById(R.id.txtPassword);
		
		String username = usernameTxt.getText().toString();
		String password = passwordTxt.getText().toString();
		
		// some validation here (membership a number, pw length, ...)
		if (!Util.usernameValid(username)) {
			Toast.makeText(MemberDetailsActivity.this, R.string.login_invalid_user, Toast.LENGTH_SHORT).show();
			usernameTxt.requestFocus();
		} else if (!Util.passwordValid(password)) {
			Toast.makeText(MemberDetailsActivity.this, R.string.login_invalid_password, Toast.LENGTH_SHORT).show();
			passwordTxt.requestFocus();
		} else {
			performLogin(username, password, UpdateSource.LOGIN_BUTTON);
		}
	}
		
	private void performLogin(String username, String password, UpdateSource source) {
		ProgressType type = viewAnimator.getDisplayedChild() == 0 ? ProgressType.DIALOG : ProgressType.TITLE_BAR;
		
		// should validate here
		new FetchMemberInfoTask(MemberDetailsActivity.this, type, source).execute(username, password);
	}
	
	public void showLoginView() {
		
		// hide the refresh stuff
		setRefreshStuffVisible(false);
		
		viewAnimator.setDisplayedChild(0);
	}
	
	public void showDetailsView() {
		
		// show the refresh stuff
		setRefreshStuffVisible(true);
		
		viewAnimator.setDisplayedChild(1);
	}
	
	public void setRefreshStuffVisible(boolean visible) {
		
		findViewById(R.id.actionBarSep1).setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		findViewById(R.id.actionBarRefreshButton).setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		
	}
	
	private View createDetailsView() {
		View v = getLayoutInflater().inflate(R.layout.member_details, null);
		return v;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
	
	/**
	 * No menu on the login screen
	 */
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (viewAnimator.getDisplayedChild() == 0) {
			return false;
		}
		return true;
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
//        case R.id.refresh:
//        	attemptLogin();
//            return true;
        case R.id.settings:
        	Intent intent = new Intent(this, SettingsActivity.class);
        	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	startActivity(intent);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void attemptLogin() {
    	// always update if the item was selected
    	UsernamePassword usernamePassword = Util.read(this);
    	
    	if (Util.usernameValid(usernamePassword.getUsername()) &&
    			Util.passwordValid(usernamePassword.getPassword())) {
    		performLogin(usernamePassword.getUsername(), usernamePassword.getPassword(), UpdateSource.REFRESH_BUTTON);
    	}
    }
	
	
	public void updateMemberInfo(MemberInfo memberInfo) {
		// could fallback to username/password
    	((TextView)detailsView.findViewById(R.id.membershipNumberTxt)).setText(memberInfo.getMembershipId());
    	
    	if (memberInfo.getPoints() >= 0) {
    		((TextView)detailsView.findViewById(R.id.yourPointsTxt)).setText(Integer.toString(memberInfo.getPoints()));
    	} else {
    		((TextView)detailsView.findViewById(R.id.yourPointsTxt)).setText(R.string.details_points_error);
    	}
    	
    	if (memberInfo.getLevel() != null) {
    		((TextView)detailsView.findViewById(R.id.memberLevelTxt2)).setText(memberInfo.getLevel().getResourceId());
    	} else {
    		((TextView)detailsView.findViewById(R.id.memberLevelTxt2)).setText(R.string.details_level_error);
    	}
    	
    	if (memberInfo.getQualifyingNights() == Util.NO_NIGHTS) {
    		String txt = getString(R.string.details_no_transactions);
    		((TextView)detailsView.findViewById(R.id.qualLbl)).setVisibility(View.GONE);
    		((TextView)detailsView.findViewById(R.id.qualTxt)).setText(txt);
    		
    	} else if (memberInfo.getQualifyingNights() == Util.UNKNOWN_NIGHTS) {
    		((TextView)detailsView.findViewById(R.id.qualLbl)).setVisibility(View.GONE);
    		((TextView)detailsView.findViewById(R.id.qualTxt)).setText(R.string.details_unknown_nights);
    	}
    	else {
    		((TextView)detailsView.findViewById(R.id.qualLbl)).setVisibility(View.VISIBLE);    		
    		((TextView)detailsView.findViewById(R.id.qualTxt)).setText(
    				getString(R.string.details_qual_nights, memberInfo.getQualifyingNights()));
    	}

    	LinearLayout vg = (LinearLayout) detailsView.findViewById(R.id.transactionTable);
    	vg.removeAllViews();
    	    	
    	Map<String, List<ScandicStay>> stayMap = staysByMonthMostRecentFirst(memberInfo.getStaysLast12Months());
    	
    	for (String month : stayMap.keySet()) {
    		List<ScandicStay> stays = stayMap.get(month);
    		
    		// long month name + year (December 2010)
    		String label = getMonthYearLabel(stays);
    		
    		// add the header row
    		View header = createHeader(label);
    		vg.addView(header);
    		
    		for (ScandicStay stay : stays) {
    			View row = createStayRow(stay);
    			vg.addView(row);
    		}
    	}
    	
    	TextView lastUpdatedText = (TextView) detailsView.findViewById(R.id.lastUpdatedTxt);
		
    	java.text.DateFormat dateFormat =
		    android.text.format.DateFormat.getDateFormat(this);
		
		java.text.DateFormat timeFormat =
		    android.text.format.DateFormat.getTimeFormat(this);
		
		Date date = memberInfo.getLastUpdated();
		lastUpdatedText.setText(getString(R.string.details_last_updated, dateFormat.format(date), timeFormat.format(date)));
		
		lastUpdated = date.getTime();
    }
	
	/**
	 * Gets the month + year (December 2010) from the list of stays.  All entries in this list of statys
	 * must have the same start month.
	 * 
	 * @param stays
	 * @return
	 */
	private String getMonthYearLabel(List<ScandicStay> stays) {
		
		for (ScandicStay stay : stays) {
			if (stay.getFromDate() != null) {
				return android.text.format.DateFormat.format("MMMM yyyy", stay.getFromDate()).toString();
			}
		}
		
		return getString(R.string.tx_unknown_month);
	}
    
    private View createHeader(String title) {
    	TextView result=(TextView)getLayoutInflater()
		.inflate(R.layout.section_header,
						 null);
    	result.setText(title);
    	return result;
    }
    
    private View createStayRow(ScandicStay stay) {
    	
    	View convertView = null;
    	ViewHolder holder;
		convertView = getLayoutInflater().inflate(R.layout.custom_row, null);
		holder = new ViewHolder();
		holder.txtName = (TextView) convertView.findViewById(R.id.name);
		holder.points = (TextView) convertView.findViewById(R.id.pointsTxt);
		holder.date = (TextView) convertView.findViewById(R.id.dateRange);

		convertView.setTag(holder);

		// what do we want for default hotel name?
		String hotelName = stay.getHotelName();
		if (hotelName == null || hotelName.length() == 0 || "?".equals(hotelName)) {
			hotelName = getString(R.string.tx_unknown_hotel);
		}
		holder.txtName.setText(hotelName);
		
		int numPoints = stay.getNumPoints();
		String numPointsText = null;
		if (numPoints >= 0) {
			numPointsText = getString(R.string.tx_points,  stay.getNumPoints());
		} else {
			numPointsText = "";
		}
		holder.points.setText(numPointsText);

		DateFormat format = android.text.format.DateFormat.getDateFormat(this);
		
		String dateTxt = "";
		
		if (stay.getNumNights() == 1) {
			dateTxt = getString(R.string.tx_dates_one_night,
					format.format(stay.getFromDate()),
					format.format(stay.getToDate()));
		} else if (stay.getNumNights() > 1) {
			dateTxt = getString(R.string.tx_dates_multi_nights,
					format.format(stay.getFromDate()),
					format.format(stay.getToDate()),
					stay.getNumNights());
		} else if (stay.getFromDate() == null && stay.getToDate() == null) {
			dateTxt = getString(R.string.tx_unknown_duration);
		} else {
			// check the dates, one is likely null
			String fromDateTxt = "?";
			String toDateTxt = "?";
			
			if (stay.getFromDate() != null) {
				fromDateTxt = format.format(stay.getFromDate());
			}
			
			if (stay.getToDate() != null) {
				toDateTxt = format.format(stay.getToDate());
			}
			
			dateTxt = getString(R.string.tx_dates_no_nights,
					fromDateTxt,
					toDateTxt);
		}

		holder.date.setText(dateTxt);

		return convertView;
    }
    
    private static Map<String,List<ScandicStay>> staysByMonthMostRecentFirst(List<ScandicStay> stays) {
    	
    	if (stays.isEmpty())
    		return Collections.emptyMap();
    	
    	Map<String, List<ScandicStay>> stayMap = new TreeMap<String,List<ScandicStay>>(Collections.reverseOrder());
    	
    	for (ScandicStay stay : stays) {
    		
    		Date fromDate = stay.getFromDate();
    		String key = "-1";
    		if (fromDate != null) {
    			key = Integer.toString(fromDate.getMonth());
    		}
    		
    		List<ScandicStay> list = stayMap.get(key);
    		if (list == null) {
    			list = new ArrayList<ScandicStay>();
    			stayMap.put(key, list);
    		}
    		list.add(stay);
    	}
    	
    	for (List<ScandicStay> stayList : stayMap.values()) {
    		Collections.sort(stayList, new Comparator<ScandicStay>() {
				@Override
				public int compare(ScandicStay left, ScandicStay right) {
					// reverse date compare
					
					if (left.getFromDate() != null && right.getFromDate() != null) {
						return right.getFromDate().compareTo(left.getFromDate());
					} else {
						// fallback to the html order (it is reversed right now)
						if (right.getHtmlOrder() == left.getHtmlOrder()) {
							return 0;
						}
						return right.getHtmlOrder() < left.getHtmlOrder() ? -1 : 1;
					}
				}
			});
    	}
    	
    	return stayMap;
    }
   
	static class ViewHolder {
		TextView txtName;
		TextView points;
		TextView date;
	}
    
}