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

package com.mrbinwin.hungryapp.utils.extension

import android.content.Context
import com.mrbinwin.hungryapp.R

/**
 * If the String is a number
 * then it's perceived as the representation of minutes and converts to the number of
 * hours with the rest of minutes:
 * "134" --> "2h 14m"
 *
 */
fun String.formatToTime(context: Context): String {
    if (toIntOrNull() == null) {
        return this
    }

    val minutesStringName = context.getString(R.string.minutes)
    val hoursStringName = context.getString(R.string.hours)

    var minutes = toInt()
    if (minutes <= 60) {
        return "$minutes $minutesStringName"
    }

    val hours = minutes / 60
    minutes %= 60
    return "$hours $hoursStringName $minutes $minutesStringName"
}