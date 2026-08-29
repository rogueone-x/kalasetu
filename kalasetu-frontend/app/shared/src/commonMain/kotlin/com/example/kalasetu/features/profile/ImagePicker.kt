package com.example.kalasetu.features.profile

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): ImagePickerLauncher

interface ImagePickerLauncher {
    fun launch()
}
