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

package com.mrbinwin.hungryapp.data.dao

import androidx.room.*
import com.mrbinwin.hungryapp.data.AppDatabase.Companion.TABLE_NAME_RECIPE
import com.mrbinwin.hungryapp.data.models.Recipe
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * Data Access Object implements methods to interact with Room database Recipe model
 *
 */
@Dao
interface RecipeDao {

    /**
     * Delete one recipe
     *
     */
    @Delete
    fun delete(recipe: Recipe): Completable

    /**
     * Delete all recipes
     *
     */
    @Query("DELETE FROM $TABLE_NAME_RECIPE")
    fun deleteAll(): Completable


    /**
     * Delete one recipe by id
     *
     */
    @Query("DELETE FROM $TABLE_NAME_RECIPE WHERE id = :recipeId")
    fun deleteById(recipeId: Int): Completable

    /**
     * Delete multiple recipes by id
     *
     */
    @Query("DELETE FROM $TABLE_NAME_RECIPE WHERE id in (:recipeIds)")
    fun deleteMany(recipeIds: List<Int>): Completable

    /**
     * Get a recipe by id
     * @return a Maybe that triggers:
     * - observer.onSuccess if recipe exists in database
     * - observer.onComplete if recipe has not found
     *
     */
    @Query("SELECT * FROM $TABLE_NAME_RECIPE WHERE id = :recipeId LIMIT 1")
    fun get(recipeId: Int): Maybe<Recipe>

    /**
     * Get all recipes
     *
     */
    @Query("SELECT * FROM $TABLE_NAME_RECIPE")
    fun getAll(): Single<List<Recipe>>

    /**
     * Insert new recipe
     * @return a Single that triggers
     * - observer.onSuccess accepts the new rowId for the inserted recipe
     *
     */
    @Insert
    fun insert(recipe: Recipe): Single<Long>

    @Update
    fun update(recipe: Recipe): Completable

}