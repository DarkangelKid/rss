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

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Environment;
import android.support.v4.text.TextDirectionHeuristicsCompat;
import android.support.v4.widget.DrawerLayout;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import static com.poloure.simplerss.Constants.*;

class Utilities
{
    static
    String formatTags(String... tags)
    {
        String tagsWithBraces = Arrays.toString(tags);
        return tagsWithBraces.substring(1, tagsWithBraces.length() - 1);
    }

    static
    boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    static
    void showWebFragment(FeedsActivity activity, ViewFeedItem view)
    {
        WebView webView = s_fragmentWeb.getWebView();
        webView.loadData(view.m_item.m_content, "text/html; charset=UTF-8", null);

        // Read the item.
        activity.readItem(view.m_item.m_time);
        AsyncNavigationAdapter.run(activity);

        // Apply read effect only if it is not in the favourites fragment.
        view.setRead(s_fragmentFeeds.isVisible());

        if(!FeedsActivity.canFitTwoFragments())
        {
            switchToFragment(s_fragmentWeb, true);
            s_fragmentManager.executePendingTransactions();

            // Form the better url.
            String url = view.m_item.m_url;
            int index = url.indexOf('/');
            String urlWithoutHttp = url.substring(-1 == index ? 0 : index + 2);
            String urlWithoutWww = urlWithoutHttp.replace("www.", "");

            // Configure the actionbar.
            s_actionBar.setTitle(urlWithoutWww);
            s_actionBar.setSubtitle(null);
            s_actionBar.setIcon(R.drawable.ic_action_web_site);
            s_drawerToggle.setDrawerIndicatorEnabled(false);
            s_drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            activity.invalidateOptionsMenu();
        }
    }

    private static
    void switchToFragment(Fragment fragment, boolean addToBackStack)
    {
        if(fragment.isHidden())
        {
            Fragment[] fragments = {
                    s_fragmentFavourites, s_fragmentManage, s_fragmentFeeds, s_fragmentSettings
            };
            FragmentTransaction transaction = s_fragmentManager.beginTransaction();

            for(Fragment frag : fragments)
            {
                if(frag.isVisible())
                {
                    transaction.hide(frag);
                }
            }
            transaction.show(fragment);
            if(addToBackStack)
            {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        }
    }

    static
    void setTitlesAndDrawerAndPage(Fragment fragment, int absolutePos)
    {
        String[] navTitles = s_resources.getStringArray(R.array.navigation_titles);

        if(null != fragment)
        {
            switchToFragment(fragment, false);
            s_fragmentManager.executePendingTransactions();
        }

        ListView list = s_fragmentDrawer.m_listView;
        HeaderViewListAdapter headerAdapter = (HeaderViewListAdapter) list.getAdapter();
        int headers = headerAdapter.getHeadersCount();

        int listPosition = -10 == absolutePos ? s_viewPager.getCurrentItem() + headers : absolutePos;
        int viewPagerPos = -10 == absolutePos ? s_viewPager.getCurrentItem() : absolutePos - headers;

        // Check the drawer item.
        String title = PagerAdapterTags.s_tagList.get(0);
        String subTitle = null;
        int imageRes = R.drawable.ic_action_labels;

        if(s_fragmentFavourites.isVisible())
        {
            listPosition = 0;
            title = navTitles[0];
            imageRes = R.drawable.ic_action_important;
        }
        else if(s_fragmentManage.isVisible())
        {
            listPosition = 1;
            title = navTitles[1];
            imageRes = R.drawable.ic_action_storage;
        }
        else if(s_fragmentSettings.isVisible())
        {
            listPosition = 2;
            title = navTitles[2];
            imageRes = R.drawable.ic_action_settings;
        }
        else
        {
            ArrayAdapter<String[]> adapter = (ArrayAdapter<String[]>) headerAdapter.getWrappedAdapter();

            if(null != adapter && 0 < adapter.getCount())
            {
                String[] item = adapter.getItem(viewPagerPos);
                title = item[0];
                int count = null == item[1] || item[1].isEmpty() ? 0 : Integer.parseInt(item[1]);
                String countString = s_resources.getQuantityString(R.plurals.actionbar_subtitle_unread, count, count);
                subTitle = 0 == count ? null : countString;
            }
        }
        s_actionBar.setTitle(title);
        s_actionBar.setSubtitle(subTitle);
        s_actionBar.setIcon(imageRes);

        list.setItemChecked(listPosition, true);

        // If we must change the view pager page.
        if(0 <= viewPagerPos)
        {
            // Switch the view pager page if different.
            if(s_viewPager.getCurrentItem() != viewPagerPos)
            {
                s_viewPager.setCurrentItem(viewPagerPos);
            }
        }
    }

    static
    XmlPullParser createXmlParser(CharSequence urlString) throws IOException, XmlPullParserException
    {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();

        URL url = new URL(urlString.toString());
        InputStream inputStream = url.openStream();
        parser.setInput(inputStream, null);
        return parser;
    }

    static
    boolean isTextRtl(CharSequence c)
    {
        return TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR.isRtl(c, 0, c.length() - 1);
    }
}
