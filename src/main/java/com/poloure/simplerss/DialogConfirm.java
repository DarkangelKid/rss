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
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public
class DialogConfirm extends DialogPreference
{
    private final FeedsActivity m_activity;

    public
    DialogConfirm(Context context, AttributeSet attrs)
    {
        super(context, attrs, android.R.style.Theme_Holo_Light_Dialog);
        m_activity = (FeedsActivity) context;
    }

    @Override
    public
    void onClick(DialogInterface dialog, int which)
    {
        if(DialogInterface.BUTTON_POSITIVE == which)
        {
            // Reset the read item collection in memory.
            m_activity.getReadItemTimes().clear();
            m_activity.deleteFile(FeedsActivity.READ_ITEMS);

            // We should then refresh the navigation drawer for the unread counts.
            AsyncNavigationAdapter.run(m_activity);

            // TODO more efficient just to reload get views but invalidate is not working.
            AsyncNewTagAdapters.update(m_activity);
        }
    }
}
