package com.poloure.simplerss;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

class FragmentWebView extends Fragment
{
   private WebView m_webView;

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

      m_webView = new WebView(context);

      return m_webView;
   }

   @Override
   public
   void onDestroy()
   {
      if(null != m_webView)
      {
         m_webView.removeAllViews();
         m_webView.destroy();
      }
      super.onDestroy();
   }

   void setData(String url, String html)
   {
      m_webView.loadDataWithBaseURL(url, html, null, null, null);
   }

   void setUrl(String url)
   {
      m_webView.loadUrl(url);
   }
}
