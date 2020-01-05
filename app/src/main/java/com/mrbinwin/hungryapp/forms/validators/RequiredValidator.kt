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

package com.mrbinwin.hungryapp.forms.validators

import android.view.View
import com.google.android.material.textfield.TextInputLayout
import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.R

/**
 * Validates that a required form field is filled
 *
 */
class RequiredValidator: Validator {

    /**
     * @return error message if the field is empty or null if everything is ok
     *
     */
    override fun process(view: View): String? {
        if (view is TextInputLayout) {
            if (view.editText?.text.toString().trim().isEmpty()) {
                return App.context.getString(R.string.forms_error_validation_required)
            }
        }
        return null
    }
}