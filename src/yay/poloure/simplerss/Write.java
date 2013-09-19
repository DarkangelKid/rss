package yay.poloure.simplerss;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Arrays;

final class Write
{
   static final String MEDIA_UNMOUNTED = "Media not mounted.";

   private Write()
   {
   }

   /* All functions in here must check that the media is available before
    * continuing. */

   public static boolean collection(String path, Iterable<?> content)
   {
      /* If storage is unmounted OR if we force to use external. */
      if(Util.isUnmounted())
      {
         return false;
      }

      path = Util.getStorage() + path;

      if(Util.useSd())
      {
         Util.remove(path);
      }

      try
      {
         BufferedWriter out = null;
         try
         {
            /* Create the buffered writer. */
            out = Util.useSd() ? writer(path, false) : writer(path, Context.MODE_PRIVATE);

            for(Object item : content)
            {
               out.write(item + FeedsActivity.NL);
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
      catch(Exception e)
      {
      }
      return true;
   }

   /* Function should be safe, returns false if fails. */
   public static boolean download(String urler, String path)
   {
      /* If storage is unmounted OR if we force to use external. */
      if(Util.isUnmounted())
      {
         return false;
      }

      Context context = Util.getContext();
      path = Util.getStorage() + path;
      String name = Util.getInternalName(path);

      try
      {
         BufferedInputStream in = null;
         FileOutputStream fout = null;
         try
         {
            in = new BufferedInputStream(new URL(urler).openStream());
            if(!Util.useSd() &&
               !urler.contains(".jpg") &&
               !urler.contains(".png") &&
               !urler.contains(".gif") &&
               !urler.contains(".JPEG") &&
               !urler.contains(".JPG") &&
               !urler.contains(".jpeg"))
            {
               fout = context.openFileOutput(name, Context.MODE_PRIVATE);
            }
            else
            {
               fout = new FileOutputStream(path);
            }

            byte[] data = new byte[1024];
            int count;
            while(-1 != (count = in.read(data, 0, 1024)))
            {
               fout.write(data, 0, count);
            }
         }
         finally
         {
            if(null != in)
            {
               in.close();
            }
            if(null != fout)
            {
               fout.close();
            }
         }
      }
      catch(Exception e)
      {
         Util.remove(path);
         return false;
      }
      /* TODO: if file exists. */
      return true;
   }

   /* Function should be safe, returns false if fails. */
   public static boolean single(String path, String string)
   {
      /* If storage is unmounted OR if we force to use external. */
      if(Util.isUnmounted())
      {
         return false;
      }

      path = Util.getStorage() + path;

      try
      {
         BufferedWriter out = null;
         try
         {
            out = Util.useSd() ? writer(path, true) : writer(path, Context.MODE_APPEND);
            out.write(string);
         }
         finally
         {
            if(null != out)
            {
               out.close();
            }
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
   static boolean removeLine(String path, CharSequence string, boolean contains)
   {
      /* If storage is unmounted OR if we force to use external. */
      if(Util.isUnmounted())
      {
         return false;
      }

      path = Util.getStorage() + path;
      String tempPath = path + FeedsActivity.TEMP;

      String[] lines;
      try
      {
         BufferedWriter out = null;
         try
         {
            /* Read the file to an array, if the file does not exist, return. */
            lines = Read.file(path);
            if(0 == lines.length)
            {
               return false;
            }

            /* No backup for internal storage. */
            out = Util.useSd() ? writer(tempPath, false) : writer(path, Context.MODE_PRIVATE);

            for(String item : lines)
            {
               if(contains && !item.contains(string) || !contains && !item.equals(string))
               {
                  out.write(item + FeedsActivity.NL);
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
      catch(Exception e)
      {
         if(Util.useSd())
         {
            Util.remove(tempPath);
         }
         return false;
      }

      /* If the rename failed, delete the file and Write the original. */
      if(Util.useSd())
      {
         boolean success = Util.move(tempPath, path);
         if(!success)
         {
            Util.remove(path);
            collection(path, Arrays.asList(lines));
         }
         return success;
      }
      return true;
   }

   public static void log(String text)
   {
      single(FeedsActivity.DUMP_FILE, text + FeedsActivity.NL);
   }

   private static BufferedWriter writer(String p, boolean ap) throws IOException
   {
      return new BufferedWriter(new FileWriter(p, ap));
   }

   private static BufferedWriter writer(String path, int MODE) throws Exception
   {
      Context context = Util.getContext();
      path = Util.getInternalName(path);
      FileOutputStream f = context.openFileOutput(path, MODE);
      return new BufferedWriter(new OutputStreamWriter(f, "UTF8"));
   }
}
