package com.poloure.simplerss;

import android.app.AlertDialog;
import android.content.DialogInterface;

class OnDialogClickPositive implements DialogInterface.OnClickListener
{
   private final String  m_oldFeedTitle;
   private final String m_applicationFolder;
   private final String m_allTag;

   OnDialogClickPositive(String oldFeedTitle, String applicationFolder, String allTag)
   {
      m_oldFeedTitle = oldFeedTitle;
      m_applicationFolder = applicationFolder;
      m_allTag = allTag;
   }

   @Override
   public
   void onClick(DialogInterface dialog, int which)
   {
      AsyncCheckFeed.newInstance((AlertDialog) dialog, m_oldFeedTitle, m_applicationFolder,
            m_allTag);
   }
}
