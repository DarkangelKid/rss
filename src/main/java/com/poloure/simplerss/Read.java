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
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

class Read
{
   static final String INDEX = "index.txt";
   static final String FAVOURITES = "favourites.txt";

   static
   Object object(Context context, String fileName)
   {
      try
      {
         ObjectInput in = new ObjectInputStream(new BufferedInputStream(context.openFileInput(fileName)));
         try
         {
            return in.readObject();
         }
         finally
         {
            if(null != in)
            {
               in.close();
            }
         }
      }
      catch(ClassNotFoundException e)
      {
         e.printStackTrace();
      }
      catch(StreamCorruptedException e)
      {
         e.printStackTrace();
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
      return null;
   }
}
