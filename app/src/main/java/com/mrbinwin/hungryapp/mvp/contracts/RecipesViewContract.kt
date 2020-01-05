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

package com.mrbinwin.hungryapp.mvp.contracts

import android.net.Uri
import com.mrbinwin.hungryapp.data.models.Recipe
import io.reactivex.Observable

interface RecipesViewContract: BaseView {
    var exportBackupClickEvent: Observable<Unit>?
    var importBackupClickEvent: Observable<Unit>?
    var recipeCreateClickEvent: Observable<Unit>?
    var recipeClickEvent: Observable<Recipe>?
    var recipesDeleteClickEvent: Observable<ArrayList<Int>>?

    fun importBackup()
    fun requestReadStoragePermissions()

    /**
     * Update UI after a backup file has been imported
     *
     */
    fun applyChanges()

    /**
     * Share (export) a backup file recently created
     *
     */
    fun shareFile(uri: Uri)

    /**
     * Navigate to a create Recipe screen
     *
     */
    fun showCreateRecipe()

    fun showErrorNoReadStoragePermissionsGranted()

    /**
     * Update the recipes list on UI
     *
     */
    fun showRecipes(recipes: ArrayList<Recipe>)

    /**
     * Navigate to a Recipe details screen
     *
     */
    fun showRecipe(recipe: Recipe)
}