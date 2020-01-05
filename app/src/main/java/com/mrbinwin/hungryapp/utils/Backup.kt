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
import com.mrbinwin.hungryapp.data.AppDatabase
import com.mrbinwin.hungryapp.utils.extension.toString
import java.io.File
import java.util.*

/**
 * Class creates and imports backups of the app's data
 *
 */
class Backup {

    /**
     * Create a zip backup of the app's database and images folder
     * @return Uri of just created backup
     *
     */
    fun requestBackup(): Uri {
        val backupDir = getBackupDir()
        clearDir(backupDir)

        val currentDate = Calendar.getInstance().time.toString("yyyy-MM-dd")
        val backupFilename = "hungyapp_backup_${currentDate}.zip"

        val imagesDir = getImagesDir().toString()
        val directories = mutableListOf(Zip.Source(imagesDir, "images", arrayOf("tmp")))
        val dbDir = App.context.getDatabasePath(AppDatabase.DB_NAME).parent
        dbDir?.let { _dbDir ->
            directories.add(Zip.Source(_dbDir, "database"))
        }
        val zipFile = Zip().zipAll(directories, File(backupDir, backupFilename).toString())
        return FileProvider.getUriForFile(App.context, "${BuildConfig.APPLICATION_ID}.provider", File(zipFile))
    }

    /**
     * Extract a zip backup from external Uri
     *
     * The list of folders of a zip archive:
     *      - "database" extracts to the internal database directory
     *      - "images" extracts to the internal files directory
     *
     */
    fun import(uri: Uri) {
        val backupDir = getBackupDir()
        clearDir(backupDir)

        Zip().extractAll(uri, backupDir)
        val backupDatabaseDir = File(backupDir,"database")
        if (backupDatabaseDir.exists()) {
            val dbDir = App.context.getDatabasePath(AppDatabase.DB_NAME).parent
            dbDir?.let { _dbDir ->
                backupDatabaseDir.copyRecursively(File(_dbDir), true)
                backupDatabaseDir.deleteRecursively()
            }
            val imagesDir = getImagesDir()
            clearDir(imagesDir)
            val backupImagesDir = File(backupDir,"images")
            if (backupImagesDir.exists()) {
                backupImagesDir.copyRecursively(imagesDir, true)
                backupImagesDir.deleteRecursively()
            }
        }
    }
}