package com.poloure.simplerss;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.Set;

class Read
{
   static final         String INDEX          = "index.txt";
   private static final char   ITEM_SEPARATOR = '|';

   /* All functions in here must check that the media is available before
    * continuing. */

   static
   String[][] indexFile(String applicationFolder)
   {
      return csvFile(INDEX, applicationFolder, 'f', 'u', 't');
   }

   static
   String[][] csvFile(String fileName, String applicationFolder, char... type)
   {
      if(isUnmounted())
      {
         return new String[type.length][0];
      }

      String[] lines = file(fileName, applicationFolder);
      if(0 == lines.length)
      {
         return new String[type.length][0];
      }

      String[][] types = new String[type.length][lines.length];

      int linesLength = lines.length;
      for(int j = 0; j < linesLength; j++)
      {
         int offset = 0;
         String line = lines[j];
         int next;

         while(-1 != (next = line.indexOf(ITEM_SEPARATOR, offset)))
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
                  next = line.indexOf(ITEM_SEPARATOR, offset);
                  types[i][j] = line.substring(offset, next);
                  break;
               }
            }
            offset = line.indexOf(ITEM_SEPARATOR, offset) + 1;
         }
      }
      return types;
   }

   static
   boolean isUnmounted()
   {
      /* Check to see if we can Write to the media. */
      String mounted = Environment.MEDIA_MOUNTED;
      String externalStorageState = Environment.getExternalStorageState();
      return !mounted.equals(externalStorageState);
   }

   /* This function is now safe. It will return a zero length array on error. */
   static
   String[] file(String fileName, String applicationFolder)
   {
      if(isUnmounted())
      {
         return new String[0];
      }

      String filePath = applicationFolder + fileName;

      /* Get the number of lines. */
      int count = count(fileName, applicationFolder);

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
            in = reader(filePath);

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
      catch(FileNotFoundException ignored)
      {
         //e.printStackTrace();
         return new String[0];
      }
      catch(UnsupportedEncodingException e)
      {
         e.printStackTrace();
         return new String[0];
      }
      catch(IOException e)
      {
         e.printStackTrace();
         return new String[0];
      }

      return lines;
   }

   static
   int count(String fileName, String applicationFolder)
   {
      if(isUnmounted())
      {
         return 0;
      }

      String filePath = applicationFolder + fileName;

      int i = 0;
      try
      {
         BufferedReader in = null;
         try
         {
            in = reader(filePath);

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
      catch(FileNotFoundException ignored)
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
   private static
   BufferedReader reader(String path) throws FileNotFoundException
   {
      return new BufferedReader(new FileReader(path));
   }

   static
   Set<Long> longSet(String fileName, String fileFolder)
   {
      Set<Long> longSet = new LinkedHashSet<Long>(0);

      /* If storage is unmounted OR if we force to use external. */
      if(isUnmounted())
      {
         return longSet;
      }

      String filePath = fileFolder + fileName;
      File fileIn = new File(filePath);

      try
      {
         FileInputStream fileInputStream = new FileInputStream(fileIn);
         BufferedInputStream in = new BufferedInputStream(fileInputStream);
         DataInputStream data = new DataInputStream(in);
         try
         {
            while(true)
            {
               long lon = data.readLong();
               longSet.add(lon);
            }
         }
         catch(EOFException ignored)
         {
         }
         finally
         {
            data.close();
         }

      }
      catch(FileNotFoundException ignored)
      {
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
      return longSet;
   }
}
