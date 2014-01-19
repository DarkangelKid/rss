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
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class AdapterTags extends BaseAdapter
{
   static final Set<Long> READ_ITEM_TIMES = Collections.synchronizedSet(new HashSet<Long>(0));
   private static final int TYPE_PLAIN = 0;
   private static final int TYPE_IMAGE = 1;
   private static final int TYPE_IMAGE_SANS_DESCRIPTION = 2;
   private static final int TYPE_PLAIN_SANS_DESCRIPTION = 3;

   /* We use indexOf on this Long List so it can not be a Set. */
   final List<Long> m_times = new ArrayList<>(0);
   final List<FeedItem> m_feedItems = new ArrayList<>(0);
   private final Context m_context;
   boolean m_isReadingItems = true;

   AdapterTags(Context context)
   {
      m_context = context;
   }

   @Override
   public
   int getItemViewType(int position)
   {
      FeedItem feedItem = m_feedItems.get(position);

      boolean isImage = !feedItem.m_imageName.isEmpty();
      boolean isDes = !feedItem.m_desLines[0].isEmpty();

      return isImage ? isDes ? TYPE_IMAGE : TYPE_IMAGE_SANS_DESCRIPTION : isDes ? TYPE_PLAIN : TYPE_PLAIN_SANS_DESCRIPTION;
   }

   @Override
   public
   int getCount()
   {
      return m_feedItems.size();
   }

   @Override
   public
   int getViewTypeCount()
   {
      return 4;
   }

   @Override
   public
   Object getItem(int position)
   {
      return m_feedItems.get(position);
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
      int viewType = getItemViewType(position);

      boolean hasImg = TYPE_IMAGE == viewType || TYPE_IMAGE_SANS_DESCRIPTION == viewType;
      boolean hasDes = TYPE_PLAIN == viewType || TYPE_IMAGE == viewType;

      /* TODO These are pix not Dip. */
      ViewCustom view = null != convertView ? (ViewCustom) convertView : new ViewCustom(m_context, hasImg ? hasDes ? 564 : 464 : hasDes ? 184 : 94);

      /* Set the information. */
      FeedItem item = m_feedItems.get(position);
      boolean isRead = READ_ITEM_TIMES.contains(item.m_time);
      view.m_item = item;

      view.setAlpha(isRead && !hasImg ? 0.5F : 1.0F);

      /* The logic that tells whether the item is Read or not. */
      if(parent.isShown() && position + 1 < m_feedItems.size() && m_isReadingItems)
      {
         FeedItem nextItem = m_feedItems.get(position + 1);
         READ_ITEM_TIMES.add(nextItem.m_time);
      }

      /* If the view was an image, load the image. */
      if(hasImg)
      {
         view.setBitmap(null);
         view.setTag(position);
         AsyncLoadImage.newInstance(view, item.m_imageName, position, isRead ? 0.5F : 1.0F);
      }

      return view;
   }
}
