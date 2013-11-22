package com.poloure.simplerss;

import android.app.ListFragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
      return inflater.inflate(R.layout.listview, container, false);
   }
}
