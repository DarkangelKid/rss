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
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.text.TextDirectionHeuristics;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Adapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class Utilities
{
   static final int EIGHT_DP = getDp(8.0F);

   static
   void updateSubtitle(Activity activity)
   {
      ListView navigationList = (ListView) activity.findViewById(R.id.navigation_drawer);
      ActionBar actionBar = activity.getActionBar();
      Resources res = activity.getResources();

      if(null != navigationList && null != actionBar)
      {
         List<String> titles = Arrays.asList(res.getStringArray(R.array.navigation_titles));
         String title = actionBar.getTitle().toString();

         ViewPager feedPager = (ViewPager) activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
         Adapter adapter = navigationList.getAdapter();

         /* If the title is not Feeds, set no subtitle. */
         if(null == feedPager || titles.contains(title))
         {
            actionBar.setSubtitle(null);
         }
         else if(0 != adapter.getCount() - 2)
         {
            String[] tag = (String[]) adapter.getItem(feedPager.getCurrentItem());
            if(null == tag || 0 == tag.length || tag[1].isEmpty())
            {
               actionBar.setSubtitle(null);
            }
            else
            {
               int count = Integer.parseInt(tag[1]);
               String countString = res.getQuantityString(R.plurals.actionbar_subtitle_unread, count, count);
               actionBar.setSubtitle(0 == count ? null : countString);
            }
         }
      }
   }

   static
   void updateTitle(Activity activity)
   {
      ListView navigationList = (ListView) activity.findViewById(R.id.navigation_drawer);
      ViewPager feedPager = (ViewPager) activity.findViewById(FragmentFeeds.VIEW_PAGER_ID);
      ActionBar bar = activity.getActionBar();

      if(null != navigationList && null != bar && null != feedPager)
      {
         int position = feedPager.getCurrentItem();
         Drawable drawable = activity.getResources().getDrawable(R.drawable.ic_action_labels);
         drawable.setAutoMirrored(true);
         bar.setIcon(drawable);
         bar.setTitle(PagerAdapterTags.TAG_LIST.get(position));
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
      DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
      float floatDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, metrics);
      return Math.round(floatDp);
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
   boolean isTextRtl(CharSequence c)
   {
      return TextDirectionHeuristics.FIRSTSTRONG_LTR.isRtl(c, 0, c.length() - 1);
   }
}
