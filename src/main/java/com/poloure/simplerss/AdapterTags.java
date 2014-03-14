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
   static final int TYPE_PLAIN = 0;
   static final int TYPE_IMAGE = 1;
   static final int TYPE_IMAGE_SANS_DESCRIPTION = 2;
   static final int TYPE_PLAIN_SANS_DESCRIPTION = 3;
   private static final float READ_OPACITY = 0.5F;

   /* We use indexOf on this Long List so it can not be a Set. */ List<Long> m_times = new ArrayList<Long>(0);
   List<FeedItem> m_feedItems = new ArrayList<FeedItem>(0);
   private final Context m_context;

   AdapterTags(Context context)
   {
      m_context = context;
   }

   @Override
   public
   int getItemViewType(int position)
   {
      FeedItem feedItem = m_feedItems.get(position);

      boolean isImage = !feedItem.m_imageLink.isEmpty();
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

      ViewFeedItem view = null != convertView ? (ViewFeedItem) convertView : new ViewFeedItem(m_context, viewType);
      FeedItem item = m_feedItems.get(position);

      /* Apply the read effect. */
      boolean isRead = READ_ITEM_TIMES.contains(item.m_time);
      view.setAlpha(isRead ? READ_OPACITY : 1.0F);
      view.setBackgroundResource(isRead ? R.drawable.selector_transparent : R.drawable.selector_white);

      /* If the recycled view is the view we want, keep it. */
      if(null != convertView)
      {
         if(item.m_time.equals(view.m_item.m_time))
         {
            return view;
         }
      }

      /* Set the information. */
      view.m_item = item;
      view.m_hasImage = hasImg;

      /* If the view was an image, load the image. */
      if(hasImg)
      {
         view.setBitmap(null);
         view.setTag(item.m_time);
         AsyncLoadImage.newInstance(view, item.m_imageName, item.m_time);
      }

      return view;
   }
}
