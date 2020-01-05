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

package com.mrbinwin.hungryapp.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.R
import com.mrbinwin.hungryapp.data.models.Recipe
import com.mrbinwin.hungryapp.di.components.DaggerFragmentComponent
import com.mrbinwin.hungryapp.dialogs.AddPictureDialog.Companion.ARGUMENT_ACTION_ID
import com.mrbinwin.hungryapp.dialogs.AddPictureDialog.Companion.PICK_FROM_GALLERY_ITEM
import com.mrbinwin.hungryapp.dialogs.AddPictureDialog.Companion.TAKE_FROM_CAMERA_ITEM
import com.mrbinwin.hungryapp.forms.EditRecipeForm
import com.mrbinwin.hungryapp.forms.validators.RequiredValidator
import com.mrbinwin.hungryapp.mvp.contracts.RecipeEditViewContract
import com.mrbinwin.hungryapp.mvp.presenters.RecipeEditPresenter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * Create/edit a Recipe screen
 *
 */
class RecipeEditFragment: BaseFragment(), RecipeEditViewContract {


    @BindView(R.id.btn_recipe_edit_picture_add) lateinit var buttonPictureAdd: Button
    @BindView(R.id.btn_recipe_edit_picture_delete) lateinit var buttonPictureDelete: Button
    @BindView(R.id.button_recipe_edit_recipe_save) lateinit var buttonRecipeSave: FloatingActionButton
    @BindView(R.id.image_recipe_edit_picture) lateinit var imagePicture: ImageView
    @BindView(R.id.layout_recipe_edit_picture_add) lateinit var layoutPictureAdd: ViewGroup
    @BindView(R.id.layout_recipe_edit_picture_edit) lateinit var layoutPictureEdit: ViewGroup
    @BindView(R.id.text_layout_recipe_edit_title) lateinit var textLayoutTitle: TextInputLayout
    @BindView(R.id.text_layout_recipe_edit_ingredients) lateinit var textLayoutIngredients: TextInputLayout
    @BindView(R.id.text_layout_recipe_edit_directions) lateinit var textLayoutDirections: TextInputLayout
    @BindView(R.id.text_layout_recipe_edit_cooking_time) lateinit var textLayoutCookingTime: TextInputLayout

    @BindView(R.id.text_recipe_edit_title) lateinit var textTitle: EditText
    @BindView(R.id.text_recipe_edit_ingredients) lateinit var textIngredients: EditText
    @BindView(R.id.text_recipe_edit_directions) lateinit var textDirections: EditText
    @BindView(R.id.text_recipe_edit_cooking_time) lateinit var textCookingTime: EditText
    @BindView(R.id.text_recipe_edit_picture) lateinit var textPicture: EditText
    @Inject lateinit var presenter: RecipeEditPresenter


    override val isSupportUpButtonShown: Boolean = true
    override var pictureAddClickEvent: Observable<Unit>? = null
    override var pictureDeleteClickEvent: Observable<Unit>? = null
    override var recipeId: Int? = null
    override var recipeSaveClickEvent: Observable<EditRecipeForm>? = null

    private var editRecipeForm: EditRecipeForm? = null

    private val pictureAddClickSubject = PublishSubject.create<Unit>()
    private val pictureDeleteClickSubject = PublishSubject.create<Unit>()
    private val recipeSaveClickSubject = PublishSubject.create<EditRecipeForm>()

    companion object {
        const val ARGUMENT_RECIPE_ID = "ARGUMENT_RECIPE_ID"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        DaggerFragmentComponent.builder().appComponent(App.component).build().inject(this)
        presenter.attachView(this)
        val view: View = inflater.inflate(R.layout.fragment_recipe_edit, container, false)
        ButterKnife.bind(this, view)
        val bundle = this.arguments
        if (bundle != null) {
            val recipeId = bundle.getInt(ARGUMENT_RECIPE_ID)
            if (recipeId > 0) {
                this.recipeId = recipeId
            }
        }
        pictureAddClickEvent = pictureAddClickSubject.hide()
        pictureDeleteClickEvent = pictureDeleteClickSubject.hide()
        recipeSaveClickEvent = recipeSaveClickSubject.hide()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {_activity ->
            if (_activity is AppCompatActivity) {
                recipeId?.let {
                    _activity.title = _activity.getString(R.string.recipe_edit_page_title)
                } ?: run {
                    _activity.title = _activity.getString(R.string.recipe_edit_new_page_title)
                }
            }
        }

        editRecipeForm = EditRecipeForm().also {
            it.recipeId = recipeId
            it.setTitleField(textLayoutTitle, listOf(RequiredValidator()))
            it.setIngredientsField(textLayoutIngredients, emptyList())
            it.setDirectionsField(textLayoutDirections, emptyList())
            it.setCookingTimeField(textLayoutCookingTime, emptyList())
            it.setPictureField(textPicture, emptyList())
        }

        buttonRecipeSave.setOnClickListener {
            editRecipeForm?.let {
                recipeSaveClickSubject.onNext(it)
            }
        }

        buttonPictureAdd.setOnClickListener {
            pictureAddClickSubject.onNext(Unit)
        }

        buttonPictureDelete.setOnClickListener {
            pictureDeleteClickSubject.onNext(Unit)
        }

        presenter.viewIsReady()
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun loadData(recipe: Recipe) {
        textLayoutTitle.editText?.setText(recipe.title)
        textLayoutIngredients.editText?.setText(recipe.ingredients)
        textLayoutDirections.editText?.setText(recipe.directions)
        textLayoutCookingTime.editText?.setText(recipe.cookingTime)
        updatePicture(recipe.picture)
    }

    override fun updatePicture(path: String?) {
        if (path == null || TextUtils.isEmpty(path)) {
            textPicture.setText("")
            imagePicture.setImageDrawable(null)
            layoutPictureAdd.visibility = View.VISIBLE
            layoutPictureEdit.visibility = View.GONE
        } else {
            textPicture.setText(path)
            Glide.with(this)
                .load(path)
                .centerCrop()
                .into(imagePicture)
            layoutPictureAdd.visibility = View.GONE
            layoutPictureEdit.visibility = View.VISIBLE
        }
    }

    override fun showAddPictureDialog(action: Int?) {
        val bundle = Bundle()
        if (action != null && action in arrayOf(TAKE_FROM_CAMERA_ITEM, PICK_FROM_GALLERY_ITEM)) {
            bundle.putInt(ARGUMENT_ACTION_ID, action)
        }
        findNavController().navigate(R.id.addPictureDialog, bundle)
    }

    override fun showErrorNoReadStoragePermissionsGranted() {
        Toast.makeText(App.context, getString(R.string.recipe_edit_error_permissions_storage_read), Toast.LENGTH_SHORT).show()
    }

    override fun showRecipes() {
        findNavController().navigate(R.id.action_global_recipes_fragment)
    }


}