package com.ingrify.app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangepasswordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_changepassword, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton: ImageView = view.findViewById(R.id.iv_back_button_changepassword)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val currentPasswordEt: TextInputEditText = view.findViewById(R.id.et_name)
        val newPasswordEt: TextInputEditText = view.findViewById(R.id.username_hint)
        val confirmPasswordEt: TextInputEditText = view.findViewById(R.id.et_age)

        val saveButton: MaterialButton = view.findViewById(R.id.btn_save)
        val cancelButton: MaterialButton = view.findViewById(R.id.btn_cancel)

        saveButton.setOnClickListener {
            val currentPassword = currentPasswordEt.text.toString()
            val newPassword = newPasswordEt.text.toString()
            val confirmPassword = confirmPasswordEt.text.toString()

            // Validation
            when {
                currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                }
                newPassword.length < 6 -> {
                    Toast.makeText(requireContext(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                }
                newPassword != confirmPassword -> {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    changePassword(currentPassword, newPassword)
                }
            }
        }

        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        lifecycleScope.launch {
            try {
                val token = UserSessionManager.getAuthToken()
                if (token.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "You must be logged in to change your password", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val request = ChangePasswordRequest(currentPassword, newPassword)
                val response = RetrofitClient.apiService.changePassword("Bearer $token", request)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Password changed successfully!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = errorBody ?: "Failed to change password"
                    Log.d("ChangePassword", errorMsg)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


}
