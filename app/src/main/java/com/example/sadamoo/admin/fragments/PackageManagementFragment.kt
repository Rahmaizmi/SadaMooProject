package com.example.sadamoo.admin.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.R
import com.example.sadamoo.admin.adapters.PackageAdapter
import com.example.sadamoo.admin.models.PackageModel
import com.example.sadamoo.admin.dialogs.AddEditPackageDialog
import com.example.sadamoo.admin.dialogs.PackageDetailDialog
import com.example.sadamoo.databinding.FragmentPackageManagementBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*

class PackageManagementFragment : Fragment() {
    private var _binding: FragmentPackageManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var packageAdapter: PackageAdapter
    private var allPackages = mutableListOf<PackageModel>()
    private var filteredPackages = mutableListOf<PackageModel>()
    private var currentFilter = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPackageManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupButtons()
        loadPackages()
    }

    private fun setupRecyclerView() {
        packageAdapter = PackageAdapter(
            packages = filteredPackages,
            onPackageClick = { packageModel -> showPackageDetail(packageModel) },
            onEditPackage = { packageModel -> editPackage(packageModel) },
            onToggleStatus = { packageModel -> togglePackageStatus(packageModel) },
            onDeletePackage = { packageModel -> deletePackage(packageModel) }
        )

        binding.rvPackages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = packageAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase().trim()
                filterPackages(query, currentFilter)
            }
        })
    }

    private fun setupFilters() {
        setActiveFilter("all")

        binding.btnFilterAll.setOnClickListener {
            setActiveFilter("all")
            filterPackages(binding.etSearch.text.toString(), "all")
        }

        binding.btnFilterActive.setOnClickListener {
            setActiveFilter("active")
            filterPackages(binding.etSearch.text.toString(), "active")
        }

        binding.btnFilterInactive.setOnClickListener {
            setActiveFilter("inactive")
            filterPackages(binding.etSearch.text.toString(), "inactive")
        }
    }

    private fun setActiveFilter(filter: String) {
        currentFilter = filter

        // Reset all buttons
        listOf(
            binding.btnFilterAll,
            binding.btnFilterActive,
            binding.btnFilterInactive
        ).forEach {
            it.background = requireContext().getDrawable(R.drawable.filter_button_inactive)
            it.setTextColor(requireContext().getColor(R.color.primary))
        }

        // Set active button
        val activeButton = when (filter) {
            "all" -> binding.btnFilterAll
            "active" -> binding.btnFilterActive
            "inactive" -> binding.btnFilterInactive
            else -> binding.btnFilterAll
        }

        activeButton.background = requireContext().getDrawable(R.drawable.filter_button_active)
        activeButton.setTextColor(requireContext().getColor(android.R.color.white))
    }



    private fun setupButtons() {
        binding.btnAddPackage.setOnClickListener {
            addNewPackage()
        }
    }

    private fun loadPackages() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                // Load packages from Firebase
                val packagesSnapshot = firestore.collection("subscription_packages")
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                allPackages.clear()

                if (packagesSnapshot.isEmpty) {
                    // Create default packages if none exist
                    createDefaultPackages()
                } else {
                    // Load existing packages
                    for (document in packagesSnapshot.documents) {
                        val packageModel = PackageModel(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            description = document.getString("description") ?: "",
                            price = document.getLong("price")?.toInt() ?: 0,
                            duration = document.getLong("duration")?.toInt() ?: 1,
                            durationType = document.getString("durationType") ?: "month",
                            features = document.get("features") as? List<String> ?: emptyList(),
                            isActive = document.getBoolean("isActive") ?: true,
                            createdAt = document.getTimestamp("createdAt")?.toDate() ?: Date(),
                            updatedAt = document.getTimestamp("updatedAt")?.toDate() ?: Date(),
                            subscriberCount = 0 // Will be calculated separately
                        )
                        allPackages.add(packageModel)
                    }
                }

                // Load subscriber counts
                loadSubscriberCounts()

                filteredPackages.clear()
                filteredPackages.addAll(allPackages)
                packageAdapter.notifyDataSetChanged()

                updatePackageCount()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading packages: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private suspend fun createDefaultPackages() {
        val defaultPackages = listOf(
            PackageModel(
                id = "",
                name = "Basic",
                description = "Perfect for small farms with basic needs",
                price = 25000,
                duration = 1,
                durationType = "month",
                features = listOf(
                    "10 scans per month",
                    "Basic disease detection",
                    "Email support",
                    "Basic reports"
                ),
                isActive = true,
                createdAt = Date(),
                updatedAt = Date(),
                subscriberCount = 0
            ),
            PackageModel(
                id = "",
                name = "Standard",
                description = "Great for medium farms with regular monitoring",
                price = 65000,
                duration = 3,
                durationType = "month",
                features = listOf(
                    "50 scans per month",
                    "Advanced disease detection",
                    "Priority email support",
                    "Detailed reports",
                    "Basic consultation"
                ),
                isActive = true,
                createdAt = Date(),
                updatedAt = Date(),
                subscriberCount = 0
            ),
            PackageModel(
                id = "",
                name = "Premium",
                description = "Best for large farms with comprehensive needs",
                price = 120000,
                duration = 6,
                durationType = "month",
                features = listOf(
                    "Unlimited scans",
                    "AI-powered detection",
                    "24/7 chat support",
                    "Advanced analytics",
                    "Unlimited consultation",
                    "Custom reports"
                ),
                isActive = true,
                createdAt = Date(),
                updatedAt = Date(),
                subscriberCount = 0
            ),
            PackageModel(
                id = "",
                name = "Ultimate",
                description = "Enterprise solution with all premium features",
                price = 200000,
                duration = 12,
                durationType = "month",
                features = listOf(
                    "Everything in Premium",
                    "Dedicated account manager",
                    "Custom integrations",
                    "API access",
                    "White-label options",
                    "Priority feature requests"
                ),
                isActive = true,
                createdAt = Date(),
                updatedAt = Date(),
                subscriberCount = 0
            )
        )

        for (packageModel in defaultPackages) {
            val packageData = hashMapOf(
                "name" to packageModel.name,
                "description" to packageModel.description,
                "price" to packageModel.price,
                "duration" to packageModel.duration,
                "durationType" to packageModel.durationType,
                "features" to packageModel.features,
                "isActive" to packageModel.isActive,
                "createdAt" to com.google.firebase.Timestamp(packageModel.createdAt),
                "updatedAt" to com.google.firebase.Timestamp(packageModel.updatedAt)
            )

            val docRef = firestore.collection("subscription_packages").add(packageData).await()
            packageModel.id = docRef.id
            allPackages.add(packageModel)
        }
    }

    private suspend fun loadSubscriberCounts() {
        for (packageModel in allPackages) {
            try {
                val subscribersSnapshot = firestore.collection("users")
                    .whereEqualTo("subscriptionType", packageModel.name)
                    .whereEqualTo("subscriptionStatus", "active")
                    .get()
                    .await()

                packageModel.subscriberCount = subscribersSnapshot.size()
            } catch (e: Exception) {
                packageModel.subscriberCount = 0
            }
        }
    }

    private fun filterPackages(query: String, filter: String) {
        var filtered = allPackages.toList()

        // Filter by status
        when (filter) {
            "active" -> filtered = filtered.filter { it.isActive }
            "inactive" -> filtered = filtered.filter { !it.isActive }
        }

        // Filter by search query
        if (query.isNotEmpty()) {
            filtered = filtered.filter { packageModel ->
                packageModel.name.lowercase().contains(query) ||
                        packageModel.description.lowercase().contains(query)
            }
        }

        filteredPackages.clear()
        filteredPackages.addAll(filtered)
        packageAdapter.notifyDataSetChanged()

        // Show/hide empty state
        if (filteredPackages.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvPackages.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvPackages.visibility = View.VISIBLE
        }


        updatePackageCount()
    }

    private fun updatePackageCount() {
        binding.tvPackageCount.text = "${filteredPackages.size} packages found"
    }

    private fun addNewPackage() {
        val dialog = AddEditPackageDialog.newInstance(null) { packageModel ->
            savePackage(packageModel)
        }
        dialog.show(parentFragmentManager, "AddPackageDialog")
    }

    private fun editPackage(packageModel: PackageModel) {
        val dialog = AddEditPackageDialog.newInstance(packageModel) { updatedPackage ->
            updatePackage(updatedPackage)
        }
        dialog.show(parentFragmentManager, "EditPackageDialog")
    }

    private fun savePackage(packageModel: PackageModel) {
        lifecycleScope.launch {
            try {
                val packageData = hashMapOf(
                    "name" to packageModel.name,
                    "description" to packageModel.description,
                    "price" to packageModel.price,
                    "duration" to packageModel.duration,
                    "durationType" to packageModel.durationType,
                    "features" to packageModel.features,
                    "isActive" to packageModel.isActive,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                firestore.collection("subscription_packages")
                    .add(packageData)
                    .await()

                Toast.makeText(requireContext(), "Package created successfully!", Toast.LENGTH_SHORT).show()
                loadPackages() // Refresh data

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error creating package: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePackage(packageModel: PackageModel) {
        lifecycleScope.launch {
            try {
                val packageData = hashMapOf(
                    "name" to packageModel.name,
                    "description" to packageModel.description,
                    "price" to packageModel.price,
                    "duration" to packageModel.duration,
                    "durationType" to packageModel.durationType,
                    "features" to packageModel.features,
                    "isActive" to packageModel.isActive,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )

                firestore.collection("subscription_packages").document(packageModel.id)
                    .update(packageData as Map<String, Any>)
                    .await()

                Toast.makeText(requireContext(), "Package updated successfully!", Toast.LENGTH_SHORT).show()
                loadPackages() // Refresh data

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating package: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun togglePackageStatus(packageModel: PackageModel) {
        lifecycleScope.launch {
            try {
                val newStatus = !packageModel.isActive

                firestore.collection("subscription_packages").document(packageModel.id)
                    .update("isActive", newStatus, "updatedAt", com.google.firebase.Timestamp.now())
                    .await()

                val statusText = if (newStatus) "activated" else "deactivated"
                Toast.makeText(requireContext(), "Package ${packageModel.name} has been $statusText", Toast.LENGTH_SHORT).show()

                loadPackages() // Refresh data

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating package status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePackage(packageModel: PackageModel) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Package")
            .setMessage("Are you sure you want to delete '${packageModel.name}' package?\n\nThis action cannot be undone and will affect ${packageModel.subscriberCount} active subscribers.")
            .setPositiveButton("Delete") { _, _ ->
                performDeletePackage(packageModel)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDeletePackage(packageModel: PackageModel) {
        lifecycleScope.launch {
            try {
                firestore.collection("subscription_packages").document(packageModel.id)
                    .delete()
                    .await()

                Toast.makeText(requireContext(), "Package '${packageModel.name}' deleted successfully", Toast.LENGTH_SHORT).show()
                loadPackages() // Refresh data

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error deleting package: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPackageDetail(packageModel: PackageModel) {
        val dialog = PackageDetailDialog.newInstance(packageModel)
        dialog.show(parentFragmentManager, "PackageDetailDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
