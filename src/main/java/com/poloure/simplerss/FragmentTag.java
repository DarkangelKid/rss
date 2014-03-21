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
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

class FragmentTag extends ListFragment
{
   private static final int LIST_VIEW_ID_BASE = 20000;
   private static final String POSITION_KEY = "POSITION";

   static
   Fragment newInstance(int position)
   {
      Fragment fragment = new FragmentTag();
      Bundle bundle = new Bundle();
      bundle.putInt(POSITION_KEY, position);
      fragment.setArguments(bundle);
      return fragment;
   }

   static
   AdapterFavourites getFavouritesAdapter(Activity activity)
   {
      FragmentManager manager = activity.getFragmentManager();
      ListFragment fragment = (ListFragment) manager.findFragmentById(R.id.fragment_favourites);
      return (AdapterFavourites) fragment.getListAdapter();
   }

   private static
   void addToFavourites(Activity activity, FeedItem item)
   {
      AdapterFavourites adapter = getFavouritesAdapter(activity);
      adapter.m_feedItems.add(item);
      adapter.notifyDataSetChanged();

      Toast.makeText(activity, activity.getString(R.string.toast_added_feed, item.m_title), Toast.LENGTH_SHORT)
            .show();
   }

   @Override
   public
   void onListItemClick(ListView l, View v, int position, long id)
   {
      Utilities.showWebFragment(v);
   }

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      LinearLayout view = (LinearLayout) inflater.inflate(R.layout.list_view, container, false);
      TextView emptyView = (TextView) view.findViewById(android.R.id.empty);
      emptyView.setText(R.string.empty_tag_list_view);
      return view;
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      ListView listView = getListView();

      listView.setId(LIST_VIEW_ID_BASE + getArguments().getInt(POSITION_KEY));
      listView.setOnScrollListener(new AbsListView.OnScrollListener()
      {
         private static final int TOUCH = AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
         private static final int IDLE = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

         @Override
         public
         void onScrollStateChanged(AbsListView view, int scrollState)
         {
            if(TOUCH == scrollState || IDLE == scrollState)
            {
               Adapter adapter = view.getAdapter();
               int first = view.getFirstVisiblePosition();
               int last = view.getLastVisiblePosition();

               for(int i = 0; last - first >= i; i++)
               {
                  View viewItem = view.getChildAt(i);

                  if(null != viewItem && viewItem.isShown() && 0 <= viewItem.getTop())
                  {
                     FeedItem item = (FeedItem) adapter.getItem(first + i);
                     boolean existed = AdapterTags.READ_ITEM_TIMES.add(item.m_time);

                     if(!existed)
                     {
                        /* TODO -1 from the subtitle. */
                     }
                  }
               }
            }
            if(IDLE == scrollState)
            {
               AsyncNavigationAdapter.run(getActivity());
            }
         }

         @Override
         public
         void onScroll(AbsListView v, int fir, int visible, int total)
         {
         }
      });

      registerForContextMenu(listView);
      setListAdapter(new AdapterTags(getActivity()));

      AsyncNewTagAdapters.update((FeedsActivity) getActivity());
   }

   @Override
   public
   boolean onOptionsItemSelected(MenuItem menuItem)
   {
      getActivity().onBackPressed();
      return true;
   }

   @Override
   public
   void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
   {
      super.onCreateContextMenu(menu, v, menuInfo);

      boolean hasImage = ((ViewFeedItem) ((AdapterView.AdapterContextMenuInfo) menuInfo).targetView).m_hasImage;

      /* Inflate the context menu from the xml file. */
      Activity activity = getActivity();
      MenuInflater inflater = activity.getMenuInflater();
      inflater.inflate(R.menu.context_menu, menu);

      /* Show the 'Save image' option only when the view has an image. */
      menu.findItem(R.id.save_image).setVisible(hasImage);

      /* Set the title of the context menu to the feed item's title. */
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
      FeedItem view = (FeedItem) ((AdapterView<ListAdapter>) v).getAdapter().getItem(info.position);
      menu.setHeaderTitle(view.m_title);
   }

   @Override
   public
   boolean onContextItemSelected(MenuItem item)
   {
      /* Get the feed url from the FeedItem. */
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
      FeedItem feedItem = ((ViewFeedItem) info.targetView).m_item;
      String url = feedItem.m_url;

      Activity activity = getActivity();

      switch(item.getItemId())
      {
         case R.id.copy:
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Url", url));

            Toast toast = Toast.makeText(activity, getString(R.string.toast_url_copied) + ' ' + url, Toast.LENGTH_SHORT);
            toast.show();
            return true;

         case R.id.open:
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;

         case R.id.favourite:
            addToFavourites(activity, feedItem);
            return true;

         case R.id.save_image:
            downloadImage(activity, feedItem.m_imageLink, feedItem.m_imageName);

         default:
            return false;
      }
   }

   private static
   void downloadImage(final Activity activity, final String imageUrl, final String imageName)
   {
      if(null != imageUrl && !imageUrl.isEmpty())
      {
         new AsyncTask<Void, Void, Boolean>()
         {
            @Override
            public
            Boolean doInBackground(Void... stuff)
            {
               try
               {
                  File pictureFolder = Utilities.getPicturesFolder(activity);
                  File file = new File(pictureFolder, imageName);

                  InputStream inputStream = new URL(imageUrl).openStream();
                  BufferedInputStream in = new BufferedInputStream(inputStream);

                  FileOutputStream fos = new FileOutputStream(file);
                  BufferedOutputStream out = new BufferedOutputStream(fos);
                  try
                  {
                     byte[] buf = new byte[1024];
                     int offset;
                     while(0 < (offset = in.read(buf)))
                     {
                        out.write(buf, 0, offset);
                     }
                  }
                  finally
                  {
                     in.close();
                     out.close();
                  }
               }
               catch(MalformedURLException e)
               {
                  e.printStackTrace();
                  return false;
               }
               catch(FileNotFoundException e)
               {
                  e.printStackTrace();
                  return false;
               }
               catch(IOException e)
               {
                  e.printStackTrace();
                  return false;
               }
               return true;
            }

            @Override
            public
            void onPostExecute(Boolean result)
            {
               if(result)
               {
                  String appName = activity.getString(R.string.application_name);
                  String success = activity.getString(R.string.image_downloaded_success, appName);
                  Toast toast = Toast.makeText(activity, success, Toast.LENGTH_SHORT);
                  toast.show();
               }
               else
               {
                  String failed = activity.getString(R.string.image_downloaded_failed);
                  Toast toast = Toast.makeText(activity, failed, Toast.LENGTH_SHORT);
                  toast.show();
               }
            }
         }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      }
   }
}
