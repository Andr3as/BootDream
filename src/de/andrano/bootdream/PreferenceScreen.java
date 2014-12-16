package de.andrano.bootdream;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class PreferenceScreen extends Activity {
	
	DreamHandler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		handler = new DreamHandler(getApplicationContext());
		if (!handler.checkDirectoryExistence()) {
			Toast.makeText(getApplicationContext(), R.string.bd_unable_to_create_directory, Toast.LENGTH_LONG).show();
			finish();
		}
		
		setContentView(R.layout.activity_preference_screen);
		
		Toolbar actionbar = (Toolbar) findViewById(R.id.actionbar);
	    actionbar.setTitle(R.string.app_name);
	    actionbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_launcher));
	    actionbar.setTitleTextColor(getResources().getColor(R.color.white));
	}
}
