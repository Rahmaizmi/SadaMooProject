package com.example.sadamoo.doctor.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sadamoo.databinding.ActivityDoctorHelpBinding

class DoctorHelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupFAQs()
        setupContactButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupFAQs() {
        // FAQ 1
        binding.faq1Header.setOnClickListener {
            toggleFAQ(binding.faq1Content, binding.faq1Arrow)
        }

        // FAQ 2
        binding.faq2Header.setOnClickListener {
            toggleFAQ(binding.faq2Content, binding.faq2Arrow)
        }

        // FAQ 3
        binding.faq3Header.setOnClickListener {
            toggleFAQ(binding.faq3Content, binding.faq3Arrow)
        }

        // FAQ 4
        binding.faq4Header.setOnClickListener {
            toggleFAQ(binding.faq4Content, binding.faq4Arrow)
        }

        // FAQ 5
        binding.faq5Header.setOnClickListener {
            toggleFAQ(binding.faq5Content, binding.faq5Arrow)
        }
    }

    private fun toggleFAQ(content: View, arrow: View) {
        if (content.visibility == View.GONE) {
            content.visibility = View.VISIBLE
            arrow.rotation = 180f
        } else {
            content.visibility = View.GONE
            arrow.rotation = 0f
        }
    }

    private fun setupContactButtons() {
        // Email Support
        binding.btnEmailSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("sada2025moo@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Bantuan Aplikasi Sada Moo - Doctor")
                setPackage("com.google.android.gm") // PAKSA Gmail
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                // Jika Gmail tidak terinstall
                val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:sada2025moo@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Bantuan Aplikasi Sada Moo - Doctor")
                }
                startActivity(fallbackIntent)
            }
        }



        // WhatsApp Support
        binding.btnWhatsappSupport.setOnClickListener {
            val phoneNumber = "6289536013315" // Replace with actual number
            val url = "https://wa.me/$phoneNumber?text=Halo, saya butuh bantuan dengan aplikasi Sada Moo"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}