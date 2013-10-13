package com.poloure.simplerss;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

class Write
{
   /* All functions in here must check that the media is available before
    * continuing. */

   /* This function should be safe, returns false if it failed.
    * NOT SAFE FOR INTERNAL IF FAILS. */
   static
   int removeLine(String path, CharSequence stringSearch, boolean contains, Context context)
   {
      /* If s_storage is unmounted OR if we force to use external. */
      if(Util.isUnmounted())
      {
         return -1;
      }

      String path1 = Util.getStorage(context) + path;
      String tempPath = path1 + Constants.TEMP;

      String[] lines = new String[0];
      int pos = -1;
      try
      {
         BufferedWriter out = null;
         try
         {
            /* Read the file to an array, if the file does not exist, return. */
            lines = Read.file(path1, context);
            if(0 == lines.length)
            {
               return -1;
            }

            /* No backup for internal s_storage. */
            out = Util.isUsingSd()
                  ? writer(tempPath, false)
                  : writer(path1, Context.MODE_PRIVATE, context);

            int line = 0;
            for(String item : lines)
            {
               if(contains && !item.contains(stringSearch) ||
                     !contains && !item.equals(stringSearch))
               {
                  out.write(item + Constants.NL);
                  line++;
               }
               else
               {
                  pos = line;
               }
            }
         }
         finally
         {
            if(null != out)
            {
               out.close();
            }
         }
      }
      /* If writing to the temp file fails, delete the temp file and return. */
      catch(FileNotFoundException e)
      {
         e.printStackTrace();
         return -1;
      }
      catch(UnsupportedEncodingException e)
      {
         e.printStackTrace();
         return -1;
      }
      catch(IOException e)
      {
         e.printStackTrace();
         if(Util.isUsingSd())
         {
            Util.remove(tempPath, context);
         }
         return -1;
      }

      /* If the rename failed, delete the file and Write the original. */
      if(Util.isUsingSd())
      {
         boolean success = !Util.move(path + Constants.TEMP, path, context);
         if(success)
         {
            Util.remove(path, context);
            collection(path, Arrays.asList(lines), context);
         }
      }
      return pos;
   }

   private static
   BufferedWriter writer(String path, int writeMode, Context context)
         throws FileNotFoundException, UnsupportedEncodingException
   {
      String path1 = Util.getInternalPath(path);
      FileOutputStream fileOutputStream = context.openFileOutput(path1, writeMode);
      return new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF8"));
   }

   private static
   BufferedWriter writer(String p, boolean ap) throws IOException
   {
      return new BufferedWriter(new FileWriter(p, ap));
   }

   static
   void collection(String path, Iterable<?> content, Context context)
   {
      /* If s_storage is unmounted OR if we force to use external. */
      if(Util.isUnmounted())
      {
         return;
      }

      String path1 = Util.getStorage(context) + path;

      if(Util.isUsingSd())
      {
         Util.remove(path1, context);
      }

      try
      {
         BufferedWriter out = null;
         try
         {
            /* Create the buffered writer. */
            out = Util.isUsingSd()
                  ? writer(path1, false)
                  : writer(path1, Context.MODE_PRIVATE, context);

            for(Object item : content)
            {
               out.write(item + Constants.NL);
            }
         }
         finally
         {
            if(null != out)
            {
               out.close();
            }
         }
      }
      catch(FileNotFoundException e)
      {
         e.printStackTrace();
      }
      catch(UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }

   static
   void log(String text, Context context)
   {
      single(Constants.DUMP_FILE, text + Constants.NL, context);
   }

   /* Function should be safe, returns false if fails. */
   static
   void single(String path, String stringToWrite, Context context)
   {
      /* If s_storage is unmounted OR if we force to use external. */
      if(Util.isUnmounted())
      {
         return;
      }

      String path1 = Util.getStorage(context) + path;

      try
      {
         BufferedWriter out = null;
         try
         {
            out = Util.isUsingSd()
                  ? writer(path1, true)
                  : writer(path1, Context.MODE_APPEND, context);
            out.write(stringToWrite);
         }
         finally
         {
            if(null != out)
            {
               out.close();
            }
         }
      }
      catch(FileNotFoundException e)
      {
         e.printStackTrace();
      }
      catch(UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }

   static
   void log(int integer, Context context)
   {
      single(Constants.DUMP_FILE, integer + Constants.NL, context);
   }

   static
   void log(Iterable<?> content, Context context)
   {
      collection(Constants.DUMP_FILE + ".iter.txt", content, context);
   }
}
