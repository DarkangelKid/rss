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

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class AdapterNavigationDrawer extends BaseAdapter
{
   private static final int[] NAV_ICONS = {
         R.drawable.action_feeds, R.drawable.action_manage, R.drawable.action_settings,
   };
   private static final float MIN_HEIGHT_MAIN = 48.0F;
   private static final float HORIZONTAL_PADDING_MAIN = 8.0F;
   private static final float HORIZONTAL_PADDING_SUB = 16.0F;
   private static final float PADDING_COMPOUND_DRAWABLE = 12.0F;
   private static final float TEXT_SIZE_MAIN = 20.0F;
   private static final ColorDrawable GREY_LINE = new ColorDrawable(Color.parseColor("#888888"));
   private static final int TYPE_TITLE = 0;
   private static final int TYPE_DIVIDER = 1;
   private static final int TYPE_TAG = 2;
   private static final int[] TYPES = {TYPE_TITLE, TYPE_DIVIDER, TYPE_TAG};
   private static final int[] EMPTY_INT_ARRAY = new int[0];
   private static final Typeface SANS_SERIF_LITE = Typeface
         .create("sans-serif-light", Typeface.NORMAL);

   private final String[] m_navigationTitles;
   private final Context m_context;
   private final List<String> m_tagArray = new ArrayList<>(0);
   private int[] m_unreadArray = EMPTY_INT_ARRAY;

   AdapterNavigationDrawer(String[] navigationTitles, Context context)
   {
      m_navigationTitles = navigationTitles.clone();
      m_context = context;
   }

   void setArrays(Collection<String> tags, int[] unreadCounts)
   {
      if(!m_tagArray.equals(tags))
      {
         m_tagArray.clear();
         m_tagArray.addAll(tags);
      }
      m_unreadArray = unreadCounts.clone();
   }

   @Override
   public
   int getCount()
   {
      return m_tagArray.size() + 4;
   }

   @Override
   public
   String getItem(int position)
   {
      return 0 == m_unreadArray.length ? "" : Integer.toString(m_unreadArray[position]);
   }

   @Override
   public
   long getItemId(int position)
   {
      return position;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      View view = convertView;
      boolean isNewView = null == convertView;
      int viewType = getItemViewType(position);

      if(TYPE_TITLE == viewType)
      {
         if(isNewView)
         {
            int minHeight = Utilities.getDp(MIN_HEIGHT_MAIN);
            int hPadding = Utilities.getDp(HORIZONTAL_PADDING_MAIN);
            int paddingDrawable = Utilities.getDp(PADDING_COMPOUND_DRAWABLE);

            TextView textView = new TextView(m_context);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTypeface(SANS_SERIF_LITE);
            textView.setMinHeight(minHeight);
            textView.setPadding(hPadding, 0, hPadding, 0);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_MAIN);
            textView.setTextColor(Color.WHITE);
            textView.setCompoundDrawablePadding(paddingDrawable);

            view = textView;
         }

         ((TextView) view).setText(m_navigationTitles[position]);
         ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(NAV_ICONS[position], 0, 0, 0);
      }
      else if(TYPE_DIVIDER == viewType && null == view)
      {
         TextView textView = new TextView(m_context);

         String tagsTitle = m_context.getString(R.string.feed_tag_title);

         int drawerWidth = parent.getWidth();
         GREY_LINE.setBounds(0, 0, drawerWidth, 3);

         int vPadding = Utilities.getDp(HORIZONTAL_PADDING_SUB / 4.0F);
         int hPadding = Utilities.getDp(HORIZONTAL_PADDING_SUB);

         textView.setCompoundDrawables(null, null, null, GREY_LINE);
         textView.setCompoundDrawablePadding(8);
         textView.setPadding(hPadding, vPadding, hPadding, vPadding);
         textView.setText(tagsTitle);
         textView.setTextColor(Color.WHITE);
         textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0F);

         view = textView;
      }
      else if(TYPE_TAG == viewType)
      {
         if(isNewView)
         {
            TextView textView = new TextView(m_context);

            int minHeight = Utilities.getDp(42.0F);
            int padding = Utilities.getDp(HORIZONTAL_PADDING_SUB);
            int half = Math.round(padding * 0.5F);

            textView.setPadding(padding, -half, padding, half);
            textView.setMinHeight(minHeight);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0F);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTextColor(Color.WHITE);
            textView.setTypeface(SANS_SERIF_LITE);
            textView.setLineSpacing(0.0F, 0.1F);

            view = textView;
         }

         String number = Integer.toString(m_unreadArray[position - 4]);
         String unreadText = "0".equals(number) ? "" : number;
         String tagTitle = m_tagArray.get(position - 4);

         /* This here allows the unread counter to be aligned to the right. */
         ((TextView) view).setText(tagTitle + '\n' + (char) 0x200F + unreadText);
      }
      return view;
   }

   @Override
   public
   boolean isEnabled(int position)
   {
      return 3 != position;
   }

   @Override
   public
   int getItemViewType(int position)
   {
      if(3 > position)
      {
         return TYPE_TITLE;
      }
      else
      {
         return 3 == position ? TYPE_DIVIDER : TYPE_TAG;
      }
   }

   @Override
   public
   int getViewTypeCount()
   {
      return TYPES.length;
   }
}
