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

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class Read
{
   static final String INDEX = "index.txt";
   private static final String ITEM_SEPARATOR = "\\|";

   static
   String[][] csvFile(Context context, String fileName, char... type)
   {
      String[] lines = file(context, fileName);
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
   String[] file(Context context, String fileName)
   {
      List<String> list = new ArrayList<>(32);

      try(BufferedReader in = new BufferedReader(
            new InputStreamReader(context.openFileInput(fileName))))
      {
         String line;
         while(null != (line = in.readLine()))
         {
            list.add(line);
         }
      }
      catch(IOException ignored)
      {
         return new String[0];
      }
      return list.toArray(new String[list.size()]);
   }

   static
   boolean fileExists(Context context, String fileName)
   {
      try(FileInputStream in = context.openFileInput(fileName))
      {
      }
      catch(IOException ignored)
      {
         return false;
      }
      return true;
   }

   static
   Set<Long> longSet(Context context, String fileName)
   {
      Set<Long> longSet = new LinkedHashSet<>(64);

      try(DataInputStream in = new DataInputStream(
            new BufferedInputStream(context.openFileInput(fileName))))
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
