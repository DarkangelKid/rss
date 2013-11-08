package com.poloure.simplerss;

import android.os.Environment;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

class Read
{
   static final         String  INDEX             = "index.txt";
   private static final Pattern SPLIT_NEWLINE     = Pattern.compile("\n");
   private static final char    ITEM_SEPARATOR    = '|';
   private static final int     COUNT_BUFFER_SIZE = 8096;
   private static final int     FILE_BUFFER_SIZE  = 8096;

   /* All functions in here must check that the media is available before
    * continuing. */

   static
   String[][] csvFile(String fileName, String applicationFolder, char... type)
   {
      if(isUnmounted())
      {
         return new String[type.length][0];
      }

      String[] lines = file(fileName, applicationFolder);
      int numberOfLines = lines.length;

      if(0 == numberOfLines)
      {
         return new String[type.length][0];
      }

      String[][] types = new String[type.length][numberOfLines];
      String typeString = new String(type);

      for(int j = 0; j < numberOfLines; j++)
      {
         String line = lines[j];

         int offset = 0;
         int nextSeparator;

         do
         {
            offset = line.indexOf(ITEM_SEPARATOR, offset) + 1;
            nextSeparator = line.indexOf(ITEM_SEPARATOR, offset);
            if(-1 == nextSeparator)
            {
               break;
            }

            char firstChar = line.charAt(offset);
            int indexOfCh = typeString.indexOf(firstChar);

            if(-1 != indexOfCh)
            {
               offset = nextSeparator + 1;
               nextSeparator = line.indexOf(ITEM_SEPARATOR, offset);
               types[indexOfCh][j] = line.substring(offset, nextSeparator);
            }
         }
         while(-1 != nextSeparator);

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

      String[] lines;

      /* Begin reading the file to the String array. */
      try
      {
         FileInputStream f = null;
         try
         {
            ByteArrayBuffer builder = new ByteArrayBuffer(FILE_BUFFER_SIZE);
            f = new FileInputStream(filePath);
            FileChannel channel = f.getChannel();
            MappedByteBuffer mb = channel.map(FileChannel.MapMode.READ_ONLY, 0L, channel.size());
            byte[] bufferArray = new byte[FILE_BUFFER_SIZE];

            while(mb.hasRemaining())
            {
               int nGet = Math.min(mb.remaining(), FILE_BUFFER_SIZE);
               mb.get(bufferArray, 0, nGet);
               builder.append(bufferArray, 0, nGet);
            }

            byte[] bytes = builder.toByteArray();
            CharSequence fullFile = new String(bytes);

            lines = SPLIT_NEWLINE.split(fullFile);
         }
         finally
         {
            if(null != f)
            {
               f.close();
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

   /* Wrapper for creating external BufferedReader. */
   private static
   BufferedReader reader(String path) throws FileNotFoundException
   {
      return new BufferedReader(new FileReader(path));
   }

   static
   int count(String fileName, String applicationFolder)
   {
      if(isUnmounted())
      {
         return 0;
      }

      String filePath = applicationFolder + fileName;
      int count = 0;

      try
      {
         InputStream is = new BufferedInputStream(new FileInputStream(filePath));
         try
         {
            byte[] c = new byte[COUNT_BUFFER_SIZE];
            int readChars = 0;
            boolean endsWithoutNewLine = false;
            while(-1 != (readChars = is.read(c)))
            {
               for(int i = 0; i < readChars; ++i)
               {
                  if('\n' == c[i])
                  {
                     ++count;
                  }
               }
               endsWithoutNewLine = '\n' != c[readChars - 1];
            }
            if(endsWithoutNewLine)
            {
               ++count;
            }
         }
         finally
         {
            is.close();
         }
      }
      catch(FileNotFoundException ignored)
      {
      }
      catch(IOException ignored)
      {
      }

      return count;
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
