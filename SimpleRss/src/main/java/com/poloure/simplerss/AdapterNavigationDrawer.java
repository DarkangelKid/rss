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
import java.util.List;

class AdapterNavigationDrawer extends BaseAdapter
{
   private static final int[] NAV_ICONS = {
         R.drawable.action_feeds, R.drawable.action_manage, R.drawable.action_settings,
   };
   private static final int MIN_HEIGHT_MAIN = Utilities.getDp(48.0F);
   private static final int MIN_HEIGHT_TAG = Utilities.getDp(42.0F);
   private static final int VPADDING_DIV = Utilities.getDp(4.0F);
   private static final int VPADDING_MAIN = Utilities.getDp(8.0F);
   private static final int HPADDING_TAG = Utilities.getDp(16.0F);
   private static final int PADDING_COMPOUND_DRAWABLE = Utilities.getDp(12.0F);
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
   List<String> m_tagArray = new ArrayList<>(0);
   int[] m_unreadArray = EMPTY_INT_ARRAY;

   AdapterNavigationDrawer(String[] navigationTitles, Context context)
   {
      m_navigationTitles = navigationTitles.clone();
      m_context = context;
   }

   @Override
   public
   int getCount()
   {
      return m_tagArray.size() + 4;
   }

   /* This is used to set the subtitle count. */
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
      TextView view = (TextView) convertView;
      int viewType = getItemViewType(position);

      if(null == convertView)
      {
         view = new TextView(m_context);
         view.setTextColor(Color.WHITE);
         view.setTypeface(SANS_SERIF_LITE);
         view.setGravity(Gravity.CENTER_VERTICAL);
      }

      if(TYPE_TITLE == viewType)
      {
         if(null == convertView)
         {
            view.setMinHeight(MIN_HEIGHT_MAIN);
            view.setPadding(VPADDING_MAIN, 0, VPADDING_MAIN, 0);
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_MAIN);
            view.setCompoundDrawablePadding(PADDING_COMPOUND_DRAWABLE);
         }

         view.setText(m_navigationTitles[position]);
         view.setCompoundDrawablesWithIntrinsicBounds(NAV_ICONS[position], 0, 0, 0);
      }
      else if(TYPE_DIVIDER == viewType && null == convertView)
      {
         String tagsTitle = m_context.getString(R.string.navigation_drawer_tag_title);

         int drawerWidth = parent.getWidth();
         GREY_LINE.setBounds(0, 0, drawerWidth, 3);

         view.setCompoundDrawables(null, null, null, GREY_LINE);
         view.setCompoundDrawablePadding(VPADDING_MAIN);
         view.setPadding(HPADDING_TAG, VPADDING_DIV, HPADDING_TAG, VPADDING_DIV);
         view.setText(tagsTitle);
         view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0F);
      }
      else if(TYPE_TAG == viewType)
      {
         if(null == convertView)
         {
            view.setMinHeight(MIN_HEIGHT_TAG);
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0F);

            /* This is what allows us to put two lines on the same line in one view. */
            view.setPadding(HPADDING_TAG, -VPADDING_MAIN, HPADDING_TAG, VPADDING_MAIN);
            view.setLineSpacing(0.0F, 0.1F);
         }

         int count = m_unreadArray[position - 4];
         String tag = m_tagArray.get(position - 4);

         /* This RTL (char) 0x200F allows the unread counter to be aligned to the right. */
         view.setText(tag + '\n' + (char) 0x200F + (0 == count ? "" : count));
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
      return 3 > position ? TYPE_TITLE : 3 == position ? TYPE_DIVIDER : TYPE_TAG;
   }

   @Override
   public
   int getViewTypeCount()
   {
      return TYPES.length;
   }
}
