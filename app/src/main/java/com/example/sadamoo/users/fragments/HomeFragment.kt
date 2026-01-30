package com.example.sadamoo.users.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.sadamoo.R
import com.example.sadamoo.databinding.FragmentHomeBinding
import com.example.sadamoo.users.SapiPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var currentPage = 0
    private var sapiList: List<Pair<String, Int>> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadUserInfo()
        loadViewPagerSapi()
    }

    private fun loadUserInfo() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val nama = document?.getString("name") ?: "User"
                    binding.tvWelcome.text = "Selamat Datang $nama!"
                }
                .addOnFailureListener {
                    binding.tvWelcome.text = "Selamat Datang!"
                }
        } else {
            binding.tvWelcome.text = "Selamat Datang!"
        }
    }

    private fun loadViewPagerSapi() {
        // isi properti sapiList, bukan local variable
        sapiList = listOf(
            "Sapi Brahman" to R.drawable.sapi_brahmana,
            "Sapi Brangus" to R.drawable.sapi_brangus,
            "Sapi Simental" to R.drawable.sapi_simmental,
            "Sapi Limosin" to R.drawable.sapi_limosin,
            "Sapi Brahman Cross" to R.drawable.sapi_brahman_cross,
            "Sapi Ongole" to R.drawable.sapi_ongole,
            "Sapi Peranakan Ongole" to R.drawable.sapi_po,
            "Sapi Aceh" to R.drawable.sapi_aceh,
            "Sapi Bali" to R.drawable.sapi_bali,
            "Sapi Madura" to R.drawable.sapi_madura
        )

        val adapter = SapiPagerAdapter(sapiList)
        binding.viewPagerSapi.adapter = adapter

        binding.viewPagerSapi.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
            }
        })

        startAutoSlide()
    }

    private fun startAutoSlide() {
        lifecycleScope.launch {
            while (isAdded && sapiList.isNotEmpty()) {
                delay(2000)
                currentPage = (currentPage + 1) % sapiList.size
                binding.viewPagerSapi.setCurrentItem(currentPage, true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
