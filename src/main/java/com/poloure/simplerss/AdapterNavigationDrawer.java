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
   AdapterNavigationDrawer(Context context)
   {
      super(context, android.R.id.list);
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      ViewNavItem view = null == convertView ? new ViewNavItem(parent.getContext()) : (ViewNavItem) convertView;

      view.m_text = getItem(position)[0];
      view.m_count = getItem(position)[1];
      view.m_image = -1;

      return view;
   }
}
