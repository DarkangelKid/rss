package com.poloure.simplerss;

import android.os.Build;

import java.util.Locale;

class Constants
{
   static final         String  SEPAR               = System.getProperty("file.separator");
   static final         String  IMAGE_TYPE          = "image" + SEPAR;
   static final         String  NL                  = System.getProperty("line.separator");
   static final         Locale  LOCALE              = Locale.getDefault();
   /* Formats */
   static final         String  INDEX_FORMAT        = "feed|%s|url|%s|tag|%s|";
   static final         String  FRAGMENT_TAG        = "android:switcher:%s:%d";
   static final         String  FEED_INFO           = "%s" + NL + "%s • %d items";
   /* Appends */
   static final         String  TXT                 = ".txt";
   static final         String  READ_ITEMS          = "read_items" + TXT;
   static final         String  FILTER_LIST         = "filter_list" + TXT;
   static final         String  TAG_LIST            = "tag_list" + TXT;
   static final         String  CONTENT             = "content" + TXT;
   static final         String  INDEX               = "index" + TXT;
   static final         String  LOG_FILE            = "dump" + TXT;
   /* Files */
   static final         String  INT_STORAGE         = "internal" + TXT;
   static final         String  TEMP                = ".temp" + TXT;
   /* Parser saves */
   static final         String  IMAGE               = "image|";
   static final         String  TIME                = "pubDate|";
   static final         String  HEIGHT              = "height|";
   static final         String  WIDTH               = "width|";
   static final         String  TAG_TITLE           = "<title";
   static final         String  ENDTAG_TITLE        = "</title>";
   /* Other things. */
   private static final String  THUMBNAILS          = "thumbnails";
   private static final String  SETTINGS            = "settings";
   private static final int     VER                 = Build.VERSION.SDK_INT;
   /* Folders */
   static final         String  THUMBNAIL_DIR       = THUMBNAILS + SEPAR;
   static final         String  SETTINGS_DIR        = SETTINGS + SEPAR;
   static final         String  UNSORTED_TAG        = "Unsorted";
   static final         String  ADD                 = "add";
   static final         String  EDIT                = "edit";
   static final         boolean JELLYBEAN           = Build.VERSION_CODES.JELLY_BEAN <= VER;
   static final         boolean HONEYCOMB           = Build.VERSION_CODES.HONEYCOMB <= VER;
   static final         boolean FROYO               = Build.VERSION_CODES.FROYO <= VER;
   static final         String  ALARM_SERVICE_START = "start";
   static final         String  ALARM_SERVICE_STOP  = "stop";
}
