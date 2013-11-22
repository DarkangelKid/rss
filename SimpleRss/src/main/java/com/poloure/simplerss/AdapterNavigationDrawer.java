package com.poloure.simplerss;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class AdapterNavigationDrawer extends BaseAdapter
{
   private static final int[] NAV_ICONS = {
         R.drawable.action_feeds, R.drawable.action_manage, R.drawable.action_settings,
   };
   private static final int TYPE_TITLE = 0;
   private static final int TYPE_DIVIDER = 1;
   private static final int TYPE_TAG = 2;
   private static final int[] TYPES = {TYPE_TITLE, TYPE_DIVIDER, TYPE_TAG};
   private static final int[] EMPTY_INT_ARRAY = new int[0];
   private static final Typeface SANS_SERIF_LITE = Typeface.create("sans-serif-light",
         Typeface.NORMAL);
   private static final int COLOR_DIVIDER = Color.parseColor("#888888");
   private final int m_twelveDp;
   private final String[] m_navigationTitles;
   private final Context m_context;
   private final LayoutInflater m_layoutInflater;
   private String[] m_tagArray = new String[0];
   private int[] m_unreadArray = EMPTY_INT_ARRAY;

   AdapterNavigationDrawer(String[] navigationTitles, Context context, int twelveDp,
         LayoutInflater layoutInflater)
   {
      m_navigationTitles = navigationTitles.clone();
      m_context = context;
      m_layoutInflater = layoutInflater;
      m_twelveDp = twelveDp;
   }

   void setArrays(String[] tags, int[] unreadCounts)
   {
      m_tagArray = tags.clone();
      m_unreadArray = unreadCounts.clone();
   }

   @Override
   public
   int getCount()
   {
      return m_tagArray.length + 4;
   }

   @Override
   public
   String getItem(int position)
   {
      return 0 == m_unreadArray.length ? "" : Integer.toString(m_unreadArray[position]);
   }

   @Override
   public
   long getItemId(int position)
   {
      return (long) position;
   }

   @Override
   public
   View getView(int position, View convertView, ViewGroup parent)
   {
      View view = convertView;
      int viewType = getItemViewType(position);

      /* These are fine. */
      if(TYPE_TITLE == viewType)
      {
         if(null == view)
         {
            view = m_layoutInflater.inflate(R.layout.navigation_drawer_main_item, parent, false);
         }

         ((TextView) view).setText(m_navigationTitles[position]);

         /* Set the item's image as a CompoundDrawable of the textView. */
         ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(NAV_ICONS[position], 0, 0, 0);
         ((TextView) view).setCompoundDrawablePadding(m_twelveDp);
      }
      else if(TYPE_DIVIDER == viewType && null == view)
      {
         String tagsTitle = m_context.getString(R.string.feed_tag_title);
         view = ViewSettingsHeader.newInstance(m_context, COLOR_DIVIDER, 12.0F);
         /* TODO Dip */
         view.setPadding(32, 8, 32, 8);
         ((TextView) view).setText(tagsTitle);
         ((TextView) view).setTextColor(Color.WHITE);
      }
      else if(TYPE_TAG == viewType)
      {
         boolean isNewView = null == convertView;

         view = isNewView ? new TextView(m_context) : convertView;

         if(isNewView)
         {
            Resources resources = m_context.getResources();
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();

            float minHeightFloat = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42.0F,
                  displayMetrics);
            int minHeight = Math.round(minHeightFloat);

            float paddingSides = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16.0F,
                  displayMetrics);
            int padding = Math.round(paddingSides);

            view.setPadding(padding, 0, padding, 0);
            ((TextView) view).setMinHeight(minHeight);
            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0F);
            ((TextView) view).setGravity(Gravity.CENTER_VERTICAL);
            ((TextView) view).setTextColor(Color.WHITE);
            ((TextView) view).setTypeface(SANS_SERIF_LITE);
         }

         /* TODO Add unread count without a two extra views each. */
         // String number = Integer.toString(m_unreadArray[position - 4]);
         //String unreadText = "0".equals(number) ? "" : number;
         String tagTitle = m_tagArray[position - 4];

         ((TextView) view).setText(tagTitle);
      }
      return view;
   }

   @Override
   public
   boolean isEnabled(int position)
   {
      return 3 != position;
   }

   @Override
   public
   int getItemViewType(int position)
   {
      if(3 > position)
      {
         return TYPE_TITLE;
      }

      else
      {
         return 3 == position ? TYPE_DIVIDER : TYPE_TAG;
      }
   }

   @Override
   public
   int getViewTypeCount()
   {
      return TYPES.length;
   }
}
