package com.poloure.simplerss;

import android.os.Environment;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

class Read
{
   static final String INDEX = "index.txt";
   private static final Pattern SPLIT_NEWLINE = Pattern.compile("\n");
   private static final char ITEM_SEPARATOR = '|';
   private static final int FILE_BUFFER_SIZE = 8096;

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

            offset = nextSeparator + 1;
            nextSeparator = line.indexOf(ITEM_SEPARATOR, offset);

            if(-1 != indexOfCh)
            {
               types[indexOfCh][j] = line.substring(offset, nextSeparator);
            }
         }
         while(-1 != nextSeparator);

      }
      return types;
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
            close(f);
         }
      }
      catch(IOException e)
      {
         e.printStackTrace();
         return new String[0];
      }

      return lines;
   }

   static
   boolean isUnmounted()
   {
      /* Check to see if we can Write to the media. */
      String mounted = Environment.MEDIA_MOUNTED;
      String externalStorageState = Environment.getExternalStorageState();
      return !mounted.equals(externalStorageState);
   }

   static
   BufferedReader open(String file)
   {
      try
      {
         return new BufferedReader(new FileReader(file));
      }
      catch(FileNotFoundException ignored)
      {
         return null;
      }
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
      catch(IOException e)
      {
         e.printStackTrace();
      }
      return longSet;
   }

   static
   void close(Closeable c)
   {
      if(null == c)
      {
         return;
      }
      try
      {
         c.close();
      }
      catch(IOException ignored)
      {
      }
   }
}
