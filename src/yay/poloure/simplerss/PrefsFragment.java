package yay.poloure.simplerss;

import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.content.SharedPreferences;
import android.os.Bundle;

public class PrefsFragment extends PreferenceFragment
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
			// Load the preferences from an XML resource
		addPreferencesFromResource(R.layout.preferences);
		Preference customPref = (Preference) findPreference("customPref");
		/*customPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference preference)
			{
				SharedPreferences customSharedPreference = getSharedPreferences("myCustomSharedPrefs", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = customSharedPreference.edit();
				editor.putString("myCustomPref","The preference has been clicked");
				editor.commit();
				return true;
			}
		});*/
	}
}
