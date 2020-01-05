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

package com.mrbinwin.hungryapp.mvp.presenters

import android.text.TextUtils
import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.data.AppDatabase
import com.mrbinwin.hungryapp.data.models.Product
import com.mrbinwin.hungryapp.di.components.DaggerFragmentComponent
import com.mrbinwin.hungryapp.mvp.contracts.RecipeDetailsViewContract
import com.mrbinwin.hungryapp.utils.getRecipeImagesDir
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * The Presenter handles a Recipe details screen
 *
 */
class RecipeDetailsPresenter @Inject constructor(): BasePresenter<RecipeDetailsViewContract>() {

    @Inject lateinit var appDatabase: AppDatabase

    init {
        DaggerFragmentComponent.builder().appComponent(App.component).build().inject(this)
    }

    override fun onAttachView(view: RecipeDetailsViewContract) {

    }

    override fun onDetachView() {

    }

    override fun viewIsReady() {
        view?.let {_view ->
            val getRecipeDisposable = appDatabase.recipeDao().get(_view.recipeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {recipe ->
                    _view.loadData(recipe)
                }
            addSubscription(getRecipeDisposable)

            _view.addIngredientsToShoppingListClickEvent?.let {
                addSubscription(
                    it.subscribe { ingredients ->
                        appDatabase.productDao().getMaxRank()
                            .subscribeOn(Schedulers.io())
                            .defaultIfEmpty(0)
                            .flatMapSingle { maxRank ->
                                Single.fromCallable {
                                    val products = ArrayList<Product>()
                                    for ((index, line) in ingredients.lines().reversed().withIndex()) {
                                        val rank = maxRank + 1 + index
                                        if (!TextUtils.isEmpty(line.trim())) {
                                            products.add(Product(line.trim(), false, rank))
                                        }
                                    }
                                    products
                                }
                            }
                            .flatMap { products ->
                                appDatabase.productDao().insertMany(products)
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { ids ->
                                _view.showMessageProductsAddedToShoppingList(ids.size)
                                _view.showShoppingList()
                            }
                    }
                )
            }

            _view.recipeDeleteClickEvent?.let {
                addSubscription(
                    it.subscribe {

                        view?.recipeId?.let {_recipeId ->
                            appDatabase.recipeDao().deleteById(_recipeId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe {
                                    Completable.fromCallable {
                                        getRecipeImagesDir(_recipeId).deleteRecursively()
                                    }
                                    .subscribeOn(Schedulers.io())
                                    .subscribe()
                                    view?.showRecipes()
                                }
                        }

                    }
                )
            }

        }
    }
}