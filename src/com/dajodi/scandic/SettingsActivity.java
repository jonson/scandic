package com.dajodi.scandic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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
        
        setContentView(R.layout.login_details);

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
        Button button = (Button) findViewById(R.id.button1);
        
        // change the text to save
        button.setText(R.string.login_save);
        
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