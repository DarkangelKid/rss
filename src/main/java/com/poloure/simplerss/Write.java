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
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

class Write
{
   static
   void object(Context context, String fileName, Object object)
   {
      try
      {
         ObjectOutput out = new ObjectOutputStream(new BufferedOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE)));
         try
         {
            out.writeObject(object);
         }
         finally
         {
            if(null != out)
            {
               out.close();
            }
         }
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }
}
