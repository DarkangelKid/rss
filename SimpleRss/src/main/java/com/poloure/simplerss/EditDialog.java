package com.poloure.simplerss;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public
class EditDialog extends Dialog
{
   private final String m_oldFeedTitle;
   private final String m_applicationFolder;
   private final String m_allTag;

   static
   Dialog newInstance(Context context, String oldFeedTitle, String applicationFolder, String allTag)
   {
      return new EditDialog(context, oldFeedTitle, applicationFolder, allTag);
   }

   private
   EditDialog(Context context, String oldFeedTitle, String applicationFolder, String allTag)
   {
      super(context);
      m_oldFeedTitle = oldFeedTitle;
      m_applicationFolder = applicationFolder;
      m_allTag = allTag;
   }

   @Override
   protected
   void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.add_rss_dialog);

      Button buttonNegative = (Button) findViewById(R.id.negative_button);
      buttonNegative.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public
         void onClick(View v)
         {
            dismiss();
         }
      });

      Button buttonPositive = (Button) findViewById(R.id.positive_button);
      buttonPositive.setOnClickListener(new OnClickPositive(this));
   }

   private
   class OnClickPositive implements View.OnClickListener
   {
      private final Dialog m_dialog;

      OnClickPositive(Dialog dialog)
      {
         m_dialog = dialog;
      }

      @Override
      public
      void onClick(View v)
      {
         AsyncCheckFeed.newInstance(m_dialog, m_oldFeedTitle, m_applicationFolder, m_allTag);
      }
   }
}
