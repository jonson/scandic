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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dajodi.scandic.FetchMemberInfoTask.ProgressType;
import com.dajodi.scandic.model.UpdateSource;
import com.dajodi.scandic.user.UsernamePassword;

public class SettingsActivity extends Activity {
    /** Called when the activity is first created. */
	
	public UsernamePassword before;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.settings_main);

        before = Util.read(this);
        
        updateUserPass(before);
        
        // only show the login screen, w/ different text on the login button
        createLoginView();
    }
	
	private void updateUserPass(UsernamePassword userpass) {
    	((EditText)findViewById(R.id.txtUsername)).setText(userpass.getUsername());
    	((EditText)findViewById(R.id.txtPassword)).setText(userpass.getPassword());
    	
    	// focus the button (not working)
//    	Button button = (Button) loginView.findViewById(R.id.button1);
//    	loginView.requestFocus();
    }
    
    private void createLoginView() {
				
		// add a progress dialog to the login button
        Button button = (Button) findViewById(R.id.loginButton);
        
        // change the text to save
        button.setText(R.string.login_save);
        
        // hide the refresh button
        ImageButton refreshButton = (ImageButton) findViewById(R.id.actionBarRefreshButton);
        refreshButton.setVisibility(View.GONE);
        findViewById(R.id.actionBarSep1).setVisibility(View.GONE);
        
        button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				EditText usernameTxt = (EditText)findViewById(R.id.txtUsername);
				EditText passwordTxt = (EditText)findViewById(R.id.txtPassword);
				
				String username = usernameTxt.getText().toString();
				String password = passwordTxt.getText().toString();
				
				// some validation here (membership a number, pw length, ...)
				if (!Util.usernameValid(username)) {
					Toast.makeText(SettingsActivity.this, R.string.login_invalid_user, Toast.LENGTH_SHORT).show();
					usernameTxt.requestFocus();
				} else if (!Util.passwordValid(password)) {
					Toast.makeText(SettingsActivity.this, R.string.login_invalid_password, Toast.LENGTH_SHORT).show();
					passwordTxt.requestFocus();
				} else {

					// write the data immedately, we need to store these if its from the settings screen
					Util.write(SettingsActivity.this, username, password);
					
					if (before.getUsername().equals(username) && before.getPassword().equals(password)) {
						// username & password are the same (prob just hit the save button), don't do anything
						// just bounce back to the main activity
						Intent intent = new Intent(SettingsActivity.this, MemberDetailsActivity.class);
						intent.putExtra(FetchMemberInfoTask.FROM_SETTINGS, true);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					} else {
						// bounce back to the main activity w/ the new data
						performLogin(username, password);
					}
					
				}
			}
		});
	}
    
    private void performLogin(String username, String password) {
		// should validate here
		new FetchMemberInfoTask(this, ProgressType.DIALOG, UpdateSource.CHANGEPW_BUTTON).execute(username, password);
	}
    
    
}