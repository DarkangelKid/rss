package yay.poloure.simplerss;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class Read
{
   static final String UTF8 = "UTF-8";

   /* All functions in here must check that the media is available before
    * continuing. */

   static
   String setting(String path)
   {
      if(Util.isUnmounted())
      {
         return null;
      }

      String[] check = file(path);
      return 0 == check.length ? "" : check[0];
   }

   /* This function is now safe. It will return a zero length array on error. */
   static
   String[] file(String path)
   {
      String path1 = path;
      if(Util.isUnmounted())
      {
         return Util.EMPTY_STRING_ARRAY;
      }

      String storage = Util.getStorage();
      if(!path1.contains(storage))
      {
         path1 = Util.getStorage() + path1;
      }

      /* Get the number of lines. */
      int count = count(path1);

      /* If the file is empty, return a zero length array. */
      if(0 == count)
      {
         return Util.EMPTY_STRING_ARRAY;
      }

      /* Use the count to allocate memory for the array. */
      String[] lines = new String[count];

      /* Begin reading the file to the String array. */
      try
      {
         BufferedReader in = null;
         try
         {
            in = Util.isUsingSd() ? reader(path1) : reader(path1, UTF8);

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
      catch(FileNotFoundException e)
      {
         //e.printStackTrace();
         return Util.EMPTY_STRING_ARRAY;
      }
      catch(UnsupportedEncodingException e)
      {
         e.printStackTrace();
         return Util.EMPTY_STRING_ARRAY;
      }
      catch(IOException e)
      {
         e.printStackTrace();
         return Util.EMPTY_STRING_ARRAY;
      }

      return lines;
   }

   static
   int count(String path)
   {
      String path1 = path;
      if(Util.isUnmounted())
      {
         return 0;
      }

      String storage = Util.getStorage();
      if(!path1.contains(storage))
      {
         path1 = Util.getStorage() + path1;
      }

      int i = 0;
      try
      {
         BufferedReader in = null;
         try
         {
            in = Util.isUsingSd() ? reader(path1) : reader(path1, UTF8);

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
      catch(FileNotFoundException e)
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

   /* Wrapper for creating external BufferedReader. */
   public static
   BufferedReader reader(String path) throws FileNotFoundException
   {
      return new BufferedReader(new FileReader(path));
   }

   /* For reading from the internal s_storage. */
   public static
   BufferedReader reader(String path, String fileEncoding)
         throws FileNotFoundException, UnsupportedEncodingException
   {
      Context context = Util.getContext();
      String path1 = Util.getInternalPath(path);
      FileInputStream fis = context.openFileInput(path1);
      return new BufferedReader(new InputStreamReader(fis, fileEncoding));
   }

   static
   String[][] csv()
   {
      return csv(Constants.INDEX, 'f', 'u', 't');
   }

   static
   String[][] csv(String path, char... type)
   {
      if(Util.isUnmounted())
      {
         return Util.EMPTY_STRING_STRING_ARRAY;
      }

      String[] lines = file(path);
      if(0 == lines.length)
      {
         return Util.EMPTY_STRING_STRING_ARRAY;
      }

      String[][] types = new String[type.length][lines.length];

      int linesLength = lines.length;
      for(int j = 0; j < linesLength; j++)
      {
         int offset = 0;
         String line = lines[j];
         int next;
         while(-1 != (next = line.indexOf('|', offset)))
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
                  next = line.indexOf('|', offset);
                  types[i][j] = line.substring(offset, next);
                  break;
               }
            }
            offset = line.indexOf('|', offset) + 1;
         }
      }
      return types;
   }

   static
   Set set(String filePath)
   {
      Set set = new HashSet<String>();

      if(Util.isUnmounted())
      {
         return set;
      }

      Collections.addAll(set, file(filePath));
      return set;
   }
}
