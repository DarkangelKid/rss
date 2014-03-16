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
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

class Utilities
{
   static final int EIGHT_DP = getDp(8.0F);
   static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.getDefault());

   static
   ListView getCurrentTagListView(Activity activity)
   {
      int currentPage = ((ViewPager) activity.findViewById(R.id.viewpager)).getCurrentItem();
      return getTagListView(activity, currentPage);
   }

   static
   ListView getTagListView(Activity activity, int page)
   {
      return (ListView) activity.findViewById(ListFragmentTag.LIST_VIEW_ID_BASE + page);
   }

   static
   String formatTags(String... tags)
   {
      String tagsWithBraces = Arrays.toString(tags);
      return tagsWithBraces.substring(1, tagsWithBraces.length() - 1);
   }

   static
   void setTitlesAndDrawerAndPage(FeedsActivity activity, String fragmentTag, int absolutePos)
   {
      ActionBar bar = activity.getActionBar();
      Resources resources = activity.getResources();
      String[] navTitles = resources.getStringArray(R.array.navigation_titles);
      ViewPager pager = (ViewPager) activity.findViewById(R.id.viewpager);

      ListView list = (ListView) activity.findViewById(R.id.navigation_drawer);
      HeaderViewListAdapter headerAdapter = (HeaderViewListAdapter) list.getAdapter();
      int headers = headerAdapter.getHeadersCount();
      int listPosition = -10 == absolutePos ? pager.getCurrentItem() + headers : absolutePos;
      int viewPagerPos = -10 == absolutePos ? pager.getCurrentItem() : absolutePos - headers;

      /* Check the drawer item. */

      String title = PagerAdapterTags.s_tagList.get(0);
      String subTitle = null;
      int imageRes = 0;

      if(FeedsActivity.FAVOURITES_TAG.equals(fragmentTag))
      {
         listPosition = 0;
         title = navTitles[0];
         imageRes = R.drawable.ic_action_important;
      }
      else if(FeedsActivity.MANAGE_TAG.equals(fragmentTag))
      {
         listPosition = 1;
         title = navTitles[1];
         imageRes = R.drawable.ic_action_storage;
      }
      else if(FeedsActivity.SETTINGS_TAG.equals(fragmentTag))
      {
         listPosition = 2;
         title = navTitles[2];
         imageRes = R.drawable.ic_action_settings;
      }
      else if(FeedsActivity.FEED_TAG.equals(fragmentTag))
      {
         ArrayAdapter<String[]> adapter = (ArrayAdapter<String[]>) headerAdapter.getWrappedAdapter();

         if(null != adapter && 0 < adapter.getCount())
         {
            String[] item = adapter.getItem(viewPagerPos);
            title = item[0];
            int count = null == item[1] || item[1].isEmpty() ? 0 : Integer.parseInt(item[1]);
            String countString = activity.getResources()
                  .getQuantityString(R.plurals.actionbar_subtitle_unread, count, count);
            subTitle = 0 == count ? null : countString;
         }
         imageRes = R.drawable.ic_action_labels;
      }
      bar.setTitle(title);
      bar.setSubtitle(subTitle);
      bar.setIcon(imageRes);

      list.setItemChecked(listPosition, true);

      /* If we must change the view pager page. */
      if(0 <= viewPagerPos)
      {
         /* Switch the view pager page if different. */
         if(pager.getCurrentItem() != viewPagerPos)
         {
            pager.setCurrentItem(viewPagerPos);
         }
      }
   }

   static
   void setTopOffset(Activity activity, int viewId)
   {
      setTopOffset(activity, activity.findViewById(viewId));
   }

   static
   void setTopOffset(Activity activity, View view)
   {
      Resources resources = activity.getResources();
      TypedValue value = new TypedValue();

      activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, value, true);
      int actionBar = activity.getResources().getDimensionPixelSize(value.resourceId);
      int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
      int statusBar = resources.getDimensionPixelSize(resourceId);

      view.setPadding(0, actionBar + statusBar, 0, 0);
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
   List<IndexItem> loadIndexList(Context context)
   {
      List<IndexItem> list = (List<IndexItem>) Read.object(context, Read.INDEX);
      return null == list ? new ArrayList<IndexItem>(0) : list;
   }

   static
   Collection<Long> loadReadItems(Context context)
   {
      Set<Long> set = (Set<Long>) Read.object(context, FeedsActivity.READ_ITEMS);
      return null == set ? new HashSet<Long>(0) : set;
   }

   static
   boolean isTextRtl(CharSequence c)
   {
      return TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR.isRtl(c, 0, c.length() - 1);
   }
}
