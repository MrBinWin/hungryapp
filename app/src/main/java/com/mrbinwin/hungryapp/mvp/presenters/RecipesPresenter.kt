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

import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.MainActivity
import com.mrbinwin.hungryapp.data.AppDatabase
import com.mrbinwin.hungryapp.di.components.DaggerFragmentComponent
import com.mrbinwin.hungryapp.mvp.contracts.RecipesViewContract
import com.mrbinwin.hungryapp.utils.Backup
import com.mrbinwin.hungryapp.utils.getRecipeImagesDir
import com.mrbinwin.hungryapp.utils.isReadStoragePermissionGranted
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class RecipesPresenter @Inject constructor(): BasePresenter<RecipesViewContract>() {

    @Inject lateinit var appDatabase: AppDatabase

    init {
        DaggerFragmentComponent.builder().appComponent(App.component).build().inject(this)
    }

    override fun onAttachView(view: RecipesViewContract) {

    }

    override fun onDetachView() {

    }

    override fun viewIsReady() {

        view?.let { _view ->
            _view.recipeClickEvent?.let {
                addSubscription(
                    it.subscribe { recipe ->
                        _view.showRecipe(recipe)
                    }
                )
            }

            _view.recipeCreateClickEvent?.let {
                addSubscription(
                    it.subscribe {
                        _view.showCreateRecipe()
                    }
                )
            }

            _view.recipesDeleteClickEvent?.let {
                addSubscription(
                    it.subscribe { recipeIds ->
                        appDatabase.recipeDao().deleteMany(recipeIds)
                            .subscribeOn(Schedulers.io())
                            .subscribe {
                                updateRecipesList()
                                recipeIds.map { _recipeId ->
                                    Completable.fromCallable {
                                        getRecipeImagesDir(_recipeId).deleteRecursively()
                                    }
                                        .subscribeOn(Schedulers.io())
                                        .subscribe()
                                }
                            }
                    }
                )
            }

            _view.exportBackupClickEvent?.let {
                addSubscription(
                    it.subscribe {
                        Single.fromCallable<Uri> {
                            Backup().requestBackup()
                        }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { backupUri ->
                                _view.shareFile(backupUri)
                            }
                    }
                )
            }

            _view.importBackupClickEvent?.let {
                addSubscription(
                    it.subscribe {
                        if (!isReadStoragePermissionGranted()) {
                            _view.requestReadStoragePermissions()
                            return@subscribe
                        }
                        _view.importBackup()
                    }
                )
            }

            _view.onActivityResultEvent?.let {
                addSubscription(
                    it.subscribe onNext@ { (requestCode, resultCode, intent) ->
                        if (resultCode != Activity.RESULT_OK) {
                            return@onNext
                        }
                        when (requestCode) {
                            MainActivity.REQUEST_CODE_IMPORT_BACKUP -> {
                                intent?.data?.let { _uri ->
                                    Completable.fromCallable {
                                        Backup().import(_uri)
                                    }
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            _view.applyChanges()
                                        }

                                }
                            }
                            else -> return@onNext
                        }
                    }
                )
            }

            /**
             * The Read External Storage Permission is granted so user can import a backup file
             *
             */
            _view.onRequestPermissionsResultEvent?.let {
                addSubscription(
                    it.subscribe onNext@ { (requestCode, _, grantResults) ->
                        when (requestCode) {
                            MainActivity.REQUEST_CODE_PERMISSIONS_READ_EXTERNAL_STORAGE_ADD_BACKUP -> {
                                if (grantResults.first() != PackageManager.PERMISSION_GRANTED) {
                                    _view.showErrorNoReadStoragePermissionsGranted()
                                } else {
                                    _view.importBackup()
                                }
                            }
                            else -> return@onNext
                        }
                    }
                )
            }
        }

        updateRecipesList()
    }

    /**
     * Receive a list of Recipes from database and update UI
     *
     */
    private fun updateRecipesList() {
        val getAllRecipesFromDbSubscription: Disposable = appDatabase.recipeDao().getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { recipes ->
                view?.showRecipes(ArrayList(recipes))
            }
        addSubscription(getAllRecipesFromDbSubscription)
    }
}