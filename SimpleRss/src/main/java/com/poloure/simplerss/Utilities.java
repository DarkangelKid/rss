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
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.ProgressBar;

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
   private static final DisplayMetrics METRICS = Resources.getSystem().getDisplayMetrics();
   static final int EIGHT_DP = getDp(8.0F);

   static
   void updateSubtitle(Activity activity, int page)
   {
      ListView navigationList = (ListView) activity.findViewById(R.id.navigation_list);
      ActionBar actionBar = activity.getActionBar();
      if(null != navigationList && null != actionBar)
      {
         if(-1 == page)
         {
            actionBar.setSubtitle(null);
         }
         else
         {
            Adapter adapter = navigationList.getAdapter();
            int count = ((NavItem) adapter.getItem(page)).m_count;

            String unreadText = activity.getString(R.string.subtitle_unread);
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
   int getDp(float pixels)
   {
      float floatDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, METRICS);
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
   XmlPullParser createXmlParser(String urlString) throws IOException, XmlPullParserException
   {
      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
      factory.setNamespaceAware(true);
      XmlPullParser parser = factory.newPullParser();

      URL url = new URL(urlString);
      InputStream inputStream = url.openStream();
      parser.setInput(inputStream, null);
      return parser;
   }

   static
   View makeProgressBar(Context context)
   {
      ProgressBar progressBar = new ProgressBar(context);
      setPaddingEqual(progressBar, getDp(7.0F));

      return progressBar;
   }

   static
   void switchFragments(FeedsActivity activity, String oldTag, String newTag)
   {
      FragmentManager manager = activity.getFragmentManager();
      manager.beginTransaction()
             .hide(manager.findFragmentByTag(oldTag))
             .show(manager.findFragmentByTag(newTag))
             .commit();
      activity.m_currentFragment = newTag;
   }

   static
   boolean isRtl(char c)
   {
      if(0x600 <= c && 0x6ff >= c)
      {
         return true;
      }
      if(0x750 <= c && 0x77f >= c)
      {
         return true;
      }
      if(0xfb50 <= c && 0xfc3f >= c)
      {
         return true;
      }
      if(0xfe70 <= c && 0xfefc >= c)
      {
         return true;
      }
      return false;
   }
}
