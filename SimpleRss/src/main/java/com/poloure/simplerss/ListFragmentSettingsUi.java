package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/* Must be public for rotation. */
public
class ListFragmentSettingsUi extends ListFragment
{
   static
   ListFragment newInstance()
   {
      return new ListFragmentSettingsUi();
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      Context context = getActivity();

      String applicationFolder = FeedsActivity.getApplicationFolder(context);

      Resources resources = context.getResources();
      String[] interfaceTitles = resources.getStringArray(R.array.settings_interface_titles);
      String[] interfaceSummaries = resources.getStringArray(R.array.settings_interface_summaries);

      setListAdapter(
            new AdapterSettingsUi(context, applicationFolder, interfaceTitles, interfaceSummaries));
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
}
