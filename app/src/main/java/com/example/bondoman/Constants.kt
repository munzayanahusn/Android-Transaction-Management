package com.example.bondoman
import android.Manifest

object Constants {
    const val TAG = "cameraX"
    const val REQUEST_CODE_PERMISSIONS = 123
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    const val  REQUEST_CODE_PICK_IMAGE = 1001
}