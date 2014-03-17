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

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewFragment;
import android.widget.AdapterView;

class OnItemClickWebView implements AdapterView.OnItemClickListener
{
   private final FeedsActivity m_activity;

   OnItemClickWebView(FeedsActivity activity)
   {
      m_activity = activity;
   }

   static
   boolean usingTwoPaneLayout(Activity activity)
   {
      Display display = activity.getWindowManager().getDefaultDisplay();
      DisplayMetrics outMetrics = new DisplayMetrics();
      display.getMetrics(outMetrics);
      float density = activity.getResources().getDisplayMetrics().density;
      return outMetrics.widthPixels / density >= 600;
   }

   @Override
   public
   void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
   {
      FragmentManager manager = m_activity.getFragmentManager();
      Fragment fragment = manager.findFragmentById(R.id.fragment_web);

      WebView webView = ((WebViewFragment) fragment).getWebView();
      webView.loadData(((ViewFeedItem) view).m_item.m_content, "text/html; charset=UTF-8", null);

      if(!usingTwoPaneLayout(m_activity))
      {
         FragmentUtils.switchToFragment(m_activity, R.id.fragment_web, true);

         /* Form the better url. */
         String url = ((ViewFeedItem) view).m_item.m_url;
         int index = url.indexOf('/');

         /* Configure the actionbar. */
         ActionBar bar = m_activity.getActionBar();
         bar.setTitle(url.substring(-1 == index ? 0 : index + 2).replace("www.", ""));
         bar.setSubtitle(null);
         bar.setIcon(R.drawable.ic_action_web_site);
         m_activity.m_FragmentDrawer.m_drawerToggle.setDrawerIndicatorEnabled(false);

         m_activity.invalidateOptionsMenu();
      }
   }
}
