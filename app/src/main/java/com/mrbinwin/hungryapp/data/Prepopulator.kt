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

package com.mrbinwin.hungryapp.data

import android.content.ContentValues
import android.content.res.XmlResourceParser
import android.database.sqlite.SQLiteDatabase.CONFLICT_ROLLBACK
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.R
import com.mrbinwin.hungryapp.data.AppDatabase.Companion.TABLE_NAME_RECIPE
import com.mrbinwin.hungryapp.utils.getRecipeImagesDir
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * This class is called from RoomDatabase.Callback()::onCreate()
 * and pre populates some initial data after the app is installed
 *
 */
class Prepopulator {
    companion object {

        fun prepopulateData(db: SupportSQLiteDatabase) {

            val values = parseRecipeValuesFromXmlResources()
                ?: return
            try {
                db.beginTransaction()
                val rowId = db.insert(TABLE_NAME_RECIPE, CONFLICT_ROLLBACK, values)
                val picturePath = movePictureFromAssetsToRecipeImagesDir(rowId.toInt())
                if (picturePath != "") {
                    val updateValues = ContentValues().apply {
                        put("picture", picturePath)
                    }
                    db.update(TABLE_NAME_RECIPE, CONFLICT_ROLLBACK, updateValues,
                        "id = ?", arrayOf(rowId)
                    )
                }
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
        }

        /**
         * Read data from /res/xml/recipe.xml
         *
         */
        private fun parseRecipeValuesFromXmlResources(): ContentValues? {

            val contentValues = ContentValues().apply {
                put("title", "")
                put("ingredients", "")
                put("directions", "")
                put("cookingTime", "")
                put("picture", "")
            }

            val xml = App.context.resources.getXml(R.xml.recipe)
            var eventType = -1
            var currentTag = ""

            while (eventType != XmlResourceParser.END_DOCUMENT) {

                if (eventType == XmlResourceParser.START_TAG) {
                    currentTag = xml.name.toLowerCase(Locale.getDefault())
                }

                if (eventType == XmlResourceParser.TEXT) {
                    when(currentTag) {
                        "title" -> {
                            contentValues.put("title", xml.text)
                        }
                        "ingredients" -> {
                            val ingredients = xml.text.lines().joinToString(
                                System.lineSeparator(),
                                transform=String::trim
                            )
                            contentValues.put("ingredients", ingredients)
                        }
                        "directions" -> {
                            val directions = xml.text.lines().joinToString(
                                System.lineSeparator(),
                                transform=String::trim
                            )
                            contentValues.put("directions", directions)
                        }
                        "cooking_time" -> {
                            contentValues.put("cookingTime", xml.text)
                        }
                    }
                }

                eventType = xml.next()

            }
            xml.close()

            if (contentValues.get("title") == "") {
                return null
            }

            return contentValues
        }

        /**
         * Move a picture for the initial recipe from assets to filesDir
         *
         */
        private fun movePictureFromAssetsToRecipeImagesDir(recipeId: Int): String {
            try {
                val asset = App.context.assets.open("fried_eggs.jpg")
                val resultFileName =  UUID.randomUUID().toString() + ".jpg"
                val resultFile = File(getRecipeImagesDir(recipeId), resultFileName)
                FileOutputStream(resultFile).use { fileOut ->
                    asset.copyTo(fileOut)
                }
                asset.close()
                return resultFile.path
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return ""
        }

    }
}
