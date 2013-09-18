package yay.poloure.simplerss;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class read
{
   /* All functions in here must check that the media is available before
    * continuing. */

   static String setting(String path)
   {
      if(util.check_unmounted())
         return null;

      String[] check = file(path);
      return (check.length == 0) ? "" : check[0];
   }

   static String[][] csv(String index)
   {
      return csv_pr(index, 'f', 'u', 't');
   }

   /* This is for reading an rss file. */
   static String[][] csv(String feed, char... type)
   {
      return csv_pr(util.get_path(feed, main.CONTENT), type);
   }

   private static String[][] csv_pr(String path, char... type)
   {
      if(util.check_unmounted())
         return new String[0][0];

      int next, offset, i, j;
      String line;
      String[][] types;
      String[] lines;
      char ch;

      lines = file(path);
      if(lines.length == 0)
         return new String[0][0];

      types = new String[type.length][lines.length];

      for(j = 0; j < lines.length; j++)
      {
         offset = 0;
         line = lines[j];
         while((next = line.indexOf('|', offset)) != -1)
         {
            if(offset == line.length())
               break;

            ch = line.charAt(offset);
            offset = next + 1;
            for(i = 0; i < type.length; i++)
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
      if(util.check_unmounted())
         return new String[0];

      String storage = util.get_storage();
      if(!path.contains(storage))
         path = util.get_storage() + path;

      String count_path = path + main.COUNT;
      int count;
      String line;

      /* If the path is not a count file, get the number of lines. */
      if(!path.contains(main.COUNT))
      {
         count = count(path);
      }
      else
         count = 1;

      /* If the file is empty, return a zero length array. */
      if(count == 0)
         return new String[0];

      /* Use the count to allocate memory for the array. */
      String[] lines = new String[count];

      /* Begin reading the file to the String array. */
      BufferedReader in = null;
      try
      {
         try
         {
            in = (util.use_sd()) ? reader(path) : reader(path, "UTF-8");

            for(int i = 0; i < lines.length; i++)
               lines[i] = in.readLine();
         }
         finally
         {
            if(in != null)
               in.close();
         }
      }
      catch(Exception e)
      {
         return new String[0];
      }
      return lines;
   }

   static Set<String> set(String file_path)
   {
      Set set = new HashSet<String>();

      if(util.check_unmounted())
         return set;

      java.util.Collections.addAll(set, file(file_path));
      return set;
   }

   static int count(String path)
   {
      if(util.check_unmounted())
         return 0;

      String storage = util.get_storage();
      if(!path.contains(storage))
         path = util.get_storage() + path;

      String[] count = file(path + main.COUNT);
      if(count.length != 0)
         return util.stoi(count[0]);

      BufferedReader in = null;
      int i = 0;
      try
      {
         try
         {
            in = (util.use_sd()) ? reader(path) : reader(path, "UTF-8");

            while(in.readLine() != null)
               i++;
         }
         finally
         {
            if(in != null)
               in.close();
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
      Context context     = util.get_context();
      path                = util.create_internal_name(path);
      FileInputStream fis = context.openFileInput(path);
      return new BufferedReader(new InputStreamReader(fis, "UTF-8"));
   }
}
