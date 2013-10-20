package com.poloure.simplerss;

import java.io.File;

class Constants
{
   static final String SEPAR         = File.separator;
   static final String NL            = System.getProperty("line.separator");
   /* Formats */
   static final String INDEX_FORMAT  = "feed|%s|url|%s|tag|%s|";
   /* Appends */
   static final String TXT           = ".txt";
   static final String READ_ITEMS    = "read_items" + TXT;
   static final String FILTER_LIST   = "filter_list" + TXT;
   static final String CONTENT       = "content" + TXT;
   static final String INDEX         = "index" + TXT;
   static final String LOG_FILE      = "dump" + TXT;
   static final String ITEM_LIST     = "item_list" + TXT;
   /* Folders */
   static final String THUMBNAIL_DIR = "thumbnails" + SEPAR;
   static final String SETTINGS_DIR  = "settings" + SEPAR;
   static final String ADD           = "add";
   static final String EDIT          = "edit";
   /* Other things. */
}
