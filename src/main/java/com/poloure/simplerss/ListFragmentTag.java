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
import android.app.ListFragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public
class ListFragmentTag extends ListFragment
{
   private static final String POSITION_KEY = "POSITION";

   static
   ListFragment newInstance(int position)
   {
      ListFragment listFragment = new ListFragmentTag();
      Bundle bundle = new Bundle();
      bundle.putInt(POSITION_KEY, position);
      listFragment.setArguments(bundle);
      return listFragment;
   }

   @Override
   public
   void onActivityCreated(Bundle savedInstanceState)
   {
      super.onActivityCreated(savedInstanceState);

      Activity activity = getActivity();
      int position = getArguments().getInt(POSITION_KEY);
      String applicationFolder = FeedsActivity.getApplicationFolder(activity);

      setListAdapter(new AdapterTags(activity, applicationFolder));

      ListView listView = getListView();
      listView.setOnScrollListener(
            new OnScrollFeed(activity, applicationFolder, position, listView.getPaddingTop()));
      listView.setDividerHeight(0);
      registerForContextMenu(listView);

      if(0 == position)
      {
         AsyncReloadTagPage.newInstance(0, listView, applicationFolder, true);
      }
   }

   @Override
   public
   void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
   {
      super.onCreateContextMenu(menu, v, menuInfo);
      getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
      menu.setHeaderTitle("URL");
   }

   @Override
   public
   boolean onContextItemSelected(MenuItem item)
   {
      AdapterView.AdapterContextMenuInfo info
            = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
      String url = ((ViewCustom) info.targetView).m_item.m_urlFull;
      Context context = getActivity();

      if(R.id.copy == item.getItemId())
      {
         ClipboardManager clipboard = (ClipboardManager) context.getSystemService(
               Context.CLIPBOARD_SERVICE);
         clipboard.setPrimaryClip(ClipData.newPlainText("Url", url));

         Toast toast = Toast.makeText(context, getString(R.string.toast_url_copied) + ' ' + url,
               Toast.LENGTH_SHORT);
         toast.show();
         return true;
      }
      else
      {
         context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
         return true;
      }
   }
}
