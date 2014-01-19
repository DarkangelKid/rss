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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

class Write
{
   static final int MODE_REMOVE = 1;
   static final int MODE_REPLACE = 2;
   static final String NEW_LINE = System.getProperty("line.separator");

   static
   void editIndexLine(Context context, CharSequence stringSearch, int mode, String replacementLine)
   {
      /* Read the file to an array, if the file does not exist, return. */
      String[] lines = Read.file(context, Read.INDEX);
      if(0 != lines.length)
      {
         String filePath = Read.INDEX;
         try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(context.openFileOutput(filePath, Context.MODE_PRIVATE))))
         {
            for(String line : lines)
            {
               if(!line.equals(stringSearch))
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
      }
   }

   static
   void longSet(Context context, String fileName, Iterable<Long> longSet)
   {
      try(DataOutputStream out = new DataOutputStream(new BufferedOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE))))
      {
         for(long l : longSet)
         {
            out.writeLong(l);
         }
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }
}
