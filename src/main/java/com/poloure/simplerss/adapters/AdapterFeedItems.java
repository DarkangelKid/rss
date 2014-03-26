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

package com.poloure.simplerss.adapters;

import android.view.View;
import android.view.ViewGroup;

import com.poloure.simplerss.AsyncLoadImage;
import com.poloure.simplerss.Constants;
import com.poloure.simplerss.FeedItem;
import com.poloure.simplerss.FeedsActivity;
import com.poloure.simplerss.ViewFeedItem;

import java.util.Map;

public
class AdapterFeedItems extends LinkedMapAdapter<Long, FeedItem>
{
    private final FeedsActivity m_activity;

    public
    AdapterFeedItems(FeedsActivity activity, Map<Long, FeedItem> map)
    {
        super(map);
        m_activity = activity;
    }

    @Override
    public
    View getView(int position, View convertView, ViewGroup parent)
    {
        Type type = Type.values()[getItemViewType(position)];
        FeedItem item = getItem(position);
        boolean recycled = null != convertView;
        boolean hasImage = Type.IMAGE == type || Type.IMAGE_SANS_DESCRIPTION == type;

        ViewFeedItem view = recycled ? (ViewFeedItem) convertView : new ViewFeedItem(m_activity, type);

        boolean isRead = m_activity.isItemRead(item.m_time);
        boolean shouldInvalidate = view.m_isViewRead != isRead;

        // Apply the read effect.
        if(view.m_isViewRead != isRead || !recycled)
        {
            // If the favourites fragment is visible, set as false.
            view.setRead(Constants.s_fragmentFavourites.isHidden() ? isRead : false);
        }

        // If the recycled view is the view we want, keep it.
        if(recycled && item.m_time.equals(view.m_item.m_time))
        {
            if(shouldInvalidate)
            {
                view.invalidate();
            }
            return view;
        }

        // Set the information.
        view.m_item = item;

        // If the view was an image, load the image.
        if(hasImage)
        {
            view.setBitmap(null);
            view.setTag(item.m_time);
            AsyncLoadImage.newInstance(view, item.m_imageName, item.m_time);
        }

        return view;
    }

    @Override
    public
    int getItemViewType(int position)
    {
        FeedItem item = getItem(position);

        boolean isDes = !item.m_desLines[0].isEmpty();

        if(item.m_imageLink.isEmpty())
        {
            return isDes ? Type.PLAIN.ordinal() : Type.PLAIN_SANS_DESCRIPTION.ordinal();
        }
        else
        {
            return isDes ? Type.IMAGE.ordinal() : Type.IMAGE_SANS_DESCRIPTION.ordinal();
        }
    }

    @Override
    public
    int getViewTypeCount()
    {
        return Type.values().length;
    }

    public
    enum Type
    {
        PLAIN,
        IMAGE,
        IMAGE_SANS_DESCRIPTION,
        PLAIN_SANS_DESCRIPTION,
    }
}
