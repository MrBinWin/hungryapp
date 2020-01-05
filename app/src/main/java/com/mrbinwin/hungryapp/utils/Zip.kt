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
import com.mrbinwin.hungryapp.App
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


/**
 * The class can zip and extract files with folder structure
 * Example of usage:
 *  val sourceDir: File = getSourceDir()
 *  val directories = mutableListOf(Zip.Source(sourceDir.toString(), "internalZipDirectory", arrayOf("tmp")))
 *  Zip().zipAll(directories, File(destinationPath, "filename.zip").toString())
 *
 */
class Zip {

    /**
     * Put a list of directories to a zip archive
     * @param directories a list of Sources
     * @param zipFile Path to destination zip file
     * @return path to the result zip file
     *
     */
    fun zipAll(directories: List<Source>, zipFile: String): String {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use {
            for ((directory, zipInternalDirectory, filenamesBlacklist) in directories) {
                zipFiles(
                    zipOut=it,
                    sourceFile=File(directory),
                    parentDirPath=zipInternalDirectory,
                    filenamesBlacklist=filenamesBlacklist
                )
            }
        }
        return zipFile
    }

    fun extractAll(sourceUri: Uri, destination: File) {
        ZipInputStream(App.context.contentResolver.openInputStream(sourceUri)).use { zis ->
            val bufferSize = 4096
            while (true) {
                val entry = zis.nextEntry ?: break
                val file = File(destination, entry.name)

                if (entry.isDirectory) {
                    file.mkdirs()
                    continue
                }

                val buffer = ByteArray(bufferSize)
                file.parentFile?.mkdirs()

                BufferedOutputStream(FileOutputStream(file)).use { out ->
                    while (true) {
                        val count = zis.read(buffer)
                        if (count < 0) {
                            break
                        }
                        out.write(buffer, 0, count)
                    }
                }
            }
        }
    }

    /**
     * Class represents a source directory to be archived
     * @param path
     * @param zipInternalDirectory
     * @param filenamesBlacklist array of file or directory names to be skipped
     *
     */
    data class Source(val path: String, val zipInternalDirectory: String = "", val filenamesBlacklist: Array<String> = arrayOf()) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Source

            if (path != other.path) return false
            if (zipInternalDirectory != other.zipInternalDirectory) return false
            if (!filenamesBlacklist.contentEquals(other.filenamesBlacklist)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = path.hashCode()
            result = 31 * result + zipInternalDirectory.hashCode()
            result = 31 * result + filenamesBlacklist.contentHashCode()
            return result
        }
    }

    private fun zipFiles(zipOut: ZipOutputStream, sourceFile: File, parentDirPath: String, filenamesBlacklist: Array<String> = emptyArray()) {

        val buffer = ByteArray(2048)
        val listFiles = sourceFile.listFiles() ?: arrayOf<File>()
        for (f in listFiles) {
            if (f.name in filenamesBlacklist) {
                continue
            }
            if (f.isDirectory) {
                val path = if (parentDirPath == "") {
                    f.name
                } else {
                    parentDirPath + File.separator + f.name
                }
                val entry = ZipEntry(path + File.separator)
                entry.time = f.lastModified()
                entry.size = f.length()
                zipOut.putNextEntry(entry)
                zipFiles(zipOut, f, path)
            } else {
                BufferedInputStream(FileInputStream(f)).use { origin ->
                    val path = if (parentDirPath != "") {
                        parentDirPath + File.separator + f.name
                    } else {
                        f.name
                    }
                    val entry = ZipEntry(path)
                    entry.time = f.lastModified()
                    entry.size = f.length()
                    zipOut.putNextEntry(entry)
                    while (true) {
                        val readBytes = origin.read(buffer)
                        if (readBytes == -1) {
                            break
                        }
                        zipOut.write(buffer, 0, readBytes)
                    }
                }
            }
        }
    }

}