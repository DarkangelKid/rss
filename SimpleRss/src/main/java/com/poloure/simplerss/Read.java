/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.poloure.simplerss;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
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

   /* This function will return a zero length array on error. */
   static
   String[] file(String fileName, String applicationFolder)
   {
      String[] lines;

      /* Begin reading the file to the String array. */
      String filePath = applicationFolder + fileName;

      try(FileInputStream f = new FileInputStream(filePath); FileChannel ch = f.getChannel())
      {
         ByteArrayBuffer builder = new ByteArrayBuffer(FILE_BUFFER_SIZE);
         MappedByteBuffer mb = ch.map(FileChannel.MapMode.READ_ONLY, 0L, ch.size());
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
      catch(IOException e)
      {
         e.printStackTrace();
         return new String[0];
      }
      return lines;
   }

   static
   Set<Long> longSet(String fileName, String fileFolder)
   {
      Set<Long> longSet = new LinkedHashSet<>(64);

      String filePath = fileFolder + fileName;
      try(DataInputStream in = new DataInputStream(
            new BufferedInputStream(new FileInputStream(filePath))))
      {
         while(true)
         {
            long lon = in.readLong();
            longSet.add(lon);
         }
      }
      catch(EOFException ignored)
      {
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
      return longSet;
   }
}
