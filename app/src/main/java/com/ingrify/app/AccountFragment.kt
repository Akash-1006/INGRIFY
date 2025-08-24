package com.ingrify.app

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ingrify.app.UserSessionManager.getAuthToken
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AccountFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvAge: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        val backButton: ImageView = view.findViewById(R.id.iv_back_button_account)
        tvName = view.findViewById(R.id.et_name)
        tvUsername = view.findViewById(R.id.username_hint)
        tvAge = view.findViewById(R.id.et_age)
        backButton.setOnClickListener {
            backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        fetchUserProfile()

        return view
    }

    private fun fetchUserProfile() {
        val token = getAuthToken()
        if (token == null) {
            Toast.makeText(requireContext(), "Please log in to view account details", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getUserProfile("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    tvName.text = "${user.name}"
                    tvUsername.text = "${user.username}"
                    tvAge.text = "${user.age ?: "Not set"}"
                } else {
                    Toast.makeText(requireContext(), "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                Toast.makeText(requireContext(), "Unauthorized! Please log in again.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
