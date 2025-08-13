package com.ingrify.app

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanFragment : Fragment() {

    private var latestTmpUri: Uri? = null
    private var startScanButton: MaterialButton? = null
    private var uploadButton: MaterialButton? = null
    private var imagePlaceholder: ImageView? = null

    private val FRAGMENT_CONTAINER_ID = R.id.fragment_container

    // Declare launchers as nullable properties
    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null
    private var takePictureLauncher: ActivityResultLauncher<Uri>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null // New: Declare galleryLauncher here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize requestPermissionLauncher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(context, "Camera permission is required to scan.", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize takePictureLauncher
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                latestTmpUri?.let { uri ->
                    loadPreviewFragment(uri)
                }
            } else {
                Toast.makeText(context, "Image capture cancelled or failed.", Toast.LENGTH_SHORT).show()
                resetUiForInitialScan()
            }
        }

        // New: Initialize galleryLauncher here in onCreate()
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                loadPreviewFragment(it)
            } ?: Toast.makeText(context, "No image selected.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements. Using ?: return to exit early if a critical view is not found.
        startScanButton = view.findViewById(R.id.btn_start_scan) ?: run {
            Log.e("ScanFragment", "Error: btn_start_scan not found in layout.")
            Toast.makeText(context, "Layout error: Scan button missing.", Toast.LENGTH_LONG).show()
            return
        }
        uploadButton = view.findViewById(R.id.btn_upload_image) ?: run {
            Log.e("ScanFragment", "Error: btn_upload_image not found in layout.")
            Toast.makeText(context, "Layout error: Upload button missing.", Toast.LENGTH_LONG).show()
            return
        }
        imagePlaceholder = view.findViewById(R.id.imageview) ?: run {
            Log.e("ScanFragment", "Error: image_placeholder_or_preview not found in layout. Check fragment_scan.xml")
            Toast.makeText(context, "Layout error: Image placeholder missing.", Toast.LENGTH_LONG).show()
            return
        }

        // Set initial UI state
        resetUiForInitialScan()

        // Set Listeners using safe call operator (?.)
        startScanButton?.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        // Updated: Now just launch the gallery using the pre-registered launcher
        uploadButton?.setOnClickListener {
            galleryLauncher?.launch("image/*") // This just launches, no re-registration
        }

        val backButton: ImageView? = view.findViewById(R.id.iv_back_button_scan)
        backButton?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    // Simplified UI state for ScanFragment
    private fun resetUiForInitialScan() {
        startScanButton?.visibility = View.VISIBLE
        uploadButton?.visibility = View.VISIBLE
        imagePlaceholder?.visibility = View.VISIBLE
        imagePlaceholder?.setImageResource(R.drawable.ic_scan)
        imagePlaceholder?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.password_toggle_blue))
        imagePlaceholder?.background = ContextCompat.getDrawable(requireContext(), R.drawable.dashed_border)
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
                requestPermissionLauncher?.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher?.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        latestTmpUri = getTempFileUri()
        latestTmpUri?.let { uri ->
            takePictureLauncher?.launch(uri)
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
            Log.e("ScanFragment", "Error getting temp file URI: ${e.message}")
            null
        }
    }

    private fun loadPreviewFragment(imageUri: Uri) {
        val imagePreviewFragment = ImagePreviewFragment.newInstance(imageUri)
        parentFragmentManager.beginTransaction()
            .replace(FRAGMENT_CONTAINER_ID, imagePreviewFragment)
            .addToBackStack(null)
            .commit()
    }
}