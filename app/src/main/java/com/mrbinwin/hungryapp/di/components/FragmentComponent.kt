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

import com.mrbinwin.hungryapp.di.scopes.FragmentScope
import com.mrbinwin.hungryapp.dialogs.AddPictureDialog
import com.mrbinwin.hungryapp.fragments.RecipeDetailsFragment
import com.mrbinwin.hungryapp.fragments.RecipeEditFragment
import com.mrbinwin.hungryapp.fragments.RecipesFragment
import com.mrbinwin.hungryapp.fragments.ShoppingListFragment
import com.mrbinwin.hungryapp.mvp.presenters.RecipeDetailsPresenter
import com.mrbinwin.hungryapp.mvp.presenters.RecipeEditPresenter
import com.mrbinwin.hungryapp.mvp.presenters.RecipesPresenter
import com.mrbinwin.hungryapp.mvp.presenters.ShoppingListPresenter
import dagger.Component

/**
 * Dagger2 component
 * Provides injections on fragments lifecycle level
 *
 */
@FragmentScope
@Component(dependencies = [AppComponent::class])
interface FragmentComponent {
    fun inject(fragment: AddPictureDialog)
    fun inject(fragment: RecipeDetailsFragment)
    fun inject(presenter: RecipeDetailsPresenter)
    fun inject(fragment: RecipeEditFragment)
    fun inject(presenter: RecipeEditPresenter)
    fun inject(fragment: RecipesFragment)
    fun inject(presenter: RecipesPresenter)
    fun inject(fragment: ShoppingListFragment)
    fun inject(presenter: ShoppingListPresenter)
}