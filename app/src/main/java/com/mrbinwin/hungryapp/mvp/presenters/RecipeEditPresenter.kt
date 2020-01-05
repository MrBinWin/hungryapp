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
import android.provider.MediaStore
import android.text.TextUtils
import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.MainActivity
import com.mrbinwin.hungryapp.MainActivity.Companion.REQUEST_CODE_PERMISSIONS_READ_EXTERNAL_STORAGE_ADD_RECIPE_PICTURE
import com.mrbinwin.hungryapp.data.AppDatabase
import com.mrbinwin.hungryapp.data.models.Recipe
import com.mrbinwin.hungryapp.di.components.DaggerFragmentComponent
import com.mrbinwin.hungryapp.dialogs.AddPictureDialog
import com.mrbinwin.hungryapp.mvp.contracts.RecipeEditViewContract
import com.mrbinwin.hungryapp.utils.CameraPhotoRepository
import com.mrbinwin.hungryapp.utils.getRecipeImagesDir
import com.mrbinwin.hungryapp.utils.getTmpImagesDir
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.UUID.randomUUID
import javax.inject.Inject

/**
 * The Presenter handles a create/edit Recipe screen
 *
 */
class RecipeEditPresenter @Inject constructor(): BasePresenter<RecipeEditViewContract>() {

    @Inject lateinit var appDatabase: AppDatabase
    @Inject lateinit var cameraPhotoRepository: CameraPhotoRepository

    init {
        DaggerFragmentComponent.builder().appComponent(App.component).build().inject(this)
    }

    override fun onAttachView(view: RecipeEditViewContract) {

    }

    override fun onDetachView() {

    }

    override fun viewIsReady() {

        view?.let {_view ->
            _view.recipeId?.let {_recipeId ->
                val getRecipeDisposable = appDatabase.recipeDao().get(_recipeId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {recipe ->
                        _view.loadData(recipe)
                    }
                addSubscription(getRecipeDisposable)
            }
        }

        view?.pictureAddClickEvent?.let {
            addSubscription(
                it.subscribe { view?.showAddPictureDialog() }
            )
        }

        view?.pictureDeleteClickEvent?.let {
            addSubscription(
                it.subscribe { view?.updatePicture(null) }
            )
        }

        view?.recipeSaveClickEvent?.let {
            addSubscription(
                it.subscribe {editRecipeForm ->

                    if (!editRecipeForm.validate().isValid) {
                        editRecipeForm.showErrors()
                        return@subscribe
                    }

                    val formData = editRecipeForm.getData()

                    // update a recipe
                    if (formData.recipeId != null) {
                        appDatabase.recipeDao().get(formData.recipeId)
                            .flatMapSingle { _recipe ->
                                Single.fromCallable {
                                    replaceRecipePicture(_recipe, formData.recipeId, formData.picture)
                                }
                            }
                            .flatMapCompletable { _recipe ->
                                _recipe.title = formData.title
                                _recipe.ingredients = formData.ingredients
                                _recipe.directions = formData.directions
                                _recipe.cookingTime = formData.cookingTime
                                appDatabase.recipeDao().update(_recipe)
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                { view?.showRecipes() },
                                { e -> e.printStackTrace() }
                            )
                    // create a recipe
                    } else {
                        val recipe = Recipe(
                            formData.title,
                            formData.ingredients,
                            formData.directions,
                            formData.cookingTime,
                            ""
                        )
                        appDatabase.recipeDao().insert(recipe)
                            .flatMapMaybe { _recipeIdLong ->
                                appDatabase.recipeDao().get(_recipeIdLong.toInt())
                            }
                            .flatMapSingle { _recipe ->
                                _recipe.id?.let {
                                    Single.fromCallable {
                                        replaceRecipePicture(_recipe, _recipe.id!!, formData.picture)
                                    }
                                } ?: Single.just(_recipe)
                            }
                            .flatMapCompletable { _recipe ->
                                appDatabase.recipeDao().update(_recipe)
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                { view?.showRecipes() },
                                { e -> e.printStackTrace() }
                            )
                    }
                }
            )
        }

        view?.onActivityResultEvent?.let {
            addSubscription(
                it.subscribe onNext@ { (requestCode, resultCode, intent) ->
                    if (resultCode != Activity.RESULT_OK) {
                        return@onNext
                    }
                    view?.let { _view ->
                        when (requestCode) {
                            MainActivity.REQUEST_CODE_CAPTURE_FROM_CAMERA -> {
                                cameraPhotoRepository.currentTmpImageFilePath?.let { _picturePath ->
                                    _view.updatePicture(_picturePath)
                                }
                            }
                            MainActivity.REQUEST_CODE_PICK_FROM_GALLERY -> {
                                intent?.data?.let { _uri ->
                                    Single.fromCallable { savePictureToStorage(_uri) }
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe { picturePath ->
                                            picturePath?.let { _picturePath -> _view.updatePicture(_picturePath) }
                                        }

                                }
                            }
                            else -> return@onNext
                        }
                    }
                }
            )
        }

        view?.onRequestPermissionsResultEvent?.let {
            addSubscription(
                it.subscribe onNext@ { (requestCode, _, grantResults) ->
                    when (requestCode) {
                        REQUEST_CODE_PERMISSIONS_READ_EXTERNAL_STORAGE_ADD_RECIPE_PICTURE -> {
                            if (grantResults.first() != PackageManager.PERMISSION_GRANTED) {
                                view?.showErrorNoReadStoragePermissionsGranted()
                                view?.showAddPictureDialog()
                            } else {
                                view?.showAddPictureDialog(AddPictureDialog.PICK_FROM_GALLERY_ITEM)
                            }

                        }
                        else -> return@onNext
                    }
                }
            )
        }
    }

    /**
     * Delete the current picture of the Recipe,
     * move tmpPicturePath to a Recipe images directory and
     * update Recipe.picture with new one
     *
     */
    private fun replaceRecipePicture(recipe: Recipe, recipeId: Int, tmpPicturePath: String): Recipe {
        return if (recipe.picture == tmpPicturePath) {
            recipe
        } else {

            val previousPicture = recipe.picture

            if (!TextUtils.isEmpty(tmpPicturePath)) {
                val tmpPictureFile = File(tmpPicturePath)
                val resultFile = File(getRecipeImagesDir(recipeId), tmpPictureFile.name)
                tmpPictureFile.copyTo(resultFile, true)
                recipe.picture = resultFile.path
                tmpPictureFile.delete()
            } else {
                recipe.picture = ""
            }

            if (!TextUtils.isEmpty(previousPicture)) {
                File(previousPicture).delete()
            }
            recipe
        }
    }

    /**
     * Save a picture from external Uri to internal storage tmp images directory
     * @return path of the picture in internal storage
     *
     */
    private fun savePictureToStorage(uri: Uri): String? {
        try {
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
            var fileName = ""
            App.context.contentResolver.query(uri, projection, null, null, null)?.use { metaCursor ->
                if (metaCursor.moveToFirst()) {
                    fileName = metaCursor.getString(0)
                }
            }
            var fileExtension: String = fileName.substring(fileName.lastIndexOf("."))
            fileExtension = if (fileExtension.toLowerCase(Locale.getDefault()) in arrayOf(".jpg", ".jpeg", ".png")) {
                fileExtension.toLowerCase(Locale.getDefault())
            } else {
                ".jpg"
            }
            fileName =  randomUUID().toString() + fileExtension
            val resultFile = File(getTmpImagesDir(), fileName)

            val inputStream: InputStream? = App.context.contentResolver.openInputStream(uri)
            inputStream?.let { _fileIn ->
                FileOutputStream(resultFile).use { fileOut ->
                    _fileIn.copyTo(fileOut)
                }
                _fileIn.close()
            }
            return  resultFile.path
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}