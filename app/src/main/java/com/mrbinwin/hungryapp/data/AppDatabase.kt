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

package com.mrbinwin.hungryapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mrbinwin.hungryapp.data.dao.RecipeDao
import com.mrbinwin.hungryapp.data.dao.ProductDao
import com.mrbinwin.hungryapp.data.models.Recipe
import com.mrbinwin.hungryapp.data.models.Product
import javax.inject.Inject

/**
 * The database stores recipes and products (shopping list items)
 *
 */
@Database(entities = [Recipe::class, Product::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DB_NAME = "hungryapp.db"
        const val TABLE_NAME_RECIPE = "recipe"
        const val TABLE_NAME_PRODUCT = "product"
    }

    abstract fun recipeDao(): RecipeDao
    abstract fun productDao(): ProductDao

    class OnCreateCallback @Inject constructor() : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Prepopulator.prepopulateData(db)
        }
    }
}