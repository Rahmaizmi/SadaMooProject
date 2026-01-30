package com.example.sadamoo.users.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.sadamoo.R
import com.example.sadamoo.databinding.DialogUpgradeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.NumberFormat
import java.util.*

class UpgradeDialogFragment : DialogFragment() {
    private lateinit var binding: DialogUpgradeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogUpgradeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPackageButtons()
        setupCloseButton()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupPackageButtons() {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("subscription_packages")
            .whereEqualTo("isActive", true)
            .orderBy("price", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val container = binding.packageButtonContainer
                container.removeAllViews()

                if (snapshot.isEmpty) {
                    Toast.makeText(requireContext(), "Tidak ada paket aktif", Toast.LENGTH_SHORT)
                        .show()
                    return@addOnSuccessListener
                }

                val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

                for (doc in snapshot.documents) {
                    val name = doc.getString("name") ?: "Tanpa Nama"
                    val price = doc.getLong("price") ?: 0
                    val duration = doc.getLong("duration") ?: 1
                    val durationType = doc.getString("durationType") ?: "bulan"

                    val formattedPrice =
                        formatter.format(price).replace("IDR", "Rp").replace(",00", "")

                    val button = Button(requireContext()).apply {
                        text = "$name - $formattedPrice / $duration $durationType"
                        setBackgroundResource(R.drawable.button_primary)
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                        textSize = 14f
                        setPadding(24, 24, 24, 24)
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 12, 0, 12)
                        }

                        setOnClickListener {
                            selectPackage(name, price.toInt(), duration.toInt())
                        }
                    }
                    container.addView(button)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Gagal memuat paket: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

                Log.e("UpgradeDialogFragment", e.message.toString())
            }
    }

    private fun selectPackage(packageName: String, price: Int, duration: Int) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val formattedPrice = formatter.format(price).replace("IDR", "Rp").replace(",00", "")

        Toast.makeText(
            requireContext(),
            "Paket $packageName dipilih - $formattedPrice",
            Toast.LENGTH_SHORT
        ).show()

        val paymentDialog = PaymentDialogFragment.newInstance(packageName, price, duration)
        paymentDialog.show(parentFragmentManager, "PaymentDialog")
        dismiss()
    }

    private fun setupCloseButton() {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.btnLater.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Anda dapat upgrade kapan saja di menu Profil",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
}
