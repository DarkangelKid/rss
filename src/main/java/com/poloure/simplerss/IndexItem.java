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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

class IndexItem implements Serializable
{
   private static final long serialVersionUID = 200L;

   long m_uid;
   String m_url;
   String[] m_tags;

   IndexItem(long id, String url, String... tags)
   {
      m_uid = id;
      m_url = url;
      m_tags = tags;
   }

   private
   void writeObject(ObjectOutputStream out) throws IOException
   {
      out.writeLong(m_uid);
      out.writeUTF(m_url);
      out.writeObject(m_tags);
   }

   private
   void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      m_uid = in.readLong();
      m_url = in.readUTF();
      m_tags = (String[]) in.readObject();
   }

}
