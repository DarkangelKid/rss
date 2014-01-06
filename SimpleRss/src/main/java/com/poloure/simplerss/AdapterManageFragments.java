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
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterManageFragments extends BaseAdapter
{
   private static final int COLOR_TEXT = Color.parseColor("#4d4d4d");
   private final Context m_context;
   private Editable[] m_editables = new Editable[0];

   AdapterManageFragments(Context context)
   {
      m_context = context;
   }

   void setEditable(Editable[] titleArray)
   {
      m_editables = titleArray.clone();
   }

   @Override
   public
   int getCount()
   {
      return m_editables.length;
   }

   @Override
   public
   Editable getItem(int position)
   {
      return m_editables[position];
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
      boolean isNewView = null == convertView;

      TextView view = (TextView) convertView;
      if(isNewView)
      {
         Resources resources = m_context.getResources();
         DisplayMetrics metrics = resources.getDisplayMetrics();

         float eightDpFloat = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0F, metrics);
         int eightDp = Math.round(eightDpFloat);

         view = new TextView(m_context);
         view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0F);
         view.setTextColor(COLOR_TEXT);
         view.setPadding(eightDp, eightDp, eightDp, eightDp);
         view.setGravity(Gravity.CENTER_VERTICAL);
      }

      view.setText(m_editables[position]);
      return view;
   }
}
