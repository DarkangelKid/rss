package com.poloure.simplerss;

import android.app.Activity;
import android.app.ListFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/* Must be public for rotation. */
public
class ListFragmentSettingsFunctions extends ListFragment
{
   static
   ListFragment newInstance()
   {
      return new ListFragmentSettingsFunctions();
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);
      return inflater.inflate(R.layout.listview, container, false);
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      Activity activity = getActivity();

      Resources resources = activity.getResources();

      String applicationFolder = FeedsActivity.getApplicationFolder(activity);

      String[] functionTitles = resources.getStringArray(R.array.settings_function_titles);
      String[] functionSummaries = resources.getStringArray(R.array.settings_function_summaries);

      setListAdapter(new AdapterSettingsFunctions(activity, applicationFolder, functionTitles,
            functionSummaries));
   }
}
