package com.ingrify.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import kotlinx.coroutines.Dispatchers
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startScanButton: MaterialButton = view.findViewById(R.id.btn_start_scan)
        val backButton: ImageView = view.findViewById(R.id.iv_back_button_login)

//        backButton.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }

        startScanButton.setOnClickListener {
            // Implement your scanning logic here
            // This is where you would typically open the camera,
            // process the image, and extract ingredients.
            Toast.makeText(context, "Processing....!", Toast.LENGTH_SHORT).show()
        }
        val UploadButton: MaterialButton = view.findViewById(R.id.btn_upload_image)

       UploadButton.setOnClickListener {
            // Implement your scanning logic here
            // This is where you would typically open the camera,
            // process the image, and extract ingredients.
            Toast.makeText(context, "Gallery Opening broooo!", Toast.LENGTH_SHORT).show()
        }
    }
}