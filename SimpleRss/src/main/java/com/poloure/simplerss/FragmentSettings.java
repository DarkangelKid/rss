package com.poloure.simplerss;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/* Must be public for rotation. */
public
class FragmentSettings extends PreferenceFragment
{
   static
   Fragment newInstance()
   {
      return new FragmentSettings();
   }

   @Override
   public
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      addPreferencesFromResource(R.xml.preferences);
      setHasOptionsMenu(true);
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      View view = super.onCreateView(inflater, container, savedInstanceState);
      view.setBackgroundColor(Color.WHITE);
      return view;
   }

   @Override
   public
   void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      MenuItem refreshMenu = menu.findItem(R.id.refresh);
      MenuItem unreadMenu = menu.findItem(R.id.unread);
      MenuItem addFeedMenu = menu.findItem(R.id.add_feed);

      if(null != refreshMenu && null != unreadMenu && null != addFeedMenu)
      {
         refreshMenu.setVisible(false);
         unreadMenu.setVisible(false);
         addFeedMenu.setVisible(false);
      }
   }
}
