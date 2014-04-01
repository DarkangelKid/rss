/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import android.app.ActionBar;
import android.app.Dialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.Window;

public
class FragmentSettings extends PreferenceFragment
{
    @Override
    public
    void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        for(CharSequence key : new String[]{"ONE", "TWO", "THREE"})
        {
            PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference(key);
            if(null != preferenceScreen)
            {
                preferenceScreen.setOnPreferenceClickListener(new preferenceClickListener(preferenceScreen));
            }
        }
    }

    class preferenceClickListener implements Preference.OnPreferenceClickListener
    {
        private final PreferenceScreen m_preferenceScreen;

        preferenceClickListener(PreferenceScreen preferenceScreen)
        {
            m_preferenceScreen = preferenceScreen;
        }

        @Override
        public
        boolean onPreferenceClick(Preference preference)
        {
            ActionBar actionBar = m_preferenceScreen.getDialog().getActionBar();
            actionBar.setIcon(R.drawable.ic_action_settings);
            actionBar.setTitle(m_preferenceScreen.getTitle());

            Dialog dialog = m_preferenceScreen.getDialog();
            Window window = dialog.getWindow();
            View view = window.getDecorView().findViewById(android.R.id.content);
            Constants.setTopOffset(getActivity(), view);

            return true;
        }
    }
}