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

import android.view.View
import com.google.android.material.textfield.TextInputLayout
import com.mrbinwin.hungryapp.forms.validators.RequiredValidator
import com.mrbinwin.hungryapp.forms.validators.Validator
import com.mrbinwin.hungryapp.utils.extension.markRequired

/**
 * Base class handles forms validation
 *
 * Example of usage:
 *      someForm = SomeImplementationOfBaseForm()
 *      someForm.addField(editText1, listOf(RequiredValidator())
 *      someForm.addField(editText2, emptyList())
 *      if (!someForm.validate().isValid) { someForm.showErrors() }
 *
 */
abstract class BaseForm {
    /**
     * Indicates if form fields are valid after the last validate() or not
     *
     */
    var isValid: Boolean = true
        private set

    /**
     * Invalid form fields mapped to the current related error messages
     *
     */
    var errors: MutableMap<View, String> = mutableMapOf()
        private set

    /**
     * Form fields mapped to their list of Validators
     *
     */
    private val fields: MutableMap<View, List<Validator>> = mutableMapOf()

    /**
     * @param view a new form field
     * @param validations a list of Validators related to this field
     *
     */
    fun addField(view: View, validations: List<Validator>) {
        fields[view] = validations

        if (validations.filterIsInstance<RequiredValidator>().isNotEmpty()) {
            if (view is TextInputLayout) {
                view.markRequired()
            }
        }
    }

    /**
     * Run form fields validation
     * - fill this.errors Map if there are any invalid fields
     * - set this.isValid to true if all fields are valid or false otherwise
     */
    @Synchronized
    open fun validate(): BaseForm {
        isValid = true
        cleanErrors()

        fields.map {(view, validations) ->
            validations.forEach validations@ { validator ->
                val errorMessage: String? = validator.process(view)
                if (errorMessage != null) {
                    isValid = false
                    errors[view] = errorMessage
                    return@validations
                }
            }
        }
        return this
    }

    open fun showErrors() {
        errors.map {(view, error) ->
            showError(view, error)
        }
    }

    private fun cleanErrors() {
        errors = mutableMapOf()
        fields.keys.forEach {view ->
            hideError(view)
        }
    }

    private fun hideError(view: View) {
        if (view is TextInputLayout) {
            view.error = null
        }
    }

    private fun showError(view: View, error: String) {
        if (view is TextInputLayout) {
            view.error = error
        }
    }
}