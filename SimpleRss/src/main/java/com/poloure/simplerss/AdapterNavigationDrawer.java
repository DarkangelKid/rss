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
import android.widget.ArrayAdapter;
import android.widget.TextView;

class AdapterNavigationDrawer extends ArrayAdapter<NavItem>
{
   private static final int[] NAV_ICONS = {
         R.drawable.action_feeds, R.drawable.action_manage, R.drawable.action_settings,
   };

   private static
   class Paddings
   {
      static final int V_MAIN = Utilities.getDp(8.0F);
      static final int H_MAIN = 0;
      static final int V_TAG = Utilities.getDp(8.0F);
      static final int H_TAG = Utilities.getDp(16.0F);
      static final int V_DIV = Utilities.getDp(4.0F);
      static final int H_DIV = Utilities.getDp(16.0F);
      static final int COMPOUND_DRAWABLE = Utilities.getDp(12.0F);
   }

   private static
   class TextSizes
   {
      static final float MAIN = 20.0F;
      static final float DIV = 14.0F;
      static final float TAG = 16.0F;
   }

   private static final int MIN_HEIGHT_MAIN = Utilities.getDp(48.0F);
   private static final int MIN_HEIGHT_TAG = Utilities.getDp(42.0F);

   private static
   class Types
   {
      static final int TITLE = 0;
      static final int DIVIDER = 1;
      static final int TAG = 2;
   }

   private static final Typeface SANS_SERIF_LITE = Typeface.create("sans-serif-light",
         Typeface.NORMAL);

   private final String[] m_navigationTitles;
   private final Context m_context;

   AdapterNavigationDrawer(Context context)
   {
      super(context, android.R.id.list);
      m_navigationTitles = context.getResources().getStringArray(R.array.navigation_titles);
      m_context = context;
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

         switch(viewType)
         {
            case Types.TITLE:
               view.setMinHeight(MIN_HEIGHT_MAIN);
               view.setPadding(Paddings.V_MAIN, Paddings.H_MAIN, Paddings.V_MAIN, Paddings.H_MAIN);
               view.setTextSize(TypedValue.COMPLEX_UNIT_SP, TextSizes.MAIN);
               view.setCompoundDrawablePadding(Paddings.COMPOUND_DRAWABLE);
               break;
            case Types.DIVIDER:
               ColorDrawable divider = new ColorDrawable(Color.parseColor("#888888"));
               divider.setBounds(0, 0, parent.getWidth(), 3);

               view.setCompoundDrawables(null, null, null, divider);
               view.setCompoundDrawablePadding(Paddings.V_MAIN);
               view.setPadding(Paddings.H_DIV, Paddings.V_DIV, Paddings.H_DIV, Paddings.V_DIV);
               view.setText(m_context.getString(R.string.tag_title));
               view.setTextSize(TypedValue.COMPLEX_UNIT_SP, TextSizes.DIV);
               break;
            case Types.TAG:
               view.setMinHeight(MIN_HEIGHT_TAG);
               view.setTextSize(TypedValue.COMPLEX_UNIT_SP, TextSizes.TAG);

               /* This is what allows us to put two lines on the same line in one view. */
               view.setPadding(Paddings.H_TAG, -Paddings.V_TAG, Paddings.H_TAG, Paddings.V_TAG);
               view.setLineSpacing(0.0F, 0.1F);
         }
      }

      switch(viewType)
      {
         case Types.TITLE:
            view.setText(m_navigationTitles[position]);
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(NAV_ICONS[position], 0, 0, 0);
            break;
         case Types.TAG:
            int count = getItem(position - 4).m_count;
            String tag = getItem(position - 4).m_title;

            /* This RTL (char) 0x200F allows the unread counter to be aligned to the right. */
            String allTag = m_context.getString(R.string.all_tag);
            tag = Utilities.isTextRtl(allTag) ? (char) 0x200F + tag + '\n' + (char) 0x200E
                  : (char) 0x200E + tag + '\n' + (char) 0x200F;

            view.setText(tag + (0 == count ? "" : Utilities.getLocaleInt(count)));
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
   int getCount()
   {
      /* Because we have the extra four views that are not tags. */
      return super.getCount() + 4;
   }

   @Override
   public
   int getItemViewType(int position)
   {
      return 3 > position ? Types.TITLE : 3 == position ? Types.DIVIDER : Types.TAG;
   }

   @Override
   public
   int getViewTypeCount()
   {
      return 3;
   }
}
