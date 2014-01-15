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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

class Read
{
   static final String INDEX = "index.txt";
   private static final Pattern SPLIT_NEWLINE = Pattern.compile("\n");
   private static final String ITEM_SEPARATOR = "\\|";
   private static final int FILE_BUFFER_SIZE = 8096;

   static
   String[][] csvFile(String fileName, String applicationFolder, char... type)
   {
      String[] lines = file(fileName, applicationFolder);
      int lineCount = lines.length;

      String[][] out = new String[type.length][lineCount];
      String typeString = new String(type);

      /* Fill the arrays with empty strings. */
      for(String[] array : out)
      {
         Arrays.fill(array, "");
      }

      for(int i = 0; i < lineCount; i++)
      {
         String[] lineContent = lines[i].split(ITEM_SEPARATOR);

         /* Replace any elements that we have in the line. */
         for(int j = 1; j < lineContent.length - 1; j += 2)
         {
            int index = typeString.indexOf(lineContent[j].charAt(0));
            if(-1 != index && !lineContent[j + 1].isEmpty())
            {
               out[index][i] = lineContent[j + 1];
            }
         }
      }
      return out;
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
