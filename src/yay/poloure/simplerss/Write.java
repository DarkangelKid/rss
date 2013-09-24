package yay.poloure.simplerss;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;

final
class Write
{
   static final String MEDIA_UNMOUNTED = "Media not mounted.";

   private
   Write()
   {
   }

   /* All functions in here must check that the media is available before
    * continuing. */

   static
   boolean collection(String path, Iterable<?> content)
   {
      /* If s_storage is unmounted OR if we force to use external. */
      if(Util.isUnmounted())
      {
         return false;
      }

      path = Util.getStorage() + path;

      if(Util.isUsingSd())
      {
         Util.remove(path);
      }

      try
      {
         BufferedWriter out = null;
         try
         {
            /* Create the buffered writer. */
            out = Util.isUsingSd() ? writer(path, false) : writer(path, Context.MODE_PRIVATE);

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
      catch(IOException e)
      {
         e.printStackTrace();
      }
      return true;
   }

   /* Function should be safe, returns false if fails. */
   static
   boolean download(String urler, String path)
   {
      /* If s_storage is unmounted OR if we force to use external. */
      if(Util.isUnmounted())
      {
         return false;
      }

      Context context = Util.getContext();
      path = Util.getStorage() + path;
      String name = Util.getInternalPath(path);

      try
      {
         BufferedInputStream in = null;
         FileOutputStream fout = null;
         try
         {
            in = new BufferedInputStream(new URL(urler).openStream());
            fout = !Util.isUsingSd() &&
                  !urler.contains(".jpg") &&
                  !urler.contains(".png") &&
                  !urler.contains(".gif") &&
                  !urler.contains(".JPEG") &&
                  !urler.contains(".JPG") &&
                  !urler.contains(".jpeg") ? context.openFileOutput(name, Context.MODE_PRIVATE)
                  : new FileOutputStream(path);

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
      catch(FileNotFoundException e)
      {
         e.printStackTrace();
         return false;
      }
      catch(IOException e)
      {
         e.printStackTrace();
         Util.remove(path);
         return false;
      }

      /* TODO: if file exists. */
      return true;
   }

   /* Function should be safe, returns false if fails. */
   static
   boolean single(String path, String stringToWrite)
   {
      /* If s_storage is unmounted OR if we force to use external. */
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
            out = Util.isUsingSd() ? writer(path, true) : writer(path, Context.MODE_APPEND);
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
         return false;
      }
      catch(IOException e)
      {
         e.printStackTrace();
         return false;
      }
      return true;
   }

   /* This function should be safe, returns false if it failed.
    * NOT SAFE FOR INTERNAL IF FAILS. */
   static
   boolean removeLine(String path, CharSequence stringSearch, boolean contains)
   {
      /* If s_storage is unmounted OR if we force to use external. */
      if(Util.isUnmounted())
      {
         return false;
      }

      path = Util.getStorage() + path;
      String tempPath = path + Constants.TEMP;

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

            /* No backup for internal s_storage. */
            out = Util.isUsingSd() ? writer(tempPath, false) : writer(path, Context.MODE_PRIVATE);

            for(String item : lines)
            {
               if(contains && !item.contains(stringSearch) ||
                     !contains && !item.equals(stringSearch))
               {
                  out.write(item + Constants.NL);
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
         return false;
      }
      catch(IOException e)
      {
         e.printStackTrace();
         if(Util.isUsingSd())
         {
            Util.remove(tempPath);
         }
         return false;
      }

      /* If the rename failed, delete the file and Write the original. */
      if(Util.isUsingSd())
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

   static
   void log(String text)
   {
      single(Constants.DUMP_FILE, text + Constants.NL);
   }

   static
   void log(int integer)
   {
      single(Constants.DUMP_FILE, integer + Constants.NL);
   }

   private static
   BufferedWriter writer(String p, boolean ap) throws IOException
   {
      return new BufferedWriter(new FileWriter(p, ap));
   }

   private static
   BufferedWriter writer(String path, int writeMode)
         throws FileNotFoundException, UnsupportedEncodingException
   {
      Context context = Util.getContext();
      path = Util.getInternalPath(path);
      FileOutputStream fileOutputStream = context.openFileOutput(path, writeMode);
      return new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF8"));
   }
}
