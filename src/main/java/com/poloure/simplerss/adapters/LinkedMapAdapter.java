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

package com.poloure.simplerss.adapters;

import android.widget.BaseAdapter;

import org.apache.commons.collections.map.LinkedMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract
class LinkedMapAdapter<K, V> extends BaseAdapter
{
    private final LinkedMap m_map;

    LinkedMapAdapter(Map<K, V> map)
    {
        m_map = null == map ? new LinkedMap(1) : new LinkedMap(map);
        notifyDataSetChanged();
    }

    @Override
    public
    boolean isEmpty()
    {
        return m_map.isEmpty();
    }

    public
    void put(K key, V value)
    {
        m_map.put(key, value);
        notifyDataSetChanged();
    }

    public
    void putAll(Map<K, V> map)
    {
        m_map.putAll(map);
        notifyDataSetChanged();
    }

    public
    boolean containsValue(V value)
    {
        return m_map.containsValue(value);
    }

    public
    void remove(K key)
    {
        m_map.remove(key);
        notifyDataSetChanged();
    }

    public
    void clear()
    {
        m_map.clear();
        notifyDataSetChanged();
    }

    public
    int indexOf(K key)
    {
        return m_map.indexOf(key);
    }

    public
    K getKey(int index)
    {
        return (K) m_map.get(index);
    }

    /**
     * Not a cheap method to call.
     *
     * @return a list of the keys, in order.
     */
    public
    List<K> getKeyList()
    {
        return (List<K>) Arrays.asList(m_map.keySet().toArray());
    }

    public
    LinkedMap getMap()
    {
        return (LinkedMap) m_map.clone();
    }

    @Override
    public
    int getCount()
    {
        return m_map.size();
    }

    @Override
    public
    V getItem(int position)
    {
        return (V) m_map.getValue(position);
    }

    @Override
    public
    long getItemId(int position)
    {
        return position;
    }
}
