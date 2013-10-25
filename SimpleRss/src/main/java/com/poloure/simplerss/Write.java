package com.poloure.simplerss;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

class Write
{
   static final         String LOG_FILE = "dump" + ".txt";
   private static final String TEMP     = ".temp" + ".txt";
   /* All functions in here must check that the media is available before
    * continuing. */

   /* This function should be safe, returns false if it failed.
    * NOT SAFE FOR INTERNAL IF FAILS. */
   static
   int removeLine(String fileName, CharSequence stringSearch, boolean contains, String fileFolder)
   {
      /* If s_storage is unmounted OR if we force to use external. */
      if(Read.isUnmounted())
      {
         return -1;
      }

      String filePath = fileFolder + fileName;
      String tempPath = filePath + TEMP;

      String[] lines = new String[0];
      int pos = -1;
      try
      {
         BufferedWriter out = null;
         try
         {
            /* Read the file to an array, if the file does not exist, return. */
            lines = Read.file(fileName, fileFolder);
            if(0 == lines.length)
            {
               return -1;
            }

            /* No backup for internal s_storage. */
            out = writer(tempPath, false);

            int line = 0;
            for(String item : lines)
            {
               if(contains && !item.contains(stringSearch) ||
                     !contains && !item.equals(stringSearch))
               {
                  out.write(item + System.getProperty("line.separator"));
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

         File file = new File(tempPath);
         file.delete();

         return -1;
      }

      /* If the rename failed, delete the file and Write the original. */
      boolean success = !moveFile(fileName + TEMP, fileName, fileFolder);
      if(success)
      {
         File file = new File(fileName);
         file.delete();
         List<String> list = Arrays.asList(lines);
         collection(fileName, list, fileFolder);

      }
      return pos;
   }

   private static
   BufferedWriter writer(String filePath, boolean appendToEnd) throws IOException
   {
      return new BufferedWriter(new FileWriter(filePath, appendToEnd));
   }

   static
   void collection(String fileName, Iterable<?> content, String fileFolder)
   {
      /* If s_storage is unmounted OR if we force to use external. */
      if(Read.isUnmounted())
      {
         return;
      }

      String filePath = fileFolder + fileName;

      /* Delete file before writing new one. */
      File file = new File(filePath);
      file.delete();

      try
      {
         BufferedWriter out = null;
         try
         {
            /* Create the buffered writer. */
            out = writer(filePath, false);

            for(Object item : content)
            {
               out.write(item + System.getProperty("line.separator"));
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
   void toLogFile(String text, String fileFolder)
   {
      single(LOG_FILE, text + System.getProperty("line.separator"), fileFolder);
   }

   /* Function should be safe, returns false if fails. */
   static
   void single(String fileName, String stringToWrite, String fileFolder)
   {
      /* If s_storage is unmounted OR if we force to use external. */
      if(Read.isUnmounted())
      {
         return;
      }

      String filePath = fileFolder + fileName;

      try
      {
         BufferedWriter out = null;
         try
         {
            out = writer(filePath, true);
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
   void longSet(String fileName, Iterable<Long> longSet, String fileFolder)
   {
      /* If storage is unmounted OR if we force to use external. */
      if(Read.isUnmounted())
      {
         return;
      }

      String filePath = fileFolder + fileName;
      File fileOut = new File(filePath);

      try
      {
         FileOutputStream fileOutputStream = new FileOutputStream(fileOut);
         BufferedOutputStream out = new BufferedOutputStream(fileOutputStream);
         DataOutputStream data = new DataOutputStream(out);
         try
         {
            for(long lon : longSet)
            {
               data.writeLong(lon);
            }
         }
         catch(IOException e)
         {
            e.printStackTrace();
         }
         finally
         {
            data.flush();
            data.close();
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
   }

   static
   boolean moveFile(String originalName, String resultingName, String storage)
   {
      File originalFile = new File(storage + originalName);
      File resultingFile = new File(storage + resultingName);

      return originalFile.renameTo(resultingFile);
   }
}
