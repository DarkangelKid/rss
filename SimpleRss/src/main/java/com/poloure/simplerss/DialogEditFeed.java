package com.poloure.simplerss;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

class DialogEditFeed extends Dialog
{
   private final String m_applicationFolder;
   static final int POSITIVE_BUTTON = 2501;
   static final int[] IDS = {2601, 2602, 2603};
   private static final int[] HINTS = {
         R.string.feed_name_hint, R.string.feed_url_hint, R.string.feed_tag_hint
   };
   private static final int[] TEXTS = {
         R.string.feed_name_dialog, R.string.feed_url_dialog, R.string.feed_tag_dialog
   };
   private static final int[] BTEXTS = {R.string.cancel_dialog, R.string.accept_dialog};
   private final Activity m_activity;
   private final int m_position;

   private
   DialogEditFeed(Activity activity, int position, String applicationFolder)
   {
      super(activity, android.R.style.Theme_Holo_Light_Dialog);
      m_activity = activity;
      m_position = position;
      m_applicationFolder = applicationFolder;
   }

   static
   Dialog newInstance(Activity activity, int position, String applicationFolder)
   {
      return new DialogEditFeed(activity, position, applicationFolder);
   }

   @Override
   protected
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      /* Get 8Dip. */
      Resources resources = m_activity.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      float eightDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0F, metrics);
      int eight = Math.round(eightDp);

      /* Get the current tags. */
      int tagListSize = PagerAdapterFeeds.TAG_LIST.size();
      String[] tags = PagerAdapterFeeds.TAG_LIST.toArray(new String[tagListSize]);

      /* Configure the ViewGroup. */
      LinearLayout layout = new LinearLayout(m_activity);
      layout.setOrientation(LinearLayout.VERTICAL);
      layout.setPadding(eight, eight, eight, 0);

      /* Make the tag EditText. */
      int oneLine = android.R.layout.simple_dropdown_item_1line;
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(m_activity, oneLine, tags);
      MultiAutoCompleteTextView tagEdit = new MultiAutoCompleteTextView(m_activity);
      tagEdit.setAdapter(adapter);
      tagEdit.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

      EditText[] editTexts = {new EditText(m_activity), new EditText(m_activity), tagEdit};

      for(int i = 0; 3 > i; i++)
      {
         editTexts[i].setId(IDS[i]);
         editTexts[i].setSingleLine(true);
         editTexts[i].setHint(HINTS[i]);

         TextView textView = new TextView(m_activity);
         textView.setText(TEXTS[i]);
         layout.addView(textView);
         layout.addView(editTexts[i]);
      }

      /* Create the button listener. */
      View.OnClickListener negativeButtonClick = new View.OnClickListener()
      {
         @Override
         public
         void onClick(View v)
         {
            dismiss();
         }
      };

      View.OnClickListener positiveButtonClick;

      /* Read the feed item's information from disk if we are editing. */
      if(-1 == m_position)
      {
         positiveButtonClick = new OnClickPositive(this, "");
      }
      else
      {
         String[][] content = Read.csvFile(Read.INDEX, m_applicationFolder, 'f', 'u', 't');

         for(int i = 0; 3 > i; i++)
         {
            editTexts[i].setText(content[i][m_position]);
         }

         positiveButtonClick = new OnClickPositive(this, content[0][m_position]);
      }

      View.OnClickListener[] onClickListeners = {negativeButtonClick, positiveButtonClick};

      /* Create the buttons. */
      LinearLayout buttonLayout = new LinearLayout(m_activity);
      buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
      buttonLayout.setWeightSum(1.0f);
      Button[] buttons = new Button[2];

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.5f);

      for(int i = 0; 2 > i; i++)
      {
         buttons[i] = new Button(m_activity);
         buttons[i].setText(BTEXTS[i]);
         buttons[i].setLayoutParams(params);
         buttons[i].setBackgroundResource(R.drawable.dialog_select);
         buttons[i].setOnClickListener(onClickListeners[i]);
         buttonLayout.addView(buttons[i]);
      }

      buttons[1].setId(POSITIVE_BUTTON);

      /* Create the top layout. */
      LinearLayout topLayout = new LinearLayout(m_activity);
      topLayout.setOrientation(LinearLayout.VERTICAL);
      topLayout.addView(layout);
      topLayout.addView(buttonLayout);

      setContentView(topLayout);
   }

   private
   class OnClickPositive implements View.OnClickListener
   {
      private final Dialog m_dialog;
      private final String m_oldFeed;

      OnClickPositive(Dialog dialog, String oldFeed)
      {
         m_dialog = dialog;
         m_oldFeed = oldFeed;
      }

      @Override
      public
      void onClick(View v)
      {
         AsyncCheckFeed.newInstance(m_activity, m_dialog, m_oldFeed, m_applicationFolder);
      }
   }
}
