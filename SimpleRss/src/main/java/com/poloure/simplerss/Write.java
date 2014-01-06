package com.poloure.simplerss;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

class Write
{
   static final String LOG_FILE = "dump.txt";
   static final int MODE_REMOVE = 1;
   static final int MODE_REPLACE = 2;
   static final String NEW_LINE = System.getProperty("line.separator");

   static
   void editIndexLineContaining(CharSequence stringSearch, String applicationFolder, int mode,
         String replacementLine)
   {
      if(Read.isUnmounted())
      {
         return;
      }

      /* Read the file to an array, if the file does not exist, return. */
      String[] lines = Read.file(Read.INDEX, applicationFolder);
      if(0 == lines.length)
      {
         return;
      }

      BufferedWriter out = open(applicationFolder + Read.INDEX, false);

      try
      {
         for(String line : lines)
         {
            if(!line.contains(stringSearch))
            {
               out.write(line + NEW_LINE);
            }
            else if(MODE_REPLACE == mode)
            {
               out.write(replacementLine);
            }
         }
      }
      catch(IOException ignored)
      {
      }
      finally
      {
         Read.close(out);
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

      DataOutputStream data = null;
      BufferedOutputStream out = null;

      try
      {
         FileOutputStream fileOutputStream = new FileOutputStream(fileOut);
         out = new BufferedOutputStream(fileOutputStream);
         data = new DataOutputStream(out);

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
         Read.close(data);
         Read.close(out);
      }
   }

   static
   BufferedWriter open(String file, boolean appendToEnd)
   {
      try
      {
         return new BufferedWriter(new FileWriter(file, appendToEnd));
      }
      catch(IOException ignored)
      {
         return null;
      }
   }
}
