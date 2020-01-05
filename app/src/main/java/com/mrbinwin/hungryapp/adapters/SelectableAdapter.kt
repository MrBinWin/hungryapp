/*
 * Copyright (C) 2019 MrBinWin (https://github.com/MrBinWin/),
 *                         Dmitry Kuznetsov <mrbinwin@gmail.com>
 *
 * This file is part of HungryApp.
 *
 * HungryApp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HungryApp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HungryApp.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.mrbinwin.hungryapp.adapters

import android.util.SparseBooleanArray
import androidx.core.util.contains
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * The items of this adapter can be selected
 *
 */
abstract class SelectableAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    /**
     * Maps Integers to Booleans and represents the selected items
     * {1: true, 3: true, 7: true, 8: true}
     *
     */
    private val selectedItems: SparseBooleanArray = SparseBooleanArray()

    open fun clearSelection() {
        val selection: ArrayList<Int> = getSelectedItems()
        selectedItems.clear()
        for (i in selection) {
            notifyItemChanged(i)
        }
    }

    open fun getSelectedItems(): ArrayList<Int> {
        val result: ArrayList<Int> = ArrayList(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            result.add(selectedItems.keyAt(i))
        }
        return result
    }

    open fun getSelectedItemsCount(): Int {
        return selectedItems.size()
    }

    open fun isItemSelected(position: Int): Boolean {
        return selectedItems.contains(position)
    }

    open fun setSelectedItems(items: ArrayList<Int>) {
        for (i in items) {
            selectedItems.put(i, true)
        }
    }

    open fun toggleItemSelection(position: Int) {
        if (selectedItems.get(position,false)) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }
        notifyItemChanged(position)
    }

}