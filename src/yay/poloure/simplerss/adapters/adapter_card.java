package yay.poloure.simplerss;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Set;
import android.net.Uri;
import java.util.regex.Pattern;

class adapter_card extends BaseAdapter
{
   String[] links        = new String[0];
   String[] titles       = new String[0];
   String[] descriptions = new String[0];
   String[] images       = new String[0];
   Integer[] heights     = new Integer[0];
   Integer[] widths      = new Integer[0];

   static Set<String> read_items = read.set(main.READ_ITEMS);

   static final Pattern thumb_img = Pattern.compile("thumbnails");
   static int two = 0, four = 0, eight = 0;
   static final int screen_width = util.get_screen_width();

   boolean   first          = true;
   ListView  listview;
   boolean   touched        = true;

   public adapter_card()
   {
      if(two == 0)
      {
         float density = util.get_context().getResources().getDisplayMetrics().density;
         two      = (int) (2  * density + 0.5f);
         four     = two * 2;
         eight    = two * 4;
      }
   }

   void add_array(String[] new_title, String[] new_des, String[] new_link,
                  String[] new_image, Integer[] new_height, Integer[] new_width)
   {
      titles       = util.concat(new_title,  titles      );
      descriptions = util.concat(new_des,    descriptions);
      links        = util.concat(new_link,   links       );
      images       = util.concat(new_image,  images      );
      heights      = util.concat(new_height, heights     );
      widths       = util.concat(new_width,  widths      );
   }


   @Override
   public int getCount()
   {
      return titles.length;
   }

   @Override
   public long getItemId(int position)
   {
      return position;
   }

   @Override
   public String getItem(int position)
   {
      return titles[position];
   }

   @Override
   public int getViewTypeCount()
   {
      return 4;
   }

   @Override
   public int getItemViewType(int position)
   {
      boolean img = ( widths[position] != null &&
                      widths[position] != 0 );
      boolean des = ( descriptions[position] != null &&
                     !descriptions[position].equals("") );

      if(img && des)
         return 0;
      if(img && !des)
         return 1;
      if(!img && des)
         return 2;
      if(!img && !des)
         return 3;

      return 3;
   }

   @Override
   public View getView(int position, View cv, ViewGroup parent)
   {
      int view_type = getItemViewType(position);

      if(first)
      {
         listview = (ListView) parent;
         listview.setScrollingCacheEnabled(false);
         listview.setOnScrollListener(new AbsListView.OnScrollListener()
         {
            @Override
            public void onScroll(AbsListView v, int first, int visible, int total)
            {
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
               if( listview.getChildAt(0).getTop() == eight &&
                   listview.getVisibility() == View.VISIBLE && touched )
               {
                  read_items.add(links[0]);
               }

               if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
                  update.navigation(null);
            }
         });
         first = false;
      }

      LayoutInflater inflater = util.get_inflater();
      String link  = links[position];
      String title = titles[position];

      /* card_full.xml img && des. */
      if(view_type == 0)
      {
         full_holder holder;
         if(cv == null)
         {
            cv  = inflater.inflate(R.layout.card_full, parent, false);
            holder       = new full_holder();
            holder.title = (TextView)  cv.findViewById(R.id.title);
            holder.url   = (TextView)  cv.findViewById(R.id.url);
            holder.des   = (TextView)  cv.findViewById(R.id.description);
            holder.image = (ImageView) cv.findViewById(R.id.image);
            cv.setOnClickListener(new webview_mode());
            cv.setOnLongClickListener(new long_press());
            cv.setTag(holder);
         }
         else
            holder = (full_holder) cv.getTag();

         display_img(holder.image, position);

         holder.title.setText(title);
         holder.des  .setText(descriptions[position]);
         holder.url  .setText(link);

         //util.set_alpha(holder.title, holder.url, holder.image,
         //               holder.des, link);
      }
      /* card_no_des_img.xml no description, image, title. */
      else if(view_type == 1)
      {
         img_no_des_holder holder;
         if(cv == null)
         {
            cv  = inflater.inflate(R.layout.card_no_des_img, parent, false);
            holder       = new img_no_des_holder();
            holder.title = (TextView)  cv.findViewById(R.id.title);
            holder.url   = (TextView)  cv.findViewById(R.id.url);
            holder.image = (ImageView) cv.findViewById(R.id.image);
            cv.setOnClickListener(new webview_mode());
            cv.setOnLongClickListener(new long_press());
            cv.setTag(holder);
         }
         else
            holder = (img_no_des_holder) cv.getTag();

         display_img(holder.image, position);

         holder.title.setText(title);
         holder.url  .setText(link);

         //util.set_alpha(holder.title, holder.url, holder.image, null, link);
      }
      /* card_des_no_img.xml no image, descirition, title. */
      else if(view_type == 2)
      {
         no_img_des_holder holder;
         if(cv == null)
         {
            cv  = inflater.inflate(R.layout.card_des_no_img, parent, false);
            holder       = new no_img_des_holder();
            holder.title = (TextView) cv.findViewById(R.id.title);
            holder.url   = (TextView) cv.findViewById(R.id.url);
            holder.des   = (TextView) cv.findViewById(R.id.description);
            cv.setOnClickListener(new webview_mode());
            cv.setOnLongClickListener(new long_press());
            cv.setTag(holder);
         }
         else
            holder = (no_img_des_holder) cv.getTag();

         holder.title.setText(title);
         holder.des  .setText(descriptions[position]);
         holder.url  .setText(link);

         //util.set_alpha(holder.title, holder.url, null, holder.des, link);
      }
      /* No description or image. */
      else if(view_type == 3)
      {
         no_img_no_des_holder holder;
         if(cv == null)
         {
            cv  = inflater.inflate(R.layout.card_no_des_no_img, parent, false);
            holder       = new no_img_no_des_holder();
            holder.title = (TextView)  cv.findViewById(R.id.title);
            holder.url   = (TextView)  cv.findViewById(R.id.url);
            cv.setOnClickListener(new webview_mode());
            cv.setOnLongClickListener(new long_press());
            cv.setTag(holder);
         }
         else
            holder = (no_img_no_des_holder) cv.getTag();

         holder.title.setText(title);
         holder.url  .setText(link);

         //util.set_alpha(holder.title, holder.url, null, null, link);
      }

      if(main.HONEYCOMB)
      {
         if(read_items.contains(link))
            cv.setAlpha(0.5f);
         else
            cv.setAlpha(1.0f);
      }

      /* The logic that tells whether the item is read or not. */
      if(listview.getVisibility() == View.VISIBLE && position - 1 >= 0 && touched)
         read_items.add(links[position - 1]);

      return cv;
   }

   void display_img(ImageView view, int position)
   {
      int height  = heights[position];
      int width   = widths[position];

      view.setImageDrawable(new ColorDrawable(Color.WHITE));
      ViewGroup.LayoutParams iv = view.getLayoutParams();

      iv.height = (int) ((((double) screen_width)/(width)) * (height));
      view.setLayoutParams(iv);
      view.setTag(position);

      if(!main.HONEYCOMB)
         (new image()).execute(view, view.getTag());
      else
         (new image()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, view, view.getTag());
   }

   class image extends AsyncTask<Object, Void, Object[]>
   {
      ImageView iv;
      int tag;

      @Override
      protected Object[] doInBackground(Object... params)
      {
         iv    = (ImageView)  params[0];
         tag   = (Integer)    params[1];
         BitmapFactory.Options o = new BitmapFactory.Options();
         o.inSampleSize = 1;
         Animation fadeIn = new AlphaAnimation(0, 1);
         fadeIn.setDuration(240);
         fadeIn.setInterpolator(new DecelerateInterpolator());
         String image = thumb_img.matcher(images[tag]).replaceAll("images");
         iv.setOnClickListener(new image_call(image));
         Object[] ob = {BitmapFactory.decodeFile(images[tag], o), fadeIn};
         return ob;
      }

      @Override
      protected void onPostExecute(Object... result)
      {
         if((Integer) iv.getTag() != tag)
            return;
         if(iv != null && result[0] != null)
         {
            iv.setImageBitmap((Bitmap) result[0]);
            iv.startAnimation((Animation) result[1]);
            if((Integer) iv.getTag() != tag)
               return;
            iv.setVisibility(View.VISIBLE);
         }
      }
   }


   static class full_holder
   {
      TextView title;
      TextView url;
      TextView des;
      ImageView image;
   }

   static class no_img_des_holder
   {
      TextView title;
      TextView url;
      TextView des;
   }

   static class img_no_des_holder
   {
      TextView title;
      TextView url;
      ImageView image;
   }

   static class no_img_no_des_holder
   {
      TextView title;
      TextView url;
   }

   class webview_mode implements View.OnClickListener
   {
      @Override
      public void onClick(View v)
      {
         main.bar.setTitle("Offline");
         navigation_drawer.drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
         navigation_drawer.drawer_toggle.setDrawerIndicatorEnabled(false);
         main.bar.setDisplayHomeAsUpEnabled(true);
         main.fman.beginTransaction()
               .hide(main.fman.findFragmentByTag(navigation_drawer.NAV_TITLES[0]))
               .add(R.id.drawer_layout, new fragment_webview(), "OFFLINE")
               .addToBackStack("BACK")
               .commit();
      }
   }

   class image_call implements View.OnClickListener
   {
      private final String image_path;
      public image_call(String im)
      {
         image_path = im;
      }

      @Override
      public void onClick(View v)
      {
         Intent intent = new Intent();
         intent.setAction(Intent.ACTION_VIEW);
         int index   = image_path.lastIndexOf('.') + 1;
         String type = image_path.substring(index, image_path.length());

         Uri uri = Uri.fromFile(new File(image_path));

         if(!main.JELLYBEAN)
            intent.setDataAndType(uri, "image/" + type);
         else
            intent.setDataAndTypeAndNormalize(uri, "image/" + type);

         util.get_context().startActivity(intent);
      }
   }

   class long_press implements View.OnLongClickListener
   {
      @Override
      public boolean onLongClick(View v)
      {
         String long_press_url = util.getstr(v, R.id.url);
        /* show_card_dialog(context, long_press_url, ((ViewHolder) v.getTag()).image_view.getVisibility());*/
         return true;
      }
   }

   static void show_card_dialog(final Context con, final String URL, final int image_visibility)
   {
      String[] menu_items;
      if(image_visibility != View.VISIBLE)
         menu_items = util.get_array(R.array.card_menu);
      else
         menu_items = util.get_array(R.array.card_menu_image);


      final AlertDialog card_dialog = new AlertDialog.Builder(con)
            .setCancelable(true)
            .setItems(menu_items, new DialogInterface.OnClickListener()
            {
               @Override
               public void onClick(DialogInterface dialog, int position)
               {
                  switch(position)
                  {
                     case(0):
                        ClipboardManager clipboard = (ClipboardManager) con.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", URL);
                        clipboard.setPrimaryClip(clip);
                        break;
                     case(1):
                        con.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL)));
                     /*case(2):
                        break;*/
                  }
               }
            })
            .create();

            card_dialog.show();
   }

   class fragment_webview extends Fragment
   {
      WebView web_view;
      FrameLayout view;
      TextView text;

      public fragment_webview()
      {
      }

      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
      {
         if(web_view != null)
            web_view.destroy();

         view = new FrameLayout(getActivity());
         web_view = new WebView(getActivity());
         view.addView(web_view, LayoutParams.MATCH_PARENT);

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
         if(web_view != null)
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
