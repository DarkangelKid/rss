package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

      ListView listView = (ListView) inflater.inflate(R.layout.listview, container, false);
      listView.setDividerHeight(0);
      listView.setBackgroundColor(Color.WHITE);

      return listView;
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      ActionBarActivity activity = (ActionBarActivity) getActivity();

      Resources resources = activity.getResources();
      Context context = getActivity();

      String applicationFolder = FeedsActivity.getApplicationFolder(activity);

      String[] functionTitles = resources.getStringArray(R.array.settings_function_titles);
      String[] functionSummaries = resources.getStringArray(R.array.settings_function_summaries);

      setListAdapter(new AdapterSettingsFunctions(context, applicationFolder, functionTitles,
            functionSummaries));
   }
}
