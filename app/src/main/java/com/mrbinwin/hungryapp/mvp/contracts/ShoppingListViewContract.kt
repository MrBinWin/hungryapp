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

import com.mrbinwin.hungryapp.data.models.Product
import com.mrbinwin.hungryapp.forms.EditProductForm
import io.reactivex.Observable

interface ShoppingListViewContract: BaseView {
    var deleteAllProductsClickEvent: Observable<Unit>?
    var productCreateClickEvent: Observable<EditProductForm>?
    var productDeleteClickEvent: Observable<Int>?

    /**
     * A Product has been dropped after drag&drop sorting
     *
     */
    var productMovedEvent: Observable<List<Product>>?
    var productUpdateClickEvent: Observable<Product>?
    var sortProductsClickEvent: Observable<Unit>?

    fun clearProductForm()

    /**
     * Update the list of Products on UI
     *
     */
    fun showProducts(products: ArrayList<Product>)
}