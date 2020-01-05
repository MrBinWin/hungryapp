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

package com.mrbinwin.hungryapp.dialogs

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.mrbinwin.hungryapp.App
import com.mrbinwin.hungryapp.MainActivity
import com.mrbinwin.hungryapp.MainActivity.Companion.REQUEST_CODE_PERMISSIONS_READ_EXTERNAL_STORAGE_ADD_RECIPE_PICTURE
import com.mrbinwin.hungryapp.R
import com.mrbinwin.hungryapp.di.components.DaggerFragmentComponent
import com.mrbinwin.hungryapp.utils.CameraPhotoRepository
import com.mrbinwin.hungryapp.utils.isReadStoragePermissionGranted
import javax.inject.Inject


/**
 * The dialog runs one of two intents:
 * - Intent with ACTION_PICK that allows user to choose a picture from gallery
 * - Intent with ACTION_IMAGE_CAPTURE to take a picture from camera
 *
 */
class AddPictureDialog : DialogFragment() {

    /**
     * Creates and stores a path to a temporary image file that will be used by camera app
     *
     */
    @Inject lateinit var cameraPhotoRepository: CameraPhotoRepository

    companion object {

        const val ARGUMENT_ACTION_ID = "ARGUMENT_ACTION_ID"

        const val TAKE_FROM_CAMERA_ITEM = 1
        const val PICK_FROM_GALLERY_ITEM = 2

        const val TAKE_FROM_CAMERA_ITEM_POSITION = 0
        const val PICK_FROM_GALLERY_ITEM_POSITION = 1
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        DaggerFragmentComponent.builder().appComponent(App.component).build().inject(this)

        val builder = AlertDialog.Builder(activity)
        val items: Map<Int, String> = mapOf(
            TAKE_FROM_CAMERA_ITEM_POSITION to getString(R.string.add_picture_dialog_capture_from_camera),
            PICK_FROM_GALLERY_ITEM_POSITION to getString(R.string.add_picture_dialog_pick_from_gallery)
        )

        builder.setItems(
            items.values.toTypedArray()
        ) { _, position ->
            when (position) {
                TAKE_FROM_CAMERA_ITEM_POSITION -> {
                    takeFromCamera()
                }
                PICK_FROM_GALLERY_ITEM_POSITION -> {
                    pickFromGallery()
                }
            }
        }
        return builder.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            if (it.getInt(ARGUMENT_ACTION_ID) == PICK_FROM_GALLERY_ITEM) {
                pickFromGallery()
                dismiss()
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun pickFromGallery() {
        activity?.let  { _activity ->
            if (!isReadStoragePermissionGranted()) {
                ActivityCompat.requestPermissions(_activity, arrayOf(READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSIONS_READ_EXTERNAL_STORAGE_ADD_RECIPE_PICTURE)
                return
            }
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickIntent.type = "image/*"
            activity?.startActivityForResult(pickIntent, MainActivity.REQUEST_CODE_PICK_FROM_GALLERY)
        }
    }

    private fun takeFromCamera() {
        cameraPhotoRepository.createTmpImageFile()?.let { _uri ->
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, _uri)
            activity?.startActivityForResult(cameraIntent, MainActivity.REQUEST_CODE_CAPTURE_FROM_CAMERA)
        }
    }
}