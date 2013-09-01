package yay.poloure.simplerss;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import android.content.Context;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
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

      String[] check = read.file(path);
      return (check.length == 0) ? "" : check[0];
   }

   static String[][] csv(String file_path, char... type)
   {
      if(util.check_unmounted())
         return new String[0][0];

      int next, offset, i, j;
      String line;
      String[][] types;
      String[] lines;
      char ch;

      lines = file(file_path);
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
   static String[] file(String file_path)
   {
      String name     = util.create_internal_name(file_path);
      Context context = util.get_context();
      String storage  = util.get_storage();
      String internal = util.get_internal();

      if(util.check_unmounted())
         return new String[0];

      String count_path = file_path + main.COUNT;
      int count;
      String line;

      /* If the file_path is not a count file, get the number of lines. */
      if(!file_path.contains(main.COUNT))
      {
         String[] temp = file(count_path);
         count = (temp.length == 0) ? count(file_path) : util.stoi(temp[0]);
      }
      else
         count = count(count_path);

      /* If the file is empty, return a zero length array. */
      if(count == 0)
         return new String[0];

      /* Use the count to allocate memory for the array. */
      String[] lines = new String[count];

      /* Begin reading the file to the String array. */
      BufferedReader in = null;
      FileInputStream f = null;
      try
      {
         try
         {
            if(internal.equals(storage))
               in = new BufferedReader(new FileReader(file_path));
            else
            {
               f  = context.openFileInput(name);
               in = new BufferedReader(new InputStreamReader(f, "UTF-8"));
            }

            for(int i = 0; i < lines.length; i++)
               lines[i] = in.readLine();
            in.close();
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

   static int count(String file_path)
   {
      String name     = util.create_internal_name(file_path);
      Context context = util.get_context();
      String storage  = util.get_storage();
      String internal = util.get_internal();

      if(util.check_unmounted())
         return 0;

      BufferedReader in = null;
      FileInputStream f = null;
      int i = 0;
      try
      {
         try
         {
            if(internal.equals(storage))
               in = new BufferedReader(new FileReader(file_path));
            else
            {
               f  = context.openFileInput(name);
               in = new BufferedReader(new InputStreamReader(f, "UTF-8"));
            }

            while(in.readLine() != null)
               i++;
         }
         finally
         {
            if(in != null)
               in.close();
         }
      }
      catch(IOException e)
      {
      }
      return i;
   }
}
