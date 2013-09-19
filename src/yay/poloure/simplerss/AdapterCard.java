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
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

class AdapterCard extends BaseAdapter
{
   String[]  m_links;
   private String[]  m_titles;
   private String[]  m_descriptions;
   private String[]  m_images;
   private Integer[] m_heights;
   private Integer[] m_widths;

   static Set<String> read_items = Read.set(FeedsActivity.READ_ITEMS);

   private static final Pattern PATTERN_THUMBNAILS = Pattern.compile("thumbnails");
   static int eight;
   private static final int SCREEN_WIDTH = Util.getScreenWidth();

   private boolean m_firstGetItem = true;
   private ListView listview;
   boolean m_touchedScreen = true;

   public AdapterCard()
   {
      if(0 == eight)
      {
         float density = Util.getContext().getResources().getDisplayMetrics().density;
         eight = (int) (8.0F * density + 0.5f);
      }
   }

   void prependArray(String[] titles, String[] descriptions, String[] links, String[] images,
                     Integer[] heights, Integer[] widths)
   {
      m_titles = Util.concat(titles, m_titles);
      m_descriptions = Util.concat(descriptions, m_descriptions);
      m_links = Util.concat(links, m_links);
      m_images = Util.concat(images, m_images);
      m_heights = Util.concat(heights, m_heights);
      m_widths = Util.concat(widths, m_widths);
   }

   @Override
   public int getCount()
   {
      return m_titles.length;
   }

   @Override
   public long getItemId(int position)
   {
      return position;
   }

   @Override
   public String getItem(int position)
   {
      return m_titles[position];
   }

   @Override
   public int getViewTypeCount()
   {
      return 4;
   }

   @Override
   public int getItemViewType(int position)
   {
      boolean img = null != m_widths[position] && 0 != m_widths[position];

      boolean des = null != m_descriptions[position] && !m_descriptions[position].isEmpty();

      if(img && des)
      {
         return 0;
      }
      if(img)
      {
         return 1;
      }
      if(des)
      {
         return 2;
      }
      return 3;
   }

   @Override
   public View getView(int position, View cv, ViewGroup parent)
   {
      int viewType = getItemViewType(position);

      if(m_firstGetItem)
      {
         listview = (ListView) parent;
         listview.setScrollingCacheEnabled(false);
         listview.setOnScrollListener(new AbsListView.OnScrollListener()
         {
            @Override
            public void onScroll(AbsListView v, int fir, int visible, int total)
            {
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
               if(listview.getChildAt(0).getTop() == eight &&
                  View.VISIBLE == listview.getVisibility() && m_touchedScreen)
               {
                  read_items.add(m_links[0]);
               }

               if(AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState)
               {
                  Update.navigation();
               }
            }
         });
         m_firstGetItem = false;
      }

      LayoutInflater inflater = Util.getLayoutInflater();
      String link = m_links[position];
      String title = m_titles[position];

      /* card_full.xml img && m_des. */
      if(0 == viewType)
      {
         FullHolder holder;
         if(null == cv)
         {
            cv = inflater.inflate(R.layout.card_full, parent, false);
            holder = new FullHolder();
            holder.m_title = (TextView) cv.findViewById(R.id.title);
            holder.m_url = (TextView) cv.findViewById(R.id.url);
            holder.m_des = (TextView) cv.findViewById(R.id.description);
            holder.m_imageView = (ImageView) cv.findViewById(R.id.image);
            cv.setOnClickListener(new WebviewMode());
            cv.setOnLongClickListener(new CardLongClick());
            cv.setTag(holder);
         }
         else
         {
            holder = (FullHolder) cv.getTag();
         }

         displayImage(holder.m_imageView, position);

         holder.m_title.setText(title);
         holder.m_des.setText(m_descriptions[position]);
         holder.m_url.setText(link);
      }
      /* card_no_des_img.xml no description, Image, m_title. */
      else if(1 == viewType)
      {
         ImgHolder holder;
         if(null == cv)
         {
            cv = inflater.inflate(R.layout.card_no_des_img, parent, false);
            holder = new ImgHolder();
            holder.title = (TextView) cv.findViewById(R.id.title);
            holder.url = (TextView) cv.findViewById(R.id.url);
            holder.image = (ImageView) cv.findViewById(R.id.image);
            cv.setOnClickListener(new WebviewMode());
            cv.setOnLongClickListener(new CardLongClick());
            cv.setTag(holder);
         }
         else
         {
            holder = (ImgHolder) cv.getTag();
         }

         displayImage(holder.image, position);

         holder.title.setText(title);
         holder.url.setText(link);
      }
      /* card_des_no_img.xml no Image, descirition, m_title. */
      else if(2 == viewType)
      {
         DesHolder holder;
         if(null == cv)
         {
            cv = inflater.inflate(R.layout.card_des_no_img, parent, false);
            holder = new DesHolder();
            holder.title = (TextView) cv.findViewById(R.id.title);
            holder.url = (TextView) cv.findViewById(R.id.url);
            holder.des = (TextView) cv.findViewById(R.id.description);
            cv.setOnClickListener(new WebviewMode());
            cv.setOnLongClickListener(new CardLongClick());
            cv.setTag(holder);
         }
         else
         {
            holder = (DesHolder) cv.getTag();
         }

         holder.title.setText(title);
         holder.des.setText(m_descriptions[position]);
         holder.url.setText(link);
      }
      /* No description or Image. */
      else if(3 == viewType)
      {
         BlankHolder holder;
         if(null == cv)
         {
            cv = inflater.inflate(R.layout.card_no_des_no_img, parent, false);
            holder = new BlankHolder();
            holder.title = (TextView) cv.findViewById(R.id.title);
            holder.url = (TextView) cv.findViewById(R.id.url);
            cv.setOnClickListener(new WebviewMode());
            cv.setOnLongClickListener(new CardLongClick());
            cv.setTag(holder);
         }
         else
         {
            holder = (BlankHolder) cv.getTag();
         }

         holder.title.setText(title);
         holder.url.setText(link);
      }

      /* The logic that tells whether the item is Read or not. */
      if(View.VISIBLE == listview.getVisibility() && 0 <= position - 1 && m_touchedScreen)
      {
         read_items.add(this.m_links[position - 1]);
      }

      return cv;
   }

   void displayImage(ImageView v, int p)
   {
      v.setImageDrawable(new ColorDrawable(Color.WHITE));
      LayoutParams lp = v.getLayoutParams();

      lp.height = (int) ((double) SCREEN_WIDTH / m_widths[p] * m_heights[p]);
      v.setLayoutParams(lp);
      v.setTag(p);

      if(FeedsActivity.HONEYCOMB)
      {
         (new Image()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, v, p);
      }
      else
      {
         (new Image()).execute(v, p);
      }
   }

   class Image extends AsyncTask<Object, Void, Object[]>
   {
      ImageView m_imageView;
      int       tag;

      @Override
      protected Object[] doInBackground(Object... params)
      {
         m_imageView = (ImageView) params[0];
         tag = (Integer) params[1];
         BitmapFactory.Options o = new BitmapFactory.Options();
         o.inSampleSize = 1;
         Animation fadeIn = new AlphaAnimation(0.0F, 1.0F);
         fadeIn.setDuration(240L);
         fadeIn.setInterpolator(new DecelerateInterpolator());
         String image = Util.getStorage() +
                        PATTERN_THUMBNAILS.matcher(m_images[tag]).replaceAll("m_images");
         m_imageView.setOnClickListener(new ImageClick(image));
         return new Object[]{
               BitmapFactory.decodeFile(Util.getStorage() + m_images[tag], o), fadeIn
         };
      }

      @Override
      protected void onPostExecute(Object... result)
      {
         if((Integer) m_imageView.getTag() != tag)
         {
            return;
         }
         if(null != m_imageView && null != result[0])
         {
            m_imageView.setImageBitmap((Bitmap) result[0]);
            m_imageView.startAnimation((Animation) result[1]);
            if((Integer) m_imageView.getTag() != tag)
            {
               return;
            }
            m_imageView.setVisibility(View.VISIBLE);
         }
      }
   }


   static class FullHolder
   {
      TextView  m_title;
      TextView  m_url;
      TextView  m_des;
      ImageView m_imageView;
   }

   static class DesHolder
   {
      TextView title;
      TextView url;
      TextView des;
   }

   static class ImgHolder
   {
      TextView  title;
      TextView  url;
      ImageView image;
   }

   static class BlankHolder
   {
      TextView title;
      TextView url;
   }

   private static class WebviewMode implements View.OnClickListener
   {
      @Override
      public void onClick(View v)
      {
         FeedsActivity.bar.setTitle("Offline");
         NavDrawer.drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
         NavDrawer.drawer_toggle.setDrawerIndicatorEnabled(false);
         FeedsActivity.bar.setDisplayHomeAsUpEnabled(true);
         FeedsActivity.fman.beginTransaction()
                      .hide(FeedsActivity.fman.findFragmentByTag(NavDrawer.NAV_TITLES[0]))
                      .add(R.id.drawer_layout, new FragmentWebView.fragment_webview(), "OFFLINE")
                      .addToBackStack("BACK").commit();
      }
   }

   static class ImageClick implements View.OnClickListener
   {
      final String m_imagePath;

      public ImageClick(String im)
      {
         m_imagePath = im;
      }

      @Override
      public void onClick(View v)
      {
         Intent intent = new Intent();
         intent.setAction(Intent.ACTION_VIEW);
         int index = m_imagePath.lastIndexOf('.') + 1;
         String type = m_imagePath.substring(index, m_imagePath.length());

         Uri uri = Uri.fromFile(new File(m_imagePath));

         if(FeedsActivity.JELLYBEAN)
         {
            intent.setDataAndTypeAndNormalize(uri, "Image" + FeedsActivity.SEPAR + type);
         }
         else
         {
            intent.setDataAndType(uri, "Image" + FeedsActivity.SEPAR + type);
         }

         Util.getContext().startActivity(intent);
      }
   }

   private static class CardLongClick implements View.OnLongClickListener
   {
      @Override
      public boolean onLongClick(View v)
      {
         String longPressUrl = Util.getText(v, R.id.url);
         show_card_dialog(longPressUrl);
         return true;
      }
   }

   private static void show_card_dialog(final String URL)
   {
      String[] menuItems = Util.getArray(R.array.card_menu_image);
      final Context con = Util.getContext();

      AlertDialog cardDialog = new AlertDialog.Builder(con).setCancelable(true).setItems(menuItems,
                                                                                         new DialogInterface.OnClickListener()
                                                                                         {
                                                                                            @Override
                                                                                            public void onClick(
                                                                                                  DialogInterface dialog,
                                                                                                  int position)
                                                                                            {
                                                                                               switch(position)
                                                                                               {
                                                                                                  case 0:
                                                                                                     ClipboardManager
                                                                                                           clipboard
                                                                                                           = (ClipboardManager) con
                                                                                                           .getSystemService(
                                                                                                                 Context.CLIPBOARD_SERVICE);
                                                                                                     ClipData
                                                                                                           clip
                                                                                                           = ClipData
                                                                                                           .newPlainText(
                                                                                                                 "label",
                                                                                                                 URL);
                                                                                                     clipboard
                                                                                                           .setPrimaryClip(
                                                                                                                 clip);
                                                                                                     break;
                                                                                                  case 1:
                                                                                                     con.startActivity(
                                                                                                           new Intent(
                                                                                                                 Intent.ACTION_VIEW,
                                                                                                                 Uri.parse(
                                                                                                                       URL)));
                     /*case(2):
                        break;*/
                                                                                               }
                                                                                            }
                                                                                         })
                                                           .create();

      cardDialog.show();
   }
}
