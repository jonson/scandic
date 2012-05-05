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
