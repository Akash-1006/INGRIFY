package com.ingrify.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanFragment : Fragment() {

    private var latestTmpUri: Uri? = null
    private lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startScanButton: MaterialButton = view.findViewById(R.id.btn_start_scan)
        val uploadButton: MaterialButton = view.findViewById(R.id.btn_upload_image)
        val backButton: ImageView = view.findViewById(R.id.iv_back_button_login)

        imageView = view.findViewById(R.id.imageview)

        startScanButton.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        uploadButton.setOnClickListener {
            Toast.makeText(context, "Gallery Opening broooo!", Toast.LENGTH_SHORT).show()
            // Implement gallery logic here
        }
    }

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(context, "Camera permission is required to scan.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(
                    context,
                    "Camera access is needed to take pictures for scanning.",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        latestTmpUri = getTempFileUri()
        latestTmpUri?.let { uri ->
            takePictureLauncher.launch(uri)
        } ?: run {
            Toast.makeText(context, "Could not create temporary file for image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTempFileUri(): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "JPEG_${timeStamp}.jpg"
        val imageFile = File(requireContext().getExternalFilesDir(null), fileName)

        return try {
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                imageFile
            )
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }

    private val takePictureLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                latestTmpUri?.let { uri ->
                    displayImage(uri)
                    Toast.makeText(context, "Image captured! Processing...", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Image capture cancelled or failed.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun displayImage(imageUri: Uri) {
        try {
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(
                    requireContext().contentResolver,
                    imageUri
                )
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
            }
            imageView.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error loading image.", Toast.LENGTH_SHORT).show()
        }
    }
}
