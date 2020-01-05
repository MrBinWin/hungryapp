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
import com.mrbinwin.hungryapp.data.AppDatabase.Companion.TABLE_NAME_PRODUCT
import com.mrbinwin.hungryapp.data.models.Product
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single


/**
 * Data Access Object implements methods to interact with Room database Product model
 *
 */
@Dao
interface ProductDao {

    /**
     * Delete one product
     *
     */
    @Delete
    fun delete(product: Product): Completable

    /**
     * Delete all products
     *
     */
    @Query("DELETE FROM $TABLE_NAME_PRODUCT")
    fun deleteAll(): Completable

    /**
     * Delete one product by id
     *
     */
    @Query("DELETE FROM $TABLE_NAME_PRODUCT WHERE id = :productId")
    fun deleteById(productId: Int): Completable

    /**
     * Get a product by id
     * @return a Maybe that triggers:
     * - observer.onSuccess if recipe exists in database
     * - observer.onComplete if recipe has not found
     *
     */
    @Query("SELECT * FROM $TABLE_NAME_PRODUCT WHERE id = :productId LIMIT 1")
    fun get(productId: Int): Maybe<Product>

    /**
     * Get all items
     *
     */
    @Query("SELECT * FROM $TABLE_NAME_PRODUCT ORDER BY rank DESC")
    fun getAll(): Single<List<Product>>

    /**
     * Get maximal product.rank from the table
     *
     */
    @Query("SELECT MAX(rank) FROM $TABLE_NAME_PRODUCT")
    fun getMaxRank(): Maybe<Int>

    /**
     * Insert new product
     * @return a Single that triggers
     * - observer.onSuccess accepts the new rowId for the inserted recipe
     *
     */
    @Insert
    fun insert(product: Product): Single<Long>

    @Insert
    fun insertMany(products: List<Product>): Single<List<Long>>

    @Update
    fun update(product: Product): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMany(products: List<Product?>?): Completable

}