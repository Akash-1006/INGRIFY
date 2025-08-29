package com.ingrify.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import com.google.gson.reflect.TypeToken
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanFragment : Fragment() {

    private var latestTmpUri: Uri? = null
    private var startScanButton: MaterialButton? = null
    private var uploadButton: MaterialButton? = null
    private var imagePlaceholder: ImageView? = null
    private lateinit var recentscanstitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var scanAdapter: ScanAdapter
    private val scanItems = mutableListOf<ScanItem>()

    private val FRAGMENT_CONTAINER_ID = R.id.fragment_container

    private var requestPermissionLauncher: ActivityResultLauncher<String>? = null
    private var takePictureLauncher: ActivityResultLauncher<Uri>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    private var cropImageLauncher: ActivityResultLauncher<android.content.Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request Camera Permission
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(context, "Camera permission is required to scan.", Toast.LENGTH_SHORT).show()
            }
        }

        // Camera Capture
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                latestTmpUri?.let { uri ->
                    startCrop(uri)
                }
            } else {
                Toast.makeText(context, "Image capture cancelled or failed.", Toast.LENGTH_SHORT).show()
                resetUiForInitialScan()
            }
        }

        // Gallery Selection
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                startCrop(it)
            } ?: Toast.makeText(context, "No image selected.", Toast.LENGTH_SHORT).show()
        }

        // Crop Result
        cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val resultUri = UCrop.getOutput(result.data!!)
                resultUri?.let {
                    loadPreviewFragment(it)
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(result.data!!)
                cropError?.printStackTrace()
                Toast.makeText(context, "Cropping failed: ${cropError?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scan, container, false)
        recyclerView = view.findViewById(R.id.recyclerScan)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


        scanAdapter = ScanAdapter(scanItems) { clickedItem ->
            val gson = Gson()

            // Step 1: Parse the analysis string into a list of Ingredient
            val analysisList: List<Ingredient> = gson.fromJson(
                clickedItem.analysis,
                object : TypeToken<List<Ingredient>>() {}.type
            )

            // Step 2: Build an OcrResponse manually
            val ocrResponse = OcrResponse(
                status = "success",
                ocrResult = clickedItem.raw_ocr_text,
                analysisResult = analysisList,
                imageFilename = clickedItem.image_filename,
                searchReference = clickedItem.scan_name,
                overallSafety = clickedItem.overall_safety
            )

            // Step 3: Open ScanResultFragment with this OcrResponse
            val fragment = ScanResultFragment.newInstance(
                ocrResponse,
                null // no error
            )

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        recyclerView.adapter = scanAdapter



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startScanButton = view.findViewById(R.id.btn_start_scan) ?: return
        uploadButton = view.findViewById(R.id.btn_upload_image) ?: return
        imagePlaceholder = view.findViewById(R.id.imageview) ?: return
        recentscanstitle=view.findViewById(R.id.recent_scans_title)

        fetchScanHistory()

        resetUiForInitialScan()

        startScanButton?.setOnClickListener {
            checkCameraPermissionAndLaunch()
        }

        uploadButton?.setOnClickListener {
            galleryLauncher?.launch("image/*")
        }

        val backButton: ImageView? = view.findViewById(R.id.iv_back_button_scan)
        backButton?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

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
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(context, "Camera access is needed to take pictures for scanning.", Toast.LENGTH_LONG).show()
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

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(
            File(requireContext().cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        )

        val options = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(90)
            setToolbarTitle("Crop Image")
            setFreeStyleCropEnabled(true)
            setHideBottomControls(false)
        }

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withOptions(options)

        cropImageLauncher?.launch(uCrop.getIntent(requireContext()))
    }

    private fun loadPreviewFragment(imageUri: Uri) {
        val imagePreviewFragment = ImagePreviewFragment.newInstance(imageUri)
        parentFragmentManager.beginTransaction()
            .replace(FRAGMENT_CONTAINER_ID, imagePreviewFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun fetchScanHistory() {
        val token = UserSessionManager.getAuthToken()
        recentscanstitle.visibility = View.GONE
        recyclerView.visibility = View.GONE
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Login to save Recent Scans.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getScan("Bearer $token")
                if (response.isSuccessful) {
                    val scans = response.body()?.data.orEmpty()

                    Log.d("API_RESPONSE", "Scans count: ${scans.size}")

                    scanItems.apply {
                        clear()
                        addAll(scans)
                    }
                    scanAdapter.notifyDataSetChanged()

                    // Always update visibility on the main thread
                    withContext(Dispatchers.Main) {
                        if (scans.isNullOrEmpty()) {
                            recentscanstitle.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        } else {
                            recentscanstitle.visibility = View.VISIBLE
                            recyclerView.visibility = View.VISIBLE
                        }
                    }

                    scans.forEachIndexed { index, scan ->
                        Log.d("API_RESPONSE", "[$index] -> id=${scan.id}, name=${scan.scan_name}, image=${scan.image_filename}")
                    }

                } else {
                    Toast.makeText(requireContext(), "Please check your Internet Connection and try again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Please check your Internet Connection and try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
