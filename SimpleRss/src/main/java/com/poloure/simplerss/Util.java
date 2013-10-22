package com.poloure.simplerss;

import android.content.Context;
import android.os.Environment;

import java.io.File;

class Util
{
   static final int[]      EMPTY_INT_ARRAY           = new int[0];
   static final String[]   EMPTY_STRING_ARRAY        = new String[0];
   static final String[][] EMPTY_STRING_STRING_ARRAY = new String[0][0];

   static
   boolean isUnmounted()
   {
      if(!isUsingSd())
      {
         return false;
      }

      /* Check to see if we can Write to the media. */
      String mounted = Environment.MEDIA_MOUNTED;
      String externalStorageState = Environment.getExternalStorageState();
      return !mounted.equals(externalStorageState);
   }

   static
   boolean isUsingSd()
   {
      /* Return true if force sd setting is true. */
      return true;
   }

   static
   String getInternalPath(String externalPath)
   {
      int index = externalPath.indexOf(File.separatorChar + "files" + File.separatorChar);
      String substring = externalPath.substring(index + 7);
      return substring.replaceAll(File.separator, "-");
   }

   /* Replaces ALL_TAG '/'s with '-' to emulate a folder directory layout in
    * data/data. */
   static
   boolean deleteDirectory(File directory)
   {
      if(directory.isDirectory())
      {
         for(String child : directory.list())
         {
            boolean success = !deleteDirectory(new File(directory, child));
            if(success)
            {
               return false;
            }
         }
      }
      return directory.delete();
   }

   static
   boolean moveFile(String original, String resulting, Context context)
   {
      String storage = FeedsActivity.getStorage(context);

      File originalFile = new File(storage + original);
      File resultingFile = new File(storage + resulting);

      return originalFile.renameTo(resultingFile);
   }

}
