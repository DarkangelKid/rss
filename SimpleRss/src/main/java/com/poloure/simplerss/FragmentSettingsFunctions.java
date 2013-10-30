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
   static
   ListFragment newInstance()
   {
      return new FragmentSettingsFunctions();
   }

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

      Resources resources = activity.getResources();
      LayoutInflater layoutInflater = getLayoutInflater(savedInstanceState);

      String applicationFolder = FeedsActivity.getApplicationFolder(activity);

      String[] functionTitles = resources.getStringArray(R.array.settings_function_titles);
      String[] functionSummaries = resources.getStringArray(R.array.settings_function_summaries);

      setListAdapter(new AdapterSettingsFunctions(applicationFolder, FeedsActivity.SETTINGS_DIR,
            functionTitles, functionSummaries, layoutInflater));

      ActionBar actionBar = activity.getSupportActionBar();

      String[] manageTitles = resources.getStringArray(R.array.settings_titles);
      actionBar.setSubtitle(manageTitles[0]);
   }
}
