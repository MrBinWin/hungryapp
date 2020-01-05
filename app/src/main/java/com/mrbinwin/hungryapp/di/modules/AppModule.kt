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

package com.mrbinwin.hungryapp.di.modules

import android.content.Context
import androidx.room.Room
import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.data.AppDatabase
import com.mrbinwin.hungryapp.utils.CameraPhotoRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Dagger2 module
 * Provides the application level injections
 *
 */
@Module
class AppModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideContext(): Context {
        return context
    }

    @Provides
    fun provideDatabase(callback: AppDatabase.OnCreateCallback): AppDatabase {
        App.appDatabase?.let {
            return it
        }
        return createDatabaseInstance(callback)
    }

    @Provides
    @Singleton
    fun provideCameraPhotoRepository(): CameraPhotoRepository {
        return CameraPhotoRepository()
    }

    @Synchronized
    private fun createDatabaseInstance(callback: AppDatabase.OnCreateCallback): AppDatabase {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DB_NAME
        )
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()
        App.appDatabase = db

        // triggers pre population of the initial data by RoomDatabase.Callback::onCreate()
        val thread = Thread {
            db.runInTransaction {  }
        }
        thread.start()
        thread.join()

        return db
    }
}