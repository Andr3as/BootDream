package de.andrano.bootdream;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

public class PreferenceFragment extends android.preference.PreferenceFragment{

	Activity activity;
	Context context;
	Resources resources;
	
	DreamHandler handler;
	List<Dream> dreams;
	
	SharedPreferences sharedPreferences;
	
	ListPreference dreamPreference;
	Preference developerOptions;
	
	boolean hided = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity 	= getActivity();
        context		= activity.getApplicationContext();
        resources	= getResources();
        
        handler = new DreamHandler(context);
        
        sharedPreferences = context.getSharedPreferences(
        			resources.getString(R.string.preferencesFile), Context.MODE_PRIVATE);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        findPreference(resources.getString(R.string.key_start_dream)).setOnPreferenceClickListener(startDaydream);
        dreamPreference = (ListPreference) findPreference(resources.getString(R.string.key_list_dreams));
        developerOptions = findPreference(resources.getString(R.string.key_developer_options));
        findPreference(resources.getString(R.string.key_version_number)).setOnPreferenceClickListener(versionNumberListener);
        
        dreamPreference.setOnPreferenceChangeListener(dreamChangeListener);
        //Add available dreams to preference
        dreams = handler.getAvailableDreams();
        String[] entries = new String[dreams.size()];
        CharSequence[] values	= new CharSequence[dreams.size()];
        for(int i = 0; i < dreams.size(); i++) {
        	entries[i] 	= dreams.get(i).getName();
        	values[i]	= dreams.get(i).getPath();
        }
        dreamPreference.setEntries(entries);
        dreamPreference.setEntryValues(values);
        String defaultDream = handler.getDefaultDream();
        dreamPreference.setSummary(handler.getDreamByPath(dreams, defaultDream).name);
        dreamPreference.setValue(defaultDream);
        
        // Remove developer options
        if (sharedPreferences.getBoolean(resources.getString(R.string.shared_hide_developer_options), true)) {
        	Log.d("de.andrano.bootdream", "removeOption");
        	getPreferenceScreen().removePreference(developerOptions);
        	hided = true;
        }
    }
	
	OnPreferenceClickListener startDaydream = new OnPreferenceClickListener() {
		
		@SuppressWarnings("static-access")
		@Override
		public boolean onPreferenceClick(Preference preference) {
			try {
				Intent i = new Intent(Intent.ACTION_MAIN);
				i.setClassName("com.android.systemui", "com.android.systemui.Somnambulator");
				getActivity().startActivity(i);
			} catch (Exception e) {
				new Toast(context).makeText(context, R.string.bd_device_problem, Toast.LENGTH_LONG).show();
			}
			return true;
		}
	};
	
	OnPreferenceChangeListener dreamChangeListener = new OnPreferenceChangeListener() {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			dreamPreference.setSummary(handler.getDreamByPath(dreams, newValue.toString()).name);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(resources.getString(R.string.shared_default_dream), newValue.toString());
			editor.apply();
			return true;
		}
	};
	
	OnPreferenceClickListener versionNumberListener = new OnPreferenceClickListener() {
		
		int times = 0;
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
			times++;
			if (times == 10) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				if (hided) {
					getPreferenceScreen().addPreference(developerOptions);
					Toast.makeText(context, R.string.bd_developer_options_enabled, Toast.LENGTH_LONG).show();
					
					editor.putBoolean(resources.getString(R.string.shared_hide_developer_options), false);
				} else {
					getPreferenceScreen().removePreference(developerOptions);
					Toast.makeText(context, R.string.bd_developer_options_disabled, Toast.LENGTH_LONG).show();
					
					editor.putBoolean(resources.getString(R.string.shared_hide_developer_options), true);
				}
				editor.apply();
				times = 0;
				hided = !hided;
			}
			return true;
		}
	};
	
}
