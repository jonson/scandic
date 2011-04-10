package com.dajodi.scandic;

import android.os.AsyncTask;

import com.dajodi.scandic.model.MemberInfo;

public class FetchPersistedStateTask extends AsyncTask<Void, Void, MemberInfo> {

	private final MemberDetailsActivity context;
	
	public FetchPersistedStateTask(MemberDetailsActivity context) {
		super();
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
	
	@Override
	protected void onPostExecute(MemberInfo result) {
		super.onPostExecute(result);
	}
	
	@Override
	protected MemberInfo doInBackground(Void... nothing) {
		
		MemberInfo info = null;
		try {
			info = Util.readMemberInfo(context);
			
		} catch (Exception e) {
			Log.w("Error reading persisted info", e);
		}
		return info;
	}

	

}
