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

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public
class ListFragmentTag extends Fragment
{
   private static final String POSITION_KEY = "POSITION";
   static boolean s_hasScrolled;
   ListView m_listView;
   static boolean s_firstLoad = true;

   static
   Fragment newInstance(int position)
   {
      Fragment fragment = new ListFragmentTag();
      Bundle bundle = new Bundle();
      bundle.putInt(POSITION_KEY, position);
      fragment.setArguments(bundle);
      return fragment;
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      final Activity activity = getActivity();

      m_listView = new ListView(activity);
      m_listView.setId(20000 + getArguments().getInt(POSITION_KEY));
      m_listView.setDivider(new ColorDrawable(getResources().getColor(R.color.item_separator)));
      m_listView.setDividerHeight(1);
      m_listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
      m_listView.setOnScrollListener(new AbsListView.OnScrollListener()
      {
         @Override
         public
         void onScrollStateChanged(AbsListView view, int scrollState)
         {
            if(AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState)
            {
               AsyncNavigationAdapter.update(activity);
            }
            if(!s_hasScrolled && AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == scrollState)
            {
               s_hasScrolled = true;

               Adapter adapter = m_listView.getAdapter();
               int first = m_listView.getFirstVisiblePosition();
               int last = m_listView.getLastVisiblePosition();

               for(int i = 0; last - first >= i; i++)
               {
                  View view1 = m_listView.getChildAt(i);
                  if(null != view1 && view1.isShown())
                  {
                     FeedItem item = (FeedItem) adapter.getItem(first + i);
                     AdapterTags.READ_ITEM_TIMES.add(item.m_time);
                  }
               }
            }
         }

         @Override
         public
         void onScroll(AbsListView v, int fir, int visible, int total)
         {
         }
      });
      registerForContextMenu(m_listView);

      return m_listView;
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      m_listView.setAdapter(new AdapterTags(getActivity()));
      if(s_firstLoad)
      {
         AsyncNewTagAdapters.update(getActivity());
         s_firstLoad = false;
      }
   }

   @Override
   public
   void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
   {
      super.onCreateContextMenu(menu, v, menuInfo);

      getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
      FeedItem view = (FeedItem) ((AdapterView<ListAdapter>) v).getAdapter().getItem(info.position);
      menu.setHeaderTitle(view.m_title);
   }

   @Override
   public
   boolean onContextItemSelected(MenuItem item)
   {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
      FeedItem feedItem = ((ViewFeedItem) info.targetView).m_item;
      String url = feedItem.m_urlFull;
      Context context = getActivity();

      if(R.id.copy == item.getItemId())
      {
         ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
         clipboard.setPrimaryClip(ClipData.newPlainText("Url", url));

         Toast toast = Toast.makeText(context, getString(R.string.toast_url_copied) + ' ' + url, Toast.LENGTH_SHORT);
         toast.show();
         return true;
      }
      if(R.id.open == item.getItemId())
      {
         context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
         return true;
      }
      return false;
   }
}
