package com.dajodi.scandic;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dajodi.scandic.model.MemberInfo;
import com.dajodi.scandic.model.UpdateSource;

public class FetchMemberInfoTask extends AsyncTask<String, Void, MemberInfo> {

	public enum ProgressType {
		DIALOG, TITLE_BAR;
	}
	
	public static final String FROM_SETTINGS = "FROMSETTINGS";
	public static final String READ_FROM_DISK = "READ_FROM_DISK";
	
	private final Activity context;
	private final ProgressType progressType;
	private final UpdateSource source;
	
	private MemberDetailsActivity memberDetailsActivity;
	private SettingsActivity settingsActivity;
	private ProgressDialog dialog;
	private boolean invalidLogin = false;
	
	public FetchMemberInfoTask(Activity context, ProgressType progressType, UpdateSource source) {
		super();
		this.context = context;
		this.progressType = progressType;
		this.source = source;
		
		if (context instanceof MemberDetailsActivity) {
			memberDetailsActivity = (MemberDetailsActivity) context;
		} else if (context instanceof SettingsActivity) {
			settingsActivity = (SettingsActivity) context;
		}
		
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		if (progressType == ProgressType.DIALOG) {
			dialog = ProgressDialog.show(context, "", context.getString(R.string.login_loading), true, true);
			dialog.show();
		} else if (progressType == ProgressType.TITLE_BAR) {
			
			if (memberDetailsActivity != null) {
				memberDetailsActivity.setRefreshActionButtonCompatState(true);
			} else {
				context.setProgressBarIndeterminateVisibility(true);
			}
		}
	}
	
	@Override
	protected void onPostExecute(MemberInfo result) {
		super.onPostExecute(result);
		
		if (progressType == ProgressType.DIALOG && dialog != null) {
			dialog.cancel();
		} else if (progressType == ProgressType.TITLE_BAR) {
			
			if (memberDetailsActivity != null) {
				memberDetailsActivity.setRefreshActionButtonCompatState(false);
			} else {
				context.setProgressBarIndeterminateVisibility(false);
			}
		}
		
		if (result != null) {
			if (isFromMainActivity()) {
				memberDetailsActivity.updateMemberInfo(result);
				memberDetailsActivity.showDetailsView();
			} else if (isFromSettingsActivity()) {
				// need to create the intent to get back to main activity
				Intent intent = new Intent(context, MemberDetailsActivity.class);
				intent.putExtra(READ_FROM_DISK, true);
				intent.putExtra(FROM_SETTINGS, true);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intent);
			}
		} else {
			
			// memberinfo is null, something bad happened
			if (isFromMainActivity()) {
				
				if (invalidLogin) {
					Toast.makeText(context, R.string.login_failed, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, R.string.login_error, Toast.LENGTH_SHORT).show();
				}
				
			} else if (isFromSettingsActivity()) {
				// could be bad password, ...
				if (invalidLogin) {
					Toast.makeText(context, R.string.login_failed, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, R.string.login_error, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
	
	private boolean isFromMainActivity() {
		return memberDetailsActivity != null;
	}
	
	private boolean isFromSettingsActivity() {
		return settingsActivity != null;
	}
	
	@Override
	protected MemberInfo doInBackground(String... params) {
		
		Tracker tracker = Singleton.INSTANCE.getTracker();
		
		try {
			
			
			if (isFromSettingsActivity()) {
				// have to relogin
				ScandicSessionHelper.clearSession();
			}
			
			long before = System.currentTimeMillis();
			MemberInfo info = ScandicSessionHelper.fetchInfo(params[0], params[1]);
			long after = System.currentTimeMillis();
			Log.d("login took " + (after - before) + "ms");
			
			if (info != null) {
				// write the username, password only after a successful request
				Util.write(context, params[0], params[1]);
				
				// finally try to write it to disk, worries if it fails here
				Util.writeMemberInfo(context, info);
				Log.d("successfully wrote fileinfo to disk");
				
			}
			
			return info;
		} catch (InvalidLoginException e) {
			Log.i("Login failed");
			invalidLogin = true;
			tracker.trackLoginError();
		} catch (Exception e) {
			Log.w("Error fetching/parsing html, cannot display anything", e);
			tracker.trackUnknownError();
		}
		return null;
	}

	

}
