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

package com.mrbinwin.hungryapp.fragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.mrbinwin.hungryapp.OnActivityResultReceiver
import com.mrbinwin.hungryapp.OnRequestPermissionsResultReceiver
import com.mrbinwin.hungryapp.mvp.contracts.BaseView
import com.mrbinwin.hungryapp.utils.ActivityResult
import com.mrbinwin.hungryapp.utils.RequestPermissionsResult
import io.reactivex.Observable

open class BaseFragment: Fragment(), BaseView {

    override var onActivityResultEvent: Observable<ActivityResult>? = null
    override var onRequestPermissionsResultEvent: Observable<RequestPermissionsResult>? = null

    /**
     * Does a fragment have the up button
     *
     */
    protected open val isSupportUpButtonShown: Boolean = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let { _activity ->
            if (_activity is AppCompatActivity) {
                _activity.supportActionBar?.setDisplayHomeAsUpEnabled(isSupportUpButtonShown)
            }
            if (_activity is OnActivityResultReceiver) {
                onActivityResultEvent = _activity.onActivityResultEvent
            }
            if (_activity is OnRequestPermissionsResultReceiver) {
                onRequestPermissionsResultEvent = _activity.onRequestPermissionsResultEvent
            }
        }

    }
}