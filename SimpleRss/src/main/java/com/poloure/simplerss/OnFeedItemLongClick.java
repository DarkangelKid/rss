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

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

/* This is the context menu that appears when you long click a feed item (card). */
class OnFeedItemLongClick implements AdapterView.OnItemLongClickListener
{
   private final Context m_context;

   OnFeedItemLongClick(Context context)
   {
      m_context = context;
   }

   @Override
   public
   boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
   {
      String urlView = ((ViewCustom) view).m_linkFull;
      String link = urlView.trim();

      DialogInterface.OnClickListener onClick = new OnContextMenuClick(link);

      AlertDialog.Builder build = new AlertDialog.Builder(m_context);
      build.setItems(R.array.card_click_menu, onClick);
      build.show();

      return true;
   }

   class OnContextMenuClick implements DialogInterface.OnClickListener
   {
      private final CharSequence m_url;

      OnContextMenuClick(CharSequence url)
      {
         m_url = url;
      }

      @Override
      public
      void onClick(DialogInterface dialog, int position)
      {
         if(0 == position)
         {
            ClipboardManager clipboard = (ClipboardManager) m_context
                  .getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clip = ClipData.newPlainText("Url", m_url);
            clipboard.setPrimaryClip(clip);

            Toast toast = Toast.makeText(m_context, "URL Copied: " + m_url, Toast.LENGTH_SHORT);
            toast.show();
         }
         else if(1 == position)
         {
            Uri uri = Uri.parse((String) m_url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            m_context.startActivity(intent);
         }
      }
   }
}
