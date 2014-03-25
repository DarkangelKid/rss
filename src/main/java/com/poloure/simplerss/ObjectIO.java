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
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

class ObjectIO
{
    private static final Logger LOGGER = Logger.getLogger(RssLogger.class.getName());
    private final Context m_context;
    private String m_fileName;
    private Object m_object;

    ObjectIO(Context context, String fileName)
    {
        m_context = context;
        m_fileName = fileName;
    }

    void setNewFileName(String fileName)
    {
        m_fileName = fileName;
        m_object = null;
    }

    void write(Object object)
    {
        try
        {
            FileOutputStream fos = m_context.openFileOutput(m_fileName, Context.MODE_PRIVATE);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutput out = new ObjectOutputStream(bos);
            try
            {
                out.writeObject(object);
            }
            finally
            {
                out.close();
            }
        }
        catch(FileNotFoundException e)
        {
            String message = e.getMessage();
            LOGGER.log(Level.WARNING, "Failed to open file for output: " + message);
        }
        catch(IOException e)
        {
            String message = e.getMessage();
            LOGGER.log(Level.SEVERE, "Failed to write object to file: " + message);
        }
    }

    int getElementCount()
    {
        int count = 0;
        try
        {
            if(null != m_object)
            {
                count = ((Collection) m_object).size();
            }
            else
            {
                Collection object = (Collection) read();
                if(null != object)
                {
                    count = object.size();
                }
            }
        }
        catch(ClassCastException ignored)
        {
            LOGGER.log(Level.SEVERE, "Tried to count object that does not interface Collection<E>: " + m_fileName);
        }
        return count;
    }

    public
    Object read()
    {
        try
        {
            ObjectInput in = new ObjectInputStream(new BufferedInputStream(m_context.openFileInput(m_fileName)));
            try
            {
                m_object = in.readObject();
                return m_object;
            }
            finally
            {
                in.close();
            }
        }
        catch(ClassNotFoundException e)
        {
            LOGGER.log(Level.SEVERE, RssLogger.READ_CLASS_NOT_FOUND + e.getMessage());
        }
        catch(StreamCorruptedException e)
        {
            LOGGER.log(Level.SEVERE, "Stream was corrupted during reading of object: " + e.getMessage());
        }
        catch(FileNotFoundException e)
        {
            LOGGER.log(Level.SEVERE, "Tried to read an object from a file that was not found: " + e.getMessage());
        }
        catch(IOException e)
        {
            LOGGER.log(Level.SEVERE, "IOException while trying to read an object from file: " + e.getMessage());
        }
        return null;
    }
}
