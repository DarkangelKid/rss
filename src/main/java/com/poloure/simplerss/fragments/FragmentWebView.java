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

package com.poloure.simplerss.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewFragment;

public
class FragmentWebView extends WebViewFragment
{
    public
    FragmentWebView()
    {
    }

    @Override
    public
    void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);

        // If hidden, reset the WebView for the next item.
        if(hidden)
        {
            WebView webView = getWebView();
            if(null != webView)
            {
                webView.loadUrl("about:blank");
            }
        }
    }

    @Override
    public
    void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public
    boolean onOptionsItemSelected(MenuItem item)
    {
        // If the back button in the ActionBar is selected, call the Activity's onBackPressed().
        if(android.R.id.home == item.getItemId())
        {
            Activity activity = getActivity();
            activity.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public
    View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        WebView view = (WebView) super.onCreateView(inflater, container, savedInstanceState);

        // Configure the WebView.
        WebSettings settings = view.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        return view;
    }
}
