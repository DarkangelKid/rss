package com.poloure.simplerss;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.Set;

class Read
{
   static final String INDEX = "index.txt";
   private static final String UTF8 = "UTF-8";
   private static final char ITEM_SEPARATOR = '|';

   /* All functions in here must check that the media is available before
    * continuing. */

   static
   String setting(String path, Context context)
   {
      if(isUnmounted())
      {
         return null;
      }

      String[] check = file(path, context);
      return 0 == check.length ? "" : check[0];
   }

   /* This function is now safe. It will return a zero length array on error. */
   static
   String[] file(String path, Context context)
   {
      String path1 = path;
      if(isUnmounted())
      {
         return new String[0];
      }

      String storage = FeedsActivity.getStorage(context);
      if(!path1.contains(storage))
      {
         path1 = FeedsActivity.getStorage(context) + path1;
      }

      /* Get the number of lines. */
      int count = count(path1, context);

      /* If the file is empty, return a zero length array. */
      if(0 == count)
      {
         return new String[0];
      }

      /* Use the count to allocate memory for the array. */
      String[] lines = new String[count];

      /* Begin reading the file to the String array. */
      try
      {
         BufferedReader in = null;
         try
         {
            in = Write.isUsingSd() ? reader(path1) : reader(path1, context);

            int linesLength = lines.length;
            for(int i = 0; i < linesLength; i++)
            {
               lines[i] = in.readLine();
            }
         }
         finally
         {
            if(null != in)
            {
               in.close();
            }
         }
      }
      catch(FileNotFoundException ignored)
      {
         //e.printStackTrace();
         return new String[0];
      }
      catch(UnsupportedEncodingException e)
      {
         e.printStackTrace();
         return new String[0];
      }
      catch(IOException e)
      {
         e.printStackTrace();
         return new String[0];
      }

      return lines;
   }

   /* For reading from the internal s_storage. */
   private static
   BufferedReader reader(String path, Context context)
         throws FileNotFoundException, UnsupportedEncodingException
   {
      String path1 = Write.getInternalPath(path);
      FileInputStream fis = context.openFileInput(path1);
      return new BufferedReader(new InputStreamReader(fis, UTF8));
   }

   /* Wrapper for creating external BufferedReader. */
   private static
   BufferedReader reader(String path) throws FileNotFoundException
   {
      return new BufferedReader(new FileReader(path));
   }

   static
   int count(String path, Context context)
   {
      String path1 = path;
      if(isUnmounted())
      {
         return 0;
      }

      String storage = FeedsActivity.getStorage(context);
      if(!path1.contains(storage))
      {
         path1 = FeedsActivity.getStorage(context) + path1;
      }

      int i = 0;
      try
      {
         BufferedReader in = null;
         try
         {
            in = Write.isUsingSd() ? reader(path1) : reader(path1, context);

            while(null != in.readLine())
            {
               i++;
            }
         }
         finally
         {
            if(null != in)
            {
               in.close();
            }
         }
      }
      catch(FileNotFoundException ignored)
      {
         //e.printStackTrace();
      }
      catch(UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
      return i;
   }

   static
   String[][] indexFile(Context context)
   {
      return csvFile(INDEX, context, 'f', 'u', 't');
   }

   static
   String[][] csvFile(String path, Context context, char... type)
   {
      if(isUnmounted())
      {
         return new String[type.length][0];
      }

      String[] lines = file(path, context);
      if(0 == lines.length)
      {
         return new String[type.length][0];
      }

      String[][] types = new String[type.length][lines.length];

      int linesLength = lines.length;
      for(int j = 0; j < linesLength; j++)
      {
         int offset = 0;
         String line = lines[j];
         int next;

         while(-1 != (next = line.indexOf(ITEM_SEPARATOR, offset)))
         {
            if(offset == line.length())
            {
               break;
            }

            char ch = line.charAt(offset);
            offset = next + 1;

            int typeLength = type.length;
            for(int i = 0; i < typeLength; i++)
            {
               if(ch == type[i])
               {
                  next = line.indexOf(ITEM_SEPARATOR, offset);
                  types[i][j] = line.substring(offset, next);
                  break;
               }
            }
            offset = line.indexOf(ITEM_SEPARATOR, offset) + 1;
         }
      }
      return types;
   }

   /* TODO Not for internal yet. */
   static
   Set<Long> longSet(String path, Context context)
   {
      Set<Long> longSet = new LinkedHashSet<Long>(0);

      /* If storage is unmounted OR if we force to use external. */
      if(isUnmounted())
      {
         return longSet;
      }

      String filePath = FeedsActivity.getStorage(context) + path;
      File fileIn = new File(filePath);

      try
      {
         FileInputStream fileInputStream = new FileInputStream(fileIn);
         BufferedInputStream in = new BufferedInputStream(fileInputStream);
         DataInputStream data = new DataInputStream(in);
         try
         {
            while(true)
            {
               long lon = data.readLong();
               longSet.add(lon);
            }
         }
         catch(EOFException ignored)
         {
         }
         finally
         {
            data.close();
         }

      }
      catch(FileNotFoundException ignored)
      {
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
      return longSet;
   }

   static
   boolean isUnmounted()
   {
      if(!Write.isUsingSd())
      {
         return false;
      }

      /* Check to see if we can Write to the media. */
      String mounted = Environment.MEDIA_MOUNTED;
      String externalStorageState = Environment.getExternalStorageState();
      return !mounted.equals(externalStorageState);
   }
}
