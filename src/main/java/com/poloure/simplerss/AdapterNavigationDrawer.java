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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

class AdapterNavigationDrawer extends ArrayAdapter<String[]>
{
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
      int type = getItemViewType(position);
      ViewNavItem view = null == convertView ? new ViewNavItem(m_context) : (ViewNavItem) convertView;

      view.m_text = TITLE == type ? m_navigationTitles[position] : getItem(position - 2)[0];
      view.m_count = TITLE == type ? "" : getItem(position - 2)[1];
      view.m_image = TITLE == type ? position : -1;

      return view;
   }

   @Override
   public
   int getCount()
   {
      /* Because we have the extra three views that are not tags. */
      return super.getCount() + 2;
   }

   @Override
   public
   int getItemViewType(int position)
   {
      return 2 > position ? TITLE : TAG;
   }

   @Override
   public
   int getViewTypeCount()
   {
      return 2;
   }
}
