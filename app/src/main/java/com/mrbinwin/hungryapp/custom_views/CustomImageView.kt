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

package com.mrbinwin.hungryapp.custom_views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * Custom class created to suppress the following Android Studio warning:
 *      "If a View that overrides onTouchEvent or uses an OnTouchListener does not also implement
 *      performClick and call it when clicks are detected, the View may not handle accessibility
 *      actions properly. Logic handling the click actions should ideally be placed in
 *      View#performClick as some accessibility services invoke performClick when a click action
 *      should occur."
 * https://stackoverflow.com/questions/47107105/android-button-has-setontouchlistener-called-on-it-but-does-not-override-perform
 *
 */
class CustomImageView : AppCompatImageView {

    constructor(context: Context?): super(context)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs)

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}