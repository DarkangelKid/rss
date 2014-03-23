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

class AdapterManage extends ArrayAdapter<String[]>
{
   private final LayoutInflater m_inflater;
   private TextView m_count;
   private TextView m_url;
   private TextView m_tags;

   AdapterManage(Context context)
   {
      super(context, android.R.id.list);
      m_inflater = LayoutInflater.from(context);
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      View layout = convertView;
      if(layout == null)
      {
         layout = m_inflater.inflate(R.layout.manage_text_view, null, false);
         m_count = (TextView) layout.findViewById(R.id.manage_count);
         m_url = (TextView) layout.findViewById(R.id.manage_url);
         m_tags = (TextView) layout.findViewById(R.id.manage_tags);
      }

      String[] item = getItem(position);
      m_count.setText(item[0]);
      m_url.setText(item[1]);
      m_tags.setText(item[2]);

      return layout;
   }
}
