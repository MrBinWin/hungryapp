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

package com.mrbinwin.hungryapp

import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mrbinwin.hungryapp.utils.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


class MainActivity : AppCompatActivity(), OnActivityResultReceiver, OnRequestPermissionsResultReceiver {

    companion object {
        const val REQUEST_CODE_PERMISSIONS_READ_EXTERNAL_STORAGE_ADD_RECIPE_PICTURE = 1
        const val REQUEST_CODE_PERMISSIONS_READ_EXTERNAL_STORAGE_ADD_BACKUP = 2

        const val REQUEST_CODE_CAPTURE_FROM_CAMERA = 101
        const val REQUEST_CODE_PICK_FROM_GALLERY = 102
        const val REQUEST_CODE_IMPORT_BACKUP = 103
    }

    @BindView(R.id.main_bottom_navigation) lateinit var bottomNavigation: BottomNavigationView

    override var onActivityResultEvent: Observable<ActivityResult>? = null
    override var onRequestPermissionsResultEvent: Observable<RequestPermissionsResult>? = null

    private var actionMode: ActionMode? = null
    private val onActivityResultSubject = PublishSubject.create<ActivityResult>()
    private val onRequestPermissionsResultSubject = PublishSubject.create<RequestPermissionsResult>()
    private val navController: NavController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }

        Completable.fromCallable { clearTmpImagesDir() }
            .subscribeOn(Schedulers.io())
            .subscribe()

        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        setSupportActionBar(findViewById(R.id.toolbar))
        onActivityResultEvent = onActivityResultSubject
        onRequestPermissionsResultEvent = onRequestPermissionsResultSubject

        bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, _, _ ->
            actionMode?.finish()
            AppKeyboard.hide(this)
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        onActivityResultSubject.onNext(ActivityResult(requestCode, resultCode, intent))
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        onRequestPermissionsResultSubject.onNext(RequestPermissionsResult(requestCode, permissions, grantResults))
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActionModeStarted(mode: ActionMode?) {
        super.onActionModeStarted(mode)
        actionMode = mode
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        super.onActionModeFinished(mode)
        actionMode = null
    }
}
