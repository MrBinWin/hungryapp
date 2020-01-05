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

package com.mrbinwin.hungryapp.forms

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import com.mrbinwin.hungryapp.forms.validators.Validator

/**
 * The form represents a Recipe on an edit Recipe screen
 *
 */
class EditRecipeForm: BaseForm() {
    var recipeId: Int? = null
    private var titleField: TextInputLayout? = null
    private var ingredientsField: TextInputLayout? = null
    private var directionsField: TextInputLayout? = null
    private var cookingTimeField: TextInputLayout? = null
    private var pictureField: EditText? = null

    fun setTitleField(view: TextInputLayout, validations: List<Validator>) {
        addField(view, validations)
        titleField = view
    }

    fun setIngredientsField(view: TextInputLayout, validations: List<Validator>) {
        addField(view, validations)
        ingredientsField = view
    }

    fun setDirectionsField(view: TextInputLayout, validations: List<Validator>) {
        addField(view, validations)
        directionsField = view
    }

    fun setCookingTimeField(view: TextInputLayout, validations: List<Validator>) {
        addField(view, validations)
        cookingTimeField = view
    }

    fun setPictureField(view: EditText, validations: List<Validator>) {
        addField(view, validations)
        pictureField = view
    }

    fun getData(): FormData {
        return FormData(
            recipeId,
            titleField?.editText?.text.toString(),
            ingredientsField?.editText?.text.toString(),
            directionsField?.editText?.text.toString(),
            cookingTimeField?.editText?.text.toString(),
            pictureField?.text.toString()
        )
    }

    data class FormData(
        val recipeId: Int?,
        val title: String,
        val ingredients: String,
        val directions: String,
        val cookingTime: String,
        val picture: String
    )
}