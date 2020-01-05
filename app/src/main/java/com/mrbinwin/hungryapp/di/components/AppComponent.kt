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

package com.mrbinwin.hungryapp.di.components

import android.content.Context
import com.mrbinwin.hungryapp.data.AppDatabase
import com.mrbinwin.hungryapp.di.modules.AppModule
import com.mrbinwin.hungryapp.utils.CameraPhotoRepository
import dagger.Component
import javax.inject.Singleton

/**
 * Dagger2 component
 * Provides the application level injections
 *
 */
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun exposeApplicationContext(): Context
    fun exposeCameraPhotoRepository(): CameraPhotoRepository
    fun exposeDatabase(): AppDatabase
}