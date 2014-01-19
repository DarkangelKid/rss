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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class AdapterNavigationDrawer extends ArrayAdapter<NavItem>
{
   private static final int[] NAV_ICONS = {
         R.drawable.action_feeds, R.drawable.action_manage, R.drawable.action_settings,
   };

   private static final int TITLE = 0;
   private static final int TAG = 1;

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
      int viewType = getItemViewType(position);
      LayoutInflater inflater = LayoutInflater.from(m_context);

      int id = TITLE == viewType ? R.layout.navigation_text_view_main : R.layout.navigation_text_view_tag;

      TextView view = (TextView) (null == convertView ? inflater.inflate(id, null) : convertView);

      if(TITLE == viewType)
      {
         view.setText(m_navigationTitles[position]);
         view.setCompoundDrawablesRelativeWithIntrinsicBounds(NAV_ICONS[position], 0, 0, 0);
      }
      else
      {
         int count = getItem(position - 3).m_count;
         String tag = getItem(position - 3).m_title;

         /* This RTL (char) 0x200F allows the unread counter to be aligned to the right. */
         String allTag = m_context.getString(R.string.all_tag);
         tag = Utilities.isTextRtl(allTag) ? (char) 0x200F + tag + Write.NEW_LINE + (char) 0x200E : (char) 0x200E + tag + Write.NEW_LINE + (char) 0x200F;

         view.setText(tag + (0 == count ? "" : Utilities.getLocaleInt(count)));
      }
      return view;
   }

   @Override
   public
   int getCount()
   {
      /* Because we have the extra three views that are not tags. */
      return super.getCount() + 3;
   }

   @Override
   public
   int getItemViewType(int position)
   {
      return 3 > position ? TITLE : TAG;
   }

   @Override
   public
   int getViewTypeCount()
   {
      return 2;
   }
}
