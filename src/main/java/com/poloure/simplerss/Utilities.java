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

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.text.TextDirectionHeuristics;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

class Utilities
{
   static final String FRAGMENT_ID_PREFIX = "android:switcher:" + R.id.view_pager_tags + ':';
   private static final DisplayMetrics METRICS = Resources.getSystem().getDisplayMetrics();
   static final int EIGHT_DP = getDp(8.0F);

   static
   void updateSubtitle(Activity activity)
   {
      ListView navigationList = (ListView) activity.findViewById(R.id.navigation_list);
      ActionBar actionBar = activity.getActionBar();
      if(null != navigationList && null != actionBar)
      {
         String title = actionBar.getTitle().toString();
         String feedsTitle = activity.getResources().getStringArray(R.array.navigation_titles)[0];

         ViewPager feedPager = (ViewPager) activity.findViewById(R.id.view_pager_tags);
         Adapter adapter = navigationList.getAdapter();

         /* If the title is not Feeds, set no subtitle. */
         if(null == feedPager || !title.equals(feedsTitle))
         {
            actionBar.setSubtitle(null);
         }
         else if(0 != adapter.getCount() - 3)
         {
            String unreadText = activity.getString(R.string.actionbar_subtitle_unread);
            int count = ((NavItem) adapter.getItem(feedPager.getCurrentItem())).m_count;

            actionBar.setSubtitle(unreadText + ' ' + getLocaleInt(count));
         }
      }
   }

   static
   String getLocaleInt(Integer count)
   {
      NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
      return format.format(count);
   }

   static
   String getLocaleLong(Long count)
   {
      NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
      return format.format(count);
   }

   static
   int getDp(float pixels)
   {
      DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
      float floatDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, metrics);
      return Math.round(floatDp);
   }

   static
   float getSp(float pixels)
   {
      return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, pixels, METRICS);
   }

   static
   void setPaddingEqual(View view, int padding)
   {
      view.setPadding(padding, padding, padding, padding);
   }

   static
   XmlPullParser createXmlParser(CharSequence urlString) throws IOException, XmlPullParserException
   {
      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
      factory.setNamespaceAware(true);
      XmlPullParser parser = factory.newPullParser();

      URL url = new URL(urlString.toString());
      InputStream inputStream = url.openStream();
      parser.setInput(inputStream, null);
      return parser;
   }

   static
   void switchFragments(FeedsActivity activity, String newTag)
   {
      /* If the page is our current page, return. */
      if(activity.m_currentFragment.equals(newTag))
      {
         return;
      }
      FragmentManager manager = activity.getFragmentManager();
      manager.beginTransaction()
             .hide(FeedsActivity.getFragment(manager, activity.m_currentFragment))
             .show(FeedsActivity.getFragment(manager, newTag))
             .commit();
      activity.m_currentFragment = newTag;
   }

   static
   boolean isTextRtl(CharSequence c)
   {
      return TextDirectionHeuristics.FIRSTSTRONG_LTR.isRtl(c, 0, c.length() - 1);
   }
}
