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

import com.mrbinwin.hungryapp.mvp.contracts.BaseView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


abstract class BasePresenter<T : BaseView> {
    private val compositeDisposable = CompositeDisposable()
    protected var view: T? = null
        private set

    fun attachView(mvpView: T) {
        view = mvpView
        onAttachView(mvpView)
    }

    protected abstract fun onAttachView(view: T)

    abstract fun viewIsReady()

    fun detachView() {
        compositeDisposable.clear()
        onDetachView()
        view = null
    }

    protected abstract fun onDetachView()

    fun addSubscription(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }
}