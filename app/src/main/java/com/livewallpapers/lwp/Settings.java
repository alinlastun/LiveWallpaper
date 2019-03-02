package com.livewallpapers.lwp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;


import com.livewallpapers.R;

import java.util.Map;

public class Settings extends PreferenceActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener,OnPreferenceClickListener {
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		//setContentView(R.layout.adlayout);
		addPreferencesFromResource(R.xml.wallpaper_settings);
		populatePreferencesDesc();
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
		getPreferenceManager().setSharedPreferencesName(
				WaterDropMain.SHARED_PREFS_NAME);
	}


	public void populatePreferencesDesc() {
		// Set up initial values for all list preferences
		Map<String, ?> sharedPreferencesMap = getPreferenceScreen()
				.getSharedPreferences().getAll();
		Preference pref;
		ListPreference listPref;
		CheckBoxPreference checkPref;
		for (Map.Entry<String, ?> entry : sharedPreferencesMap.entrySet()) {
			pref = findPreference(entry.getKey());
			if (pref instanceof ListPreference) {
				listPref = (ListPreference) pref;
				CharSequence[] mPositions = listPref.getEntries();
				int index = Integer.valueOf(listPref.getPreferenceManager()
						.getSharedPreferences().getString(entry.getKey(), "0"));

				if ((entry.getKey() == "horizontal_option") && (index < 10)) {
					index = 20;
				}
				((ListPreference) pref).setValue(index + "");

				if (index >= 10) {
					index = index / 10;
				}
			
			} else if(pref instanceof CheckBoxPreference){
				
			}
		}

	};

	@Override
	protected void onResume() {
		super.onResume();
		populatePreferencesDesc();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}


	public boolean checkForInternetConnection() {

		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// ARE WE CONNECTED TO THE NET
		if (conMgr.getActiveNetworkInfo() != null
				&& conMgr.getActiveNetworkInfo().isAvailable()
				&& conMgr.getActiveNetworkInfo().isConnected()) {
			return true;
		} else
			return false;
	}
	

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Preference pref = findPreference(key);
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}
	}

	

	
	public boolean onPreferenceClick(Preference var1) {
		/*if (var1 == this.imagePreference1) {
			System.out.println("imagePreference1 clicked!");
			this.openLink(this.getString(R.string.recommend1_url));
		} else if (var1 == this.imagePreference2) {
			this.openLink(this.getString(R.string.recommend2_url));
		} else if (var1 == this.imagePreference3) {
			this.openLink(this.getString(R.string.recommend3_url));
		} else if (var1 == this.shareToPreference) {
			this.shareThisApp();
		}
*/
		return false;
	}
}
