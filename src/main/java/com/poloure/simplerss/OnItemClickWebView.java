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

import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;

import static com.poloure.simplerss.Constants.*;

class OnItemClickWebView implements AdapterView.OnItemClickListener
{
   @Override
   public
   void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
   {
      WebView webView = s_fragmentWeb.getWebView();
      webView.loadData(((ViewFeedItem) view).m_item.m_content, "text/html; charset=UTF-8", null);

      /* Read the item. */
      AdapterTags.READ_ITEM_TIMES.add(((ViewFeedItem) view).m_item.m_time);
      AsyncNavigationAdapter.run(s_activity);

      view.setAlpha(AdapterTags.READ_OPACITY);
      view.setBackgroundResource(R.drawable.selector_transparent);

      if(!FeedsActivity.usingTwoPaneLayout(s_activity))
      {
         Utilities.switchToFragment(s_fragmentWeb, true);
         s_fragmentManager.executePendingTransactions();

         /* Form the better url. */
         String url = ((ViewFeedItem) view).m_item.m_url;
         int index = url.indexOf('/');
         String urlWithoutHttp = url.substring(-1 == index ? 0 : index + 2);
         String urlWithoutWww = urlWithoutHttp.replace("www.", "");

         /* Configure the actionbar. */
         s_actionBar.setTitle(urlWithoutWww);
         s_actionBar.setSubtitle(null);
         s_actionBar.setIcon(R.drawable.ic_action_web_site);
         s_drawerToggle.setDrawerIndicatorEnabled(false);
         s_drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

         s_activity.invalidateOptionsMenu();
      }
   }
}
