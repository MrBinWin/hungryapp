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

import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.data.AppDatabase
import com.mrbinwin.hungryapp.data.models.Product
import com.mrbinwin.hungryapp.di.components.DaggerFragmentComponent
import com.mrbinwin.hungryapp.mvp.contracts.RecipesViewContract
import com.mrbinwin.hungryapp.mvp.contracts.ShoppingListViewContract
import com.mrbinwin.hungryapp.utils.getRecipeImagesDir
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ShoppingListPresenter @Inject constructor(): BasePresenter<ShoppingListViewContract>() {

    @Inject lateinit var appDatabase: AppDatabase

    init {
        DaggerFragmentComponent.builder().appComponent(App.component).build().inject(this)
    }

    override fun onAttachView(view: ShoppingListViewContract) {

    }

    override fun onDetachView() {

    }

    override fun viewIsReady() {
        view?.let { _view ->

            _view.deleteAllProductsClickEvent?.let {
                addSubscription(
                    it.subscribe {
                        appDatabase.productDao().deleteAll()
                            .subscribeOn(Schedulers.io())
                            .subscribe {
                                updateShoppingList()
                            }
                    }
                )
            }

            _view.productCreateClickEvent?.let {
                addSubscription(
                    it.subscribe { editProductForm ->
                        if (!editProductForm.validate().isValid) {
                            editProductForm.showErrors()
                            return@subscribe
                        }
                        val title = editProductForm.getData().title
                        val product = Product(
                            title, false
                        )
                        appDatabase.productDao().getMaxRank()
                            .subscribeOn(Schedulers.io())
                            .defaultIfEmpty(-1)
                            .flatMapSingle { maxRank ->
                                product.rank = maxRank + 1
                                appDatabase.productDao().insert(product)
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                {
                                    _view.clearProductForm()
                                    updateShoppingList()
                                },
                                { e -> e.printStackTrace() }
                            )

                    }
                )
            }

            _view.productMovedEvent?.let {
                addSubscription(
                    it.subscribe { products ->
                        appDatabase.productDao().updateMany(products)
                            .subscribeOn(Schedulers.io())
                            .subscribe()
                    }
                )
            }

            _view.productDeleteClickEvent?.let {
                addSubscription(
                    it.subscribe { productId ->
                        appDatabase.productDao().deleteById(productId)
                            .subscribeOn(Schedulers.io())
                            .subscribe()
                    }
                )
            }

            _view.productUpdateClickEvent?.let {
                addSubscription(
                    it.subscribe { product ->
                        appDatabase.productDao().update(product)
                            .subscribeOn(Schedulers.io())
                            .subscribe()
                    }
                )
            }

            _view.sortProductsClickEvent?.let {
                addSubscription(
                    it.subscribe {
                        appDatabase.productDao().getAll()
                            .subscribeOn(Schedulers.io())
                            .flatMap { products ->
                                Single.fromCallable {
                                    products
                                        .sortedByDescending { product -> product.title.toLowerCase(Locale.getDefault()) }
                                        .mapIndexed { rank, product ->
                                            product.rank = rank
                                            product
                                        }
                                }
                            }
                            .flatMapCompletable { products ->
                                appDatabase.productDao().updateMany(products)
                            }
                            .subscribe {
                                updateShoppingList()
                            }
                    }
                )
            }
        }

        updateShoppingList()

    }

    /**
     * Receive a list of Products from database and update UI
     *
     */
    private fun updateShoppingList() {
        val getAllProductsFromDbSubscription: Disposable = appDatabase.productDao().getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { products ->
                view?.showProducts(ArrayList(products))
            }
        addSubscription(getAllProductsFromDbSubscription)
    }
}