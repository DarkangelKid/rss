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

package com.poloure.simplerss.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.poloure.simplerss.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public
class AsyncTaskSaveImage extends AsyncTask<Void, Void, Boolean>
{
    private final Context mContext;
    private final String mImageName;
    private final String mImageUrl;

    public
    AsyncTaskSaveImage(Context context, String imageName, String imageUrl)
    {
        mContext = context;
        mImageName = imageName;
        mImageUrl = imageUrl;
    }

    @Override
    public
    Boolean doInBackground(Void... stuff)
    {
        try
        {
            String appName = mContext.getString(R.string.application_name);
            File pictureFolder = getPicturesFolder(appName);
            File file = new File(pictureFolder, mImageName);

            InputStream inputStream = new URL(mImageUrl).openStream();
            BufferedInputStream in = new BufferedInputStream(inputStream);

            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream out = new BufferedOutputStream(fos);
            try
            {
                byte[] buf = new byte[1024];
                int offset;
                while(0 < (offset = in.read(buf)))
                {
                    out.write(buf, 0, offset);
                }
            }
            finally
            {
                in.close();
                out.close();
            }
        }
        catch(MalformedURLException e)
        {
            e.printStackTrace();
            return false;
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Get a File representing the folder to save images to and make folder structure.
     *
     * @param subFolder the folder to save images in inside ./Pictures/.
     *
     * @return File representing the folder to save images to.
     *
     * @throws java.io.FileNotFoundException when the file structure could not be made.
     */
    private static
    File getPicturesFolder(String subFolder) throws FileNotFoundException
    {
        // Get the directory for the user's public pictures directory.
        File picsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(picsFolder, subFolder);

        // If the directory structure does not exist, make it.
        if(!file.exists() && !file.mkdirs())
        {
            throw new FileNotFoundException();
        }
        return file;
    }

    @Override
    public
    void onPostExecute(Boolean result)
    {
        if(result)
        {
            String appName = mContext.getString(R.string.application_name);
            String success = mContext.getString(R.string.image_downloaded_success, appName);
            Toast toast = Toast.makeText(mContext, success, Toast.LENGTH_SHORT);
            toast.show();
        }
        else
        {
            String failed = mContext.getString(R.string.image_downloaded_failed);
            Toast toast = Toast.makeText(mContext, failed, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
