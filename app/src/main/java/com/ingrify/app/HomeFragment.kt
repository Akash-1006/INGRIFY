package com.ingrify.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.ingrify.app.UserSessionManager
import com.ingrify.app.RetrofitClient
import okio.IOException
import retrofit2.HttpException

class HomeFragment : Fragment() {

    private lateinit var welcomeMessageTextView: TextView
    private lateinit var recentscanstitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var scanAdapter: ScanAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var hamburger: ImageView

    private val scanItems = mutableListOf<ScanItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
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

        drawerLayout = view.findViewById(R.id.drawer_layout_home)
        navigationView = view.findViewById(R.id.navigation_view_home)
        hamburger = view.findViewById(R.id.Hamburger_menu)

        // Open drawer when hamburger clicked
        hamburger.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // Handle nav item clicks
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                }
                R.id.nav_scan -> {
                    replaceFragment(ScanFragment())
                }
                R.id.nav_search -> {
                    replaceFragment(SearchFragment())
                }
                R.id.nav_allergen -> {
                    replaceFragment(AllergenFragment())
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment())
                }
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        return view

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        welcomeMessageTextView = view.findViewById(R.id.tv_welcome_message)

        recentscanstitle=view.findViewById(R.id.recent_scans_title)

        fetchUserProfile()
        fetchScanHistory()


        // Setup click listeners for the MaterialCardView elements for fragment navigation
        view.findViewById<MaterialCardView>(R.id.card_scan).setOnClickListener {
            // Create an instance of the ScanFragment
            val scanFragment = ScanFragment()
            (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))
                .selectedItemId = R.id.navigation_scan
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, scanFragment)
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<MaterialCardView>(R.id.card_search).setOnClickListener {
            val searchFragment = SearchFragment()
            (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))
                .selectedItemId = R.id.navigation_search
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<MaterialCardView>(R.id.card_allergen).setOnClickListener {
            val allergenFragment = AllergenFragment()
            (requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation))
                .selectedItemId = R.id.navigation_profile
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, allergenFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun fetchUserProfile() {
        val authToken = UserSessionManager.getAuthToken()
        recentscanstitle.visibility = View.GONE
        recyclerView.visibility = View.GONE
        if (authToken.isNullOrEmpty()) {
            welcomeMessageTextView.text = "Hello, User!!"
            context?.let {
                Toast.makeText(
                    it,
                    "Please log in to personalize your experience.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            val user = UserSessionManager.getName()
            welcomeMessageTextView.text = "Hello, $user!!"
        }
    }
    private fun fetchScanHistory() {
        val token = UserSessionManager.getAuthToken()

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
    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}
