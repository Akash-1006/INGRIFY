package com.ingrify.app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class ImagePreviewFragment : Fragment() {

    private var imageUri: Uri? = null
    private lateinit var imagePreviewDisplay: ImageView
    private lateinit var confirmScanButton: MaterialButton
    private lateinit var retakeImageButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    private val FRAGMENT_CONTAINER_ID = R.id.fragment_container

    companion object {
        private const val ARG_IMAGE_URI = "imageUri"

        fun newInstance(imageUri: Uri): ImagePreviewFragment {
            val fragment = ImagePreviewFragment()
            val args = Bundle().apply {
                putString(ARG_IMAGE_URI, imageUri.toString())
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(ARG_IMAGE_URI)?.let { uriString ->
            imageUri = Uri.parse(uriString)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imagePreviewDisplay = view.findViewById(R.id.image_preview_display)
        confirmScanButton = view.findViewById(R.id.btn_confirm_scan)
        retakeImageButton = view.findViewById(R.id.btn_retake_image)
        progressBar = view.findViewById(R.id.progress_bar_preview)

        imageUri?.let { uri ->
            displayImage(uri)
        } ?: run {
            Toast.makeText(context, "No image to preview.", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

        confirmScanButton.setOnClickListener {
            imageUri?.let { uri ->
                uploadImageToOcr(uri)
            } ?: run {
                Toast.makeText(context, "Error: No image to process.", Toast.LENGTH_SHORT).show()
            }
        }

        retakeImageButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<ImageView>(R.id.iv_back_button_preview).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun displayImage(uri: Uri) {
        try {
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(
                    requireContext().contentResolver,
                    uri
                )
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
            imagePreviewDisplay.setImageBitmap(bitmap)
            imagePreviewDisplay.scaleType = ImageView.ScaleType.FIT_CENTER
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("ImagePreviewFragment", "Error loading image: ${e.message}")
            Toast.makeText(context, "Error loading image for preview.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun uploadImageToOcr(imageUri: Uri) {
        setUiState(isProcessing = true)
        viewLifecycleOwner.lifecycleScope.launch {
            var tempFileForUpload: File? = null
            try {
                // Ensure temp file creation is done on the IO thread
                tempFileForUpload = withContext(Dispatchers.IO) {
                    createTempFileFromUri(requireContext(), imageUri)
                }

                if (tempFileForUpload == null) {
                    // All UI updates must be on the main thread
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to prepare image for upload.", Toast.LENGTH_SHORT).show()
                        loadScanResultFragment(null)
                    }
                    return@launch
                }

                val requestFile = tempFileForUpload.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", tempFileForUpload.name, requestFile)

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.uploadImageForOcr(imagePart)
                }

                // All subsequent UI updates must be on the main thread
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val ocrResponse = response.body()
                        if (ocrResponse != null) {
                            Log.d("ImagePreviewFragment", "OCR Result: ${ocrResponse.ocrResult}")
                            // CORRECTED: Pass the entire ocrResponse object
                            loadScanResultFragment(ocrResponse)
                        } else {
                            val msg = "OCR response successful but no body received."
                            Log.w("ImagePreviewFragment", msg)
                            loadScanResultFragment(null, msg)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val msg = "Error during OCR: ${response.code()} - ${errorBody ?: "Unknown error"}"
                        Log.e("ImagePreviewFragment", "OCR API Error: $msg")
                        loadScanResultFragment(null, msg)
                    }
                }
            } catch (e: Exception) {
                // All UI updates must be on the main thread
                withContext(Dispatchers.Main) {
                    val msg = "An unexpected error occurred during scan: ${e.localizedMessage ?: "Unknown error"}"
                    Log.e("ImagePreviewFragment", msg, e)
                    loadScanResultFragment(null, msg)
                }
            } finally {
                // All UI updates must be on the main thread
                withContext(Dispatchers.Main) {
                    tempFileForUpload?.let { file ->
                        if (file.exists()) {
                            file.delete()
                            Log.d("ImagePreviewFragment", "Temporary image file deleted after upload: ${file.name}")
                        }
                    }
                    setUiState(isProcessing = false)
                }
            }
        }
    }

    private fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, "upload_image_copy_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("ImagePreviewFragment", "Error creating temp file from URI: ${e.message}", e)
            null
        }
    }

    // Corrected function signature
    private fun loadScanResultFragment(ocrResponse: OcrResponse?, errorMessage: String? = null) {
        val scanResultFragment = ScanResultFragment.newInstance(ocrResponse, errorMessage)
        parentFragmentManager.beginTransaction()
            .replace(FRAGMENT_CONTAINER_ID, scanResultFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setUiState(isProcessing: Boolean) {
        progressBar.visibility = if (isProcessing) View.VISIBLE else View.GONE
        confirmScanButton.isEnabled = !isProcessing
        retakeImageButton.isEnabled = !isProcessing
        imagePreviewDisplay.alpha = if (isProcessing) 0.5f else 1.0f
    }
}