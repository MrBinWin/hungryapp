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

import com.mrbinwin.hungryapp.data.models.Recipe
import com.mrbinwin.hungryapp.forms.EditRecipeForm
import io.reactivex.Observable

interface RecipeEditViewContract: BaseView {
    var recipeId: Int?
    var pictureAddClickEvent: Observable<Unit>?
    var pictureDeleteClickEvent: Observable<Unit>?
    var recipeSaveClickEvent: Observable<EditRecipeForm>?
    fun loadData(recipe: Recipe)
    fun showAddPictureDialog(action: Int? = 0)
    fun showErrorNoReadStoragePermissionsGranted()
    /**
     * Navigate to Recipes screen
     *
     */
    fun showRecipes()
    fun updatePicture(path: String?)
}