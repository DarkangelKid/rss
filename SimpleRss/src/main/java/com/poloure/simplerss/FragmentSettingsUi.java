package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/* Must be public for rotation. */
public
class FragmentSettingsUi extends ListFragment
{
   static
   ListFragment newInstance()
   {
      return new FragmentSettingsUi();
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);
      Context context = getActivity();

      Resources resources = context.getResources();
      LayoutInflater layoutInflater = getLayoutInflater(savedInstanceState);

      String applicationFolder = FeedsActivity.getApplicationFolder(context);

      String[] interfaceTitles = resources.getStringArray(R.array.settings_interface_titles);
      String[] interfaceSummaries = resources.getStringArray(R.array.settings_interface_summaries);

      setListAdapter(new AdapterSettingsUi(applicationFolder, interfaceTitles, interfaceSummaries,
            layoutInflater));
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      return inflater.inflate(R.layout.listview_settings_function, container, false);
   }
}
