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
   static final String LOG_FILE = "dump.txt";
   static final int MODE_REMOVE = 1;
   static final int MODE_REPLACE = 2;
   private static final String TEMP = ".temp.txt";
   private static final String NEW_LINE = System.getProperty("line.separator");
   /* All functions in here must check that the media is available before
    * continuing. */

   /* This function should be safe, returns false if it failed. */
   static
   void editLine(String fileName, CharSequence stringSearch, boolean containing,
         String applicationFolder, int mode, String replacementLine)
   {
      /* If s_storage is unmounted OR if we force to use external. */
      if(Read.isUnmounted())
      {
         return;
      }

      String filePath = applicationFolder + fileName;
      String tempPath = filePath + TEMP;

      String[] lines = new String[0];

      try
      {
         BufferedWriter out = null;
         try
         {
            /* Read the file to an array, if the file does not exist, return. */
            lines = Read.file(fileName, applicationFolder);
            if(0 == lines.length)
            {
               return;
            }

            /* No backup for internal s_storage. */
            out = writer(tempPath, false);

            for(String line : lines)
            {
               boolean notEqualOrContaining = containing ? !line.contains(stringSearch)
                     : !line.equals(stringSearch);

               if(notEqualOrContaining)
               {
                  out.write(line + NEW_LINE);
               }
               else if(MODE_REPLACE == mode)
               {
                  out.write(replacementLine);
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
      catch(IOException e)
      {
         e.printStackTrace();

         File file = new File(tempPath);
         file.delete();

         return;
      }

      /* If the rename failed, delete the file and Write the original. */
      boolean success = !moveFile(fileName + TEMP, fileName, applicationFolder);
      if(success)
      {
         File file = new File(fileName);
         file.delete();
         List<String> list = Arrays.asList(lines);
         collection(fileName, list, applicationFolder);

      }
   }

   static
   boolean moveFile(String originalName, String resultingName, String storage)
   {
      File originalFile = new File(storage + originalName);
      File resultingFile = new File(storage + resultingName);

      return originalFile.renameTo(resultingFile);
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
               out.write(item + NEW_LINE);
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

   private static
   BufferedWriter writer(String filePath, boolean appendToEnd) throws IOException
   {
      return new BufferedWriter(new FileWriter(filePath, appendToEnd));
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
}
