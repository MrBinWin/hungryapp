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

import com.mrbinwin.hungryapp.App
import java.io.File

/**
 * Remove all Files from a directory
 *
 */
fun clearDir(dir: File) {
    if (dir.isDirectory) {
        val children: Array<String>? = dir.list()
        children?.also {
            for (i in it.indices) {
                File(dir, it[i]).delete()
            }
        }
    }
}

fun clearTmpImagesDir() {
    val tmpImagesDir = getTmpImagesDir()
    clearDir(tmpImagesDir)
}

fun getBackupDir(): File {
    val dir = File(App.context.filesDir, "backup")
    dir.mkdirs()
    return dir
}

fun getImagesDir(): File {
    val imagesDir = File(App.context.filesDir, "images")
    imagesDir.mkdirs()
    return imagesDir
}

fun getRecipeImagesDir(recipeId: Int): File {
    val recipeImagesDir = File(getImagesDir(), "$recipeId")
    recipeImagesDir.mkdirs()
    return recipeImagesDir
}

fun getTmpImagesDir(): File {
    val imagesDir = File(getImagesDir(), "tmp")
    imagesDir.mkdirs()
    return imagesDir
}