package yay.poloure.simplerss;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class Read
{
   private Read()
   {
   }
   /* All functions in here must check that the media is available before
    * continuing. */

   static String setting(String path)
   {
      if(Util.isUnmounted())
      {
         return null;
      }

      String[] check = file(path);
      return 0 == check.length ? "" : check[0];
   }

   static String[][] csv()
   {
      return csv_private(FeedsActivity.INDEX, 'f', 'u', 't');
   }

   /* This is for reading an rss file. */
   static String[][] csv(String feed, char... type)
   {
      return csv_private(Util.getPath(feed, FeedsActivity.CONTENT), type);
   }

   private static String[][] csv_private(String path, char... type)
   {
      if(Util.isUnmounted())
      {
         return new String[0][0];
      }

      String[] lines = file(path);
      if(0 == lines.length)
      {
         return new String[0][0];
      }

      String[][] types = new String[type.length][lines.length];

      for(int j = 0; j < lines.length; j++)
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
            for(int i = 0; i < type.length; i++)
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

   /* This function is now safe. It will return a zero length array on error. */
   static String[] file(String path)
   {
      if(Util.isUnmounted())
      {
         return new String[0];
      }

      String storage = Util.getStorage();
      if(!path.contains(storage))
      {
         path = Util.getStorage() + path;
      }

      int count;

      /* If the path is not a count file, get the number of lines. */
      count = !path.contains(FeedsActivity.COUNT) ? count(path) : 1;

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
            in = Util.useSd() ? reader(path) : reader(path);

            for(int i = 0; i < lines.length; i++)
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
      catch(Exception e)
      {
         return new String[0];
      }
      return lines;
   }

   static Set set(String file_path)
   {
      Set set = new HashSet<String>();

      if(Util.isUnmounted())
      {
         return set;
      }

      Collections.addAll(set, file(file_path));
      return set;
   }

   static int count(String path)
   {
      if(Util.isUnmounted())
      {
         return 0;
      }

      String storage = Util.getStorage();
      if(!path.contains(storage))
      {
         path = Util.getStorage() + path;
      }

      String[] count = file(path + FeedsActivity.COUNT);
      if(0 != count.length)
      {
         return Util.stoi(count[0]);
      }

      int i = 0;
      try
      {
         BufferedReader in = null;
         try
         {
            in = Util.useSd() ? reader(path) : reader(path);

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
      catch(Exception e)
      {
      }
      return i;
   }

   /* Wrapper for creating external BufferedReader. */
   public static BufferedReader reader(String path) throws IOException
   {
      return new BufferedReader(new FileReader(path));
   }

   /* For reading from the internal storage. */
   public static BufferedReader reader(String path, String UTF) throws Exception
   {
      Context context = Util.getContext();
      path = Util.getInternalName(path);
      FileInputStream fis = context.openFileInput(path);
      return new BufferedReader(new InputStreamReader(fis, "UTF-8"));
   }
}
