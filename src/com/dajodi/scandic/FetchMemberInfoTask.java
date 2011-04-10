package com.dajodi.scandic;

import org.htmlcleaner.ConfigFileTagProvider;
import org.htmlcleaner.DefaultTagProvider;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dajodi.scandic.model.MemberInfo;

public class FetchMemberInfoTask extends AsyncTask<String, Void, MemberInfo> {

	public enum ProgressType {
		DIALOG, TITLE_BAR;
	}
	
	public static final String FROM_SETTINGS = "FROMSETTINGS";
	public static final String READ_FROM_DISK = "READ_FROM_DISK";
	
	private final Activity context;
	
	private MemberDetailsActivity memberDetailsActivity;
	private SettingsActivity settingsActivity;
	private ProgressDialog dialog;
	private ProgressType progressType; 
	
	private boolean invalidLogin = false;
	
	public FetchMemberInfoTask(Activity context, ProgressType progressType) {
		super();
		this.context = context;
		
		if (context instanceof MemberDetailsActivity) {
			memberDetailsActivity = (MemberDetailsActivity) context;
		} else if (context instanceof SettingsActivity) {
			settingsActivity = (SettingsActivity) context;
		}
		this.progressType = progressType;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		if (progressType == ProgressType.DIALOG) {
			dialog = ProgressDialog.show(context, "", context.getString(R.string.login_loading), true, true);
			dialog.show();
		} else if (progressType == ProgressType.TITLE_BAR) {
			context.setProgressBarIndeterminateVisibility(true);
		}
	}
	
	@Override
	protected void onPostExecute(MemberInfo result) {
		super.onPostExecute(result);
		
		if (progressType == ProgressType.DIALOG && dialog != null) {
			dialog.cancel();
		} else if (progressType == ProgressType.TITLE_BAR) {
			context.setProgressBarIndeterminateVisibility(false);
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
		
		try {
			MemberInfo info = null;
			
			if (isFromSettingsActivity()) {
				// have to relogin
				ScandicSessionHelper.clearSession();
				// write the data immedately, we need to store these if its from the settings screen
				Util.write(context, params[0], params[1]);
			}
			
			info = ScandicSessionHelper.fetchInfo(params[0], params[1]);
			
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
		} catch (Exception e) {
			Log.w("Error fetching/parsing html, cannot display anything", e);
		}
		return null;
	}

	

}
