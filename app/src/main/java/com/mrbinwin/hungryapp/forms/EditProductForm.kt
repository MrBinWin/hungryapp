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
import com.mrbinwin.hungryapp.forms.validators.Validator

/**
 * The form represents a Product in Shopping list
 *
 */
class EditProductForm: BaseForm() {
    var productId: Int? = null
    private var titleField: EditText? = null

    override fun showErrors() {}

    fun setTitleField(view: EditText, validations: List<Validator>) {
        addField(view, validations)
        titleField = view
    }

    fun getData(): FormData {
        return FormData(
            productId,
            titleField?.text.toString()
        )
    }

    data class FormData(
        val productId: Int?,
        val title: String
    )
}