package com.poloure.simplerss;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

class FragmentWebView extends Fragment
{
   private WebView     m_webView;
   private FrameLayout view;

   @Override
   public
   View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      super.onCreateView(inflater, container, savedInstanceState);

      if(null != m_webView)
      {
         m_webView.destroy();
      }

      Context context = getActivity();

      view = new FrameLayout(context);
      m_webView = new WebView(context);

      return view;
   }

   void setData(String url, String html)
   {
      m_webView.loadDataWithBaseURL(url, html, null, null, null);
   }

   void setUrl(String url)
   {
      m_webView.loadUrl(url);
   }

   @Override
   public
   void onResume()
   {
         /* min api 11. */
      m_webView.onResume();
      super.onResume();
   }

   @Override
   public
   void onPause()
   {
      /* min api 11. */
      m_webView.onPause();
      super.onPause();
   }

   @Override
   public
   void onDestroy()
   {
      if(null != m_webView)
      {
         view.removeAllViews();
         m_webView.removeAllViews();
         m_webView.destroy();
      }
      super.onDestroy();
   }
}
