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
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public
class FragmentFeeds extends Fragment
{
   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      Activity activity = getActivity();

      String applicationFolder = FeedsActivity.getApplicationFolder(activity);
      ViewPager.OnPageChangeListener onTagPageChange = new OnPageChangeTags(activity,
            applicationFolder);

      PagerAdapterFeeds adapter = new PagerAdapterFeeds(activity.getFragmentManager());
      adapter.updateTags(applicationFolder, activity);

      /* Inflate and configure the ViewPager. */
      ViewPager viewPager = (ViewPager) inflater.inflate(R.layout.view_pager_tags, container,
            false);
      viewPager.setAdapter(adapter);
      viewPager.setOffscreenPageLimit(128);
      viewPager.setOnPageChangeListener(onTagPageChange);

      return viewPager;
   }
}
