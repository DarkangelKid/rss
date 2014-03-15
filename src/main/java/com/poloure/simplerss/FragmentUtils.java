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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.webkit.WebViewFragment;

class FragmentUtils
{
   static
   void addAllFragments(Activity activity)
   {
      FragmentManager manager = activity.getFragmentManager();
      FragmentTransaction trans = manager.beginTransaction();

      add(manager, trans, new FragmentFeeds(), FeedsActivity.FEED_TAG);
      add(manager, trans, new ListFragmentManage(), FeedsActivity.MANAGE_TAG);
      add(manager, trans, new ListFragmentFavourites(), FeedsActivity.FAVOURITES_TAG);
      add(manager, trans, new WebViewFragment(), FeedsActivity.WEB_TAG);
      add(manager, trans, new FeedsActivity.SettingsFragment(), FeedsActivity.SETTINGS_TAG);

      trans.commit();
   }

   static
   void switchToFragment(FeedsActivity activity, String nextTag, boolean addToBackStack)
   {
      if(!activity.m_currentTag.equals(nextTag))
      {
         FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();

         transaction.hide(getFragment(activity, activity.m_currentTag))
               .show(getFragment(activity, nextTag));
         if(addToBackStack)
         {
            transaction.addToBackStack(null);
         }
         transaction.commit();
         activity.m_previousTag = activity.m_currentTag;
         activity.m_currentTag = nextTag;
      }
   }

   static
   void add(FragmentManager manager, FragmentTransaction transaction, Fragment fragment, String tag)
   {
      Fragment fragmentByTag = manager.findFragmentByTag(tag);

      if(null == fragmentByTag)
      {
         transaction.add(R.id.content_frame, fragment, tag);
         if(!tag.equals(FeedsActivity.FEED_TAG))
         {
            transaction.hide(fragment);
         }
      }
   }

   static
   Fragment getFragment(Activity activity, String tag)
   {
      return activity.getFragmentManager().findFragmentByTag(tag);
   }
}
