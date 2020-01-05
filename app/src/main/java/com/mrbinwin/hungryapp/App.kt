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

import android.app.Application
import android.content.Context
import com.mrbinwin.hungryapp.data.AppDatabase
import com.mrbinwin.hungryapp.di.components.AppComponent
import com.mrbinwin.hungryapp.di.components.DaggerAppComponent
import com.mrbinwin.hungryapp.di.modules.AppModule

class App: Application() {
    companion object {
        var appDatabase: AppDatabase? = null
        lateinit var context: Context
        private lateinit var appComponent: AppComponent
        val component: AppComponent
            get() = appComponent
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        initializeDagger()
    }

    private fun initializeDagger() {
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

}