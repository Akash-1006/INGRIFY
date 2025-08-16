package com.ingrify.app

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat

class PermissionsFragment : Fragment() {

    private lateinit var cameraToggle: SwitchCompat
    private lateinit var storageToggle: SwitchCompat

    private val CAMERA_REQUEST_CODE = 2001
    private val STORAGE_REQUEST_CODE = 2002

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_permissions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton: ImageView = view.findViewById(R.id.iv_back_button_permission)
        cameraToggle = view.findViewById(R.id.cameraswitch)
        storageToggle = view.findViewById(R.id.StorageSwitch)

        val sharedPreferences = requireContext().getSharedPreferences("Permissions", MODE_PRIVATE)
        val isCameraON = sharedPreferences.getBoolean("Camera", false)
        val isStorageON = sharedPreferences.getBoolean("Storage", false)

        // Back button
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Load saved states
        cameraToggle.isChecked = isCameraON
        storageToggle.isChecked = isStorageON

        // Camera toggle
        cameraToggle.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Camera", isChecked).apply()

            if (isChecked) {
                // Request camera permission if not granted
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
                } else {
                    Toast.makeText(requireContext(), "Camera permission already granted", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Camera disabled (revoke from settings to fully block)", Toast.LENGTH_LONG).show()
            }
        }

        // Storage toggle
        storageToggle.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("Storage", isChecked).apply()

            if (isChecked) {
                // Request storage permission if not granted
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
                } else {
                    Toast.makeText(requireContext(), "Storage permission already granted", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Storage disabled (revoke from settings to fully block)", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Handle permission results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Camera permission granted", Toast.LENGTH_SHORT).show()
                    cameraToggle.isChecked = true
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                    cameraToggle.isChecked = false
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Storage permission granted", Toast.LENGTH_SHORT).show()
                    storageToggle.isChecked = true
                } else {
                    Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
                    storageToggle.isChecked = false
                }
            }
        }
    }

}
