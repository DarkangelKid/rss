package com.poloure.simplerss;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

class FragmentSettingsFunctions extends ListFragment
{
   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      return inflater.inflate(R.layout.listview_settings_function, container, false);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      ActionBarActivity activity = (ActionBarActivity) getActivity();

      setListAdapter(new AdapterSettingsFunctions(activity));

      Resources resources = activity.getResources();
      String[] manageTitles = resources.getStringArray(R.array.settings_titles);

      ActionBar actionBar = activity.getSupportActionBar();
      actionBar.setSubtitle(manageTitles[0]);
   }
}
