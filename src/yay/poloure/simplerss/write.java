package yay.poloure.simplerss;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;

public class write
{
   static final String MEDIA_UNMOUNTED = "Media not mounted.";

   /* All functions in here must check that the media is available before
    * continuing. */

   public static boolean collection(String path, Iterable<?> content)
   {
      /* If storage is unmounted OR if we force to use external. */
      if(util.check_unmounted())
         return false;

      path = util.get_storage() + path;

      if(util.use_sd())
         util.rm(path);

      BufferedWriter out = null;
      try
      {
         try
         {
            /* Create the buffered writer. */
            out = util.use_sd() ? writer(path, false) :
                                  writer(path, Context.MODE_PRIVATE);

            for(Object item : content)
            {
               out.write(item.toString() + main.NL);
            }
         }
         finally
         {
            if(out != null)
               out.close();
         }
      }
      catch(Exception e)
      {
      }
      return true;
   }

   /* Function should be safe, returns false if fails. */
   public static boolean dl(String urler, String path)
   {
      /* If storage is unmounted OR if we force to use external. */
      if(util.check_unmounted())
         return false;

      Context context = util.get_context();
      path            = util.get_storage() + path;
      String name     = util.create_internal_name(path);

      try
      {
         BufferedInputStream in = null;
         FileOutputStream fout = null;
         try
         {
            in = new BufferedInputStream(new URL(urler).openStream());
            if(!util.use_sd()         &&
               !urler.contains(".jpg")&&
               !urler.contains(".png")&&
               !urler.contains(".gif")&&
               !urler.contains(".JPEG")&&
               !urler.contains(".JPG")&&
               !urler.contains(".jpeg"))
               fout = context.openFileOutput(name, Context.MODE_PRIVATE);
            else
               fout = new FileOutputStream(path);

            byte data[] = new byte[1024];
            int count;
            while((count = in.read(data, 0, 1024)) != -1)
               fout.write(data, 0, count);
         }
         finally
         {
            if (in != null)
               in.close();
            if (fout != null)
               fout.close();
         }
      }
      catch(Exception e)
      {
         util.rm(path);
         return false;
      }
      /* TODO: if file exists. */
      return true;
   }

   /* Function should be safe, returns false if fails. */
   public static boolean single(String path, String string)
   {
      /* If storage is unmounted OR if we force to use external. */
      if(util.check_unmounted())
         return false;

      path = util.get_storage() + path;

      BufferedWriter out = null;
      try
      {
         try
         {
            out = util.use_sd() ? writer(path, true) :
                                  writer(path, Context.MODE_APPEND);
            out.write(string);
         }
         finally
         {
            if(out != null)
               out.close();
         }
      }
      catch(Exception e)
      {
         return false;
      }
      return true;
   }

   /* This function should be safe, returns false if it failed.
    * NOT SAFE FOR INTERNAL IF FAILS. */
   static boolean remove_string(String path, String string, boolean contains)
   {
      /* If storage is unmounted OR if we force to use external. */
      if(util.check_unmounted())
         return false;

      String[] lines;
      path = util.get_storage() + path;
      String temp_path = path + main.TEMP;

      BufferedWriter out = null;
      try
      {
         try
         {
            /* Read the file to an array, if the file does not exist, return. */
            lines = read.file(path);
            if(lines.length == 0)
               return false;

            /* No backup for internal storage. */
            out = util.use_sd() ? writer(temp_path, false) :
                                  writer(path, Context.MODE_PRIVATE);

            for(String item : lines)
            {
               if(contains && !item.contains(string))
                  out.write(item + main.NL);
               else if(!contains && !item.equals(string))
                  out.write(item + main.NL);
            }
         }
         finally
         {
            if(out != null)
               out.close();
         }
      }
      /* If writing to the temp file fails, delete the temp file and return. */
      catch(Exception e)
      {
         if(util.use_sd())
            util.rm(temp_path);
         return false;
      }

      /* If the rename failed, delete the file and write the original. */
      if(util.use_sd())
      {
         boolean success = util.mv(temp_path, path);
         if(!success)
         {
            util.rm(path);
            collection(path, java.util.Arrays.asList(lines));
         }
         return success;
      }
      return true;
   }

   public static void log(String text)
   {
      single(main.DUMP_FILE, text + main.NL);
   }

   public static BufferedWriter writer(String p, boolean ap) throws IOException
   {
      return new BufferedWriter(new FileWriter(p, ap));
   }

   public static BufferedWriter writer(String path, int MODE) throws Exception
   {
      Context context    = util.get_context();
      path               = util.create_internal_name(path);
      FileOutputStream f = context.openFileOutput(path, MODE);
      return new BufferedWriter(new OutputStreamWriter(f, "UTF8"));
   }
}
