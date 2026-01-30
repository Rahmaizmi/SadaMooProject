package com.example.sadamoo.admin.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sadamoo.R
import com.example.sadamoo.admin.adapters.UserManagementAdapter
import com.example.sadamoo.admin.dialogs.AddEditUserDialog
import com.example.sadamoo.admin.dialogs.ChangeSubscriptionDialog
import com.example.sadamoo.admin.dialogs.UserDetailDialog
import com.example.sadamoo.admin.models.UserModel
import com.example.sadamoo.databinding.FragmentUserManagementBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class UserManagementFragment : Fragment() {
    private var _binding: FragmentUserManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userAdapter: UserManagementAdapter
    private var allUsers = mutableListOf<UserModel>()
    private var filteredUsers = mutableListOf<UserModel>()
    private var currentFilter = "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupFAB()
        loadUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserManagementAdapter(
            users = filteredUsers,
            onUserClick = { user -> showUserDetail(user) },
            onEditClick = { user -> showAddEditDialog(user) },
            onSubscriptionChange = { user -> changeUserSubscription(user) },
            onBanUser = { user -> banUnbanUser(user) },
            onDeleteClick = { user -> confirmDeleteUser(user) }
        )

        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase().trim()
                filterUsers(query, currentFilter)
            }
        })
    }

    private fun setupFilters() {
        setActiveFilter("all")

        binding.btnFilterAll.setOnClickListener {
            setActiveFilter("all")
            filterUsers(binding.etSearch.text.toString(), "all")
        }

        binding.btnFilterUser.setOnClickListener {
            setActiveFilter("user")
            filterUsers(binding.etSearch.text.toString(), "user")
        }

        binding.btnFilterAdmin.setOnClickListener {
            setActiveFilter("admin")
            filterUsers(binding.etSearch.text.toString(), "admin")
        }

        binding.btnFilterDoctor.setOnClickListener {
            setActiveFilter("doctor")
            filterUsers(binding.etSearch.text.toString(), "doctor")
        }

        binding.btnFilterTrial.setOnClickListener {
            setActiveFilter("trial")
            filterUsers(binding.etSearch.text.toString(), "trial")
        }

        binding.btnFilterPremium.setOnClickListener {
            setActiveFilter("premium")
            filterUsers(binding.etSearch.text.toString(), "premium")
        }

        binding.btnFilterExpired.setOnClickListener {
            setActiveFilter("expired")
            filterUsers(binding.etSearch.text.toString(), "expired")
        }
    }

    private fun setupFAB() {
        binding.fabAddUser.setOnClickListener {
            showAddEditDialog(null) // null = create new user
        }
    }

    private fun setActiveFilter(filter: String) {
        currentFilter = filter

        // Reset semua button ke inactive
        listOf(
            binding.btnFilterAll,
            binding.btnFilterUser,
            binding.btnFilterAdmin,
            binding.btnFilterDoctor,
            binding.btnFilterTrial,
            binding.btnFilterPremium,
            binding.btnFilterExpired
        ).forEach {
            it.background = requireContext().getDrawable(R.drawable.filter_button_inactive)
            it.setTextColor(requireContext().getColor(R.color.primary))
        }

        // Set button yang aktif
        val activeButton = when (filter) {
            "all" -> binding.btnFilterAll
            "user" -> binding.btnFilterUser
            "admin" -> binding.btnFilterAdmin
            "doctor" -> binding.btnFilterDoctor
            "trial" -> binding.btnFilterTrial
            "premium" -> binding.btnFilterPremium
            "expired" -> binding.btnFilterExpired
            else -> binding.btnFilterAll
        }

        activeButton.background = requireContext().getDrawable(R.drawable.filter_button_active)
        activeButton.setTextColor(requireContext().getColor(android.R.color.white))
    }

    private fun loadUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.rvUsers.visibility = View.GONE
                binding.emptyState.visibility = View.GONE

                val usersSnapshot = firestore.collection("users")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                allUsers.clear()

                for (document in usersSnapshot.documents) {
                    val user = UserModel(
                        id = document.id,
                        name = document.getString("name") ?: "Unknown",
                        email = document.getString("email") ?: "No Email",
                        phone = document.getString("phone") ?: "",
                        role = document.getString("role") ?: "user",
                        subscriptionStatus = document.getString("subscriptionStatus") ?: "trial",
                        subscriptionType = document.getString("subscriptionType"),
                        createdAt = document.getTimestamp("createdAt")?.toDate() ?: Date(),
                        lastActive = document.getTimestamp("lastActive")?.toDate(),
                        trialStartDate = document.getTimestamp("trialStartDate")?.toDate(),
                        subscriptionEndDate = document.getTimestamp("subscriptionEndDate")?.toDate(),
                        isBanned = document.getBoolean("isBanned") ?: false,
                        totalScans = 0,
                        photoBase64 = document.getString("photoBase64")
                    )
                    allUsers.add(user)
                }

                // Load scan counts
                loadUserScanCounts()

                filteredUsers.clear()
                filteredUsers.addAll(allUsers)
                userAdapter.notifyDataSetChanged()

                updateUserCount()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading users: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.rvUsers.visibility = View.VISIBLE
            }
        }
    }

    private suspend fun loadUserScanCounts() {
        for (user in allUsers) {
            try {
                val scanSnapshot = firestore.collection("scan_history")
                    .whereEqualTo("userId", user.id)
                    .get()
                    .await()

                user.totalScans = scanSnapshot.size()
            } catch (e: Exception) {
                user.totalScans = 0
            }
        }
    }

    private fun filterUsers(query: String, filter: String) {
        var filtered = allUsers.toList()

        // Filter by role or subscription status
        when (filter) {
            "user", "admin", "doctor" -> {
                filtered = filtered.filter { it.role == filter }
            }
            "trial" -> filtered = filtered.filter { it.subscriptionStatus == "trial" }
            "premium" -> filtered = filtered.filter { it.subscriptionStatus == "active" }
            "expired" -> filtered = filtered.filter { it.subscriptionStatus == "expired" }
        }

        // Filter by search query
        if (query.isNotEmpty()) {
            filtered = filtered.filter { user ->
                user.name.lowercase().contains(query) ||
                        user.email.lowercase().contains(query) ||
                        user.phone.contains(query)
            }
        }

        filteredUsers.clear()
        filteredUsers.addAll(filtered)
        userAdapter.notifyDataSetChanged()

        // Show/hide empty state
        if (filteredUsers.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvUsers.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvUsers.visibility = View.VISIBLE
        }

        updateUserCount()
    }

    private fun updateUserCount() {
        val count = filteredUsers.size
        val filterText = when (currentFilter) {
            "user" -> "User"
            "admin" -> "Admin"
            "doctor" -> "Dokter"
            "trial" -> "Trial"
            "premium" -> "Premium"
            "expired" -> "Expired"
            else -> "Total"
        }
        binding.tvUserCount.text = "$count $filterText found"
    }

    private fun showUserDetail(user: UserModel) {
        val dialog = UserDetailDialog.newInstance(user)
        dialog.show(parentFragmentManager, "UserDetailDialog")
    }

    private fun showAddEditDialog(user: UserModel?) {
        val dialog = AddEditUserDialog.newInstance(user) {
            loadUsers() // Refresh list after save
        }
        dialog.show(parentFragmentManager, "AddEditUserDialog")
    }

    private fun changeUserSubscription(user: UserModel) {
        val dialog = ChangeSubscriptionDialog.newInstance(user) { updatedUser ->
            updateUserSubscription(updatedUser)
        }
        dialog.show(parentFragmentManager, "ChangeSubscriptionDialog")
    }

    private fun updateUserSubscription(user: UserModel) {
        lifecycleScope.launch {
            try {
                val updates = hashMapOf<String, Any>(
                    "subscriptionStatus" to user.subscriptionStatus,
                    "subscriptionType" to (user.subscriptionType ?: ""),
                    "subscriptionEndDate" to (user.subscriptionEndDate?.let {
                        com.google.firebase.Timestamp(it)
                    } ?: com.google.firebase.Timestamp.now())
                )

                firestore.collection("users").document(user.id)
                    .update(updates)
                    .await()

                Toast.makeText(requireContext(), "Subscription updated successfully", Toast.LENGTH_SHORT).show()
                loadUsers()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun banUnbanUser(user: UserModel) {
        val action = if (user.isBanned) "unban" else "ban"
        val message = "Apakah Anda yakin ingin $action ${user.name}?"

        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi ${action.capitalize()}")
            .setMessage(message)
            .setPositiveButton("Ya") { _, _ ->
                performBanUnban(user)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performBanUnban(user: UserModel) {
        lifecycleScope.launch {
            try {
                val newBanStatus = !user.isBanned

                firestore.collection("users").document(user.id)
                    .update("isBanned", newBanStatus)
                    .await()

                val action = if (newBanStatus) "banned" else "unbanned"
                Toast.makeText(requireContext(), "${user.name} has been $action", Toast.LENGTH_SHORT).show()

                loadUsers()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDeleteUser(user: UserModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pengguna")
            .setMessage("⚠️ Apakah Anda yakin ingin menghapus ${user.name}?\n\nData scan history dan semua data terkait akan ikut terhapus.\n\nTindakan ini tidak dapat dibatalkan!")
            .setPositiveButton("Hapus") { _, _ ->
                performDeleteUser(user)
            }
            .setNegativeButton("Batal", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun performDeleteUser(user: UserModel) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                // Delete user's scan history
                val scanDocs = firestore.collection("scan_history")
                    .whereEqualTo("userId", user.id)
                    .get()
                    .await()

                // Delete all scan history documents
                for (doc in scanDocs.documents) {
                    doc.reference.delete().await()
                }

                // Delete user document
                firestore.collection("users")
                    .document(user.id)
                    .delete()
                    .await()

                Toast.makeText(
                    requireContext(),
                    "✅ ${user.name} berhasil dihapus",
                    Toast.LENGTH_SHORT
                ).show()

                loadUsers()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}