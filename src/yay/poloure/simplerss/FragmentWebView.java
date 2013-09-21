package yay.poloure.simplerss;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;

public class FragmentWebView
{
   static class fragment_webview extends Fragment
   {
      WebView     web_view;
      FrameLayout view;
      TextView    text;

      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container,
                               Bundle savedInstanceState)
      {
         if(null != web_view)
         {
            web_view.destroy();
         }

         view = new FrameLayout(Util.getContext());
         web_view = new WebView(Util.getContext());
         //view.addView(web_view, LayoutParams.MATCH_PARENT);

         /*text = new TextView();
         text.setText("webview");
         text.setGravity(Gravity.CENTER);
         text.setVisibility(View.GONE);
         view.addView(text, android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);*/

         return view;
      }

      @Override
      public void onPause()
      {
      /* min api 11. */
         web_view.onPause();
         super.onPause();
      }

      @Override
      public void onResume()
      {
         /* min api 11. */
         web_view.onResume();
         super.onResume();
      }

      @Override
      public void onDestroy()
      {
         if(null != web_view)
         {
            view.removeAllViews();
            web_view.removeAllViews();
            web_view.destroy();
            web_view = null;
            view = null;
         }
         super.onDestroy();
      }

      WebView get_webview()
      {
         return web_view;
      }
   }
}
