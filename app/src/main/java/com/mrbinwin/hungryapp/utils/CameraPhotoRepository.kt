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

package com.mrbinwin.hungryapp.utils

import android.net.Uri
import androidx.core.content.FileProvider
import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.BuildConfig
import java.io.File
import java.util.*

/**
 * The class creates and stores a tmp picture file to be passed to the default camera app
 *
 */
class CameraPhotoRepository {

    var currentTmpImageFileContentProviderUri: Uri? = null
        private set

    var currentTmpImageFilePath: String? = null
        private set

    fun createTmpImageFile(): Uri? {
        val fileName =  UUID.randomUUID().toString() + ".jpg"
        val resultFile = File(getTmpImagesDir(), fileName)
        currentTmpImageFilePath = resultFile.path
        currentTmpImageFileContentProviderUri = FileProvider.getUriForFile(App.context, "${BuildConfig.APPLICATION_ID}.provider", resultFile)
        return currentTmpImageFileContentProviderUri
    }
}