package com.example.sadamoo.users

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sadamoo.databinding.ActivityHelpSupportBinding

class HelpSupportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        loadFAQData()
    }

    private fun setupClickListeners() {
        // tombol back - perbaiki dengan onBackPressed()
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // tombol telepon support
        binding.btnContactSupport.setOnClickListener {
            contactSupport()
        }

        // tombol email support
        binding.btnEmailSupport.setOnClickListener {
            sendEmail()
        }

        // tombol WhatsApp support
        binding.btnWhatsappSupport.setOnClickListener {
            openWhatsApp()
        }

        // tombol user guide - perbaiki pesan
        binding.btnUserGuide.setOnClickListener {
            Toast.makeText(this, "Panduan Pengguna akan segera tersedia. Gunakan FAQ di bawah untuk bantuan sementara.", Toast.LENGTH_LONG).show()
        }

        // tombol report bug
        binding.btnReportBug.setOnClickListener {
            reportBug()
        }
    }

    private fun loadFAQData() {
        val faqData = """
        **Pertanyaan yang Sering Diajukan (FAQ)**
        
        **Q: Bagaimana cara menggunakan fitur scan?**
        A: Klik tombol scan di tengah, arahkan kamera ke sapi, dan tekan tombol capture. Pastikan pencahayaan cukup baik.
        
        **Q: Apakah hasil scan akurat?**
        A: Aplikasi menggunakan AI dengan tingkat akurasi 85-95%, namun tetap disarankan konsultasi dengan dokter hewan untuk diagnosis yang lebih tepat.
        
        **Q: Bagaimana cara upgrade ke premium?**
        A: Buka menu Profil > Upgrade ke Premium, pilih paket yang sesuai dengan kebutuhan Anda.
        
        **Q: Apa saja fitur premium?**
        A: Scan unlimited, konsultasi dokter hewan 24/7, riwayat lengkap dengan backup cloud, dan prioritas support.
        
        **Q: Bagaimana cara menghubungi support?**
        A: Gunakan tombol "Hubungi Support" di atas atau email ke support@sadamoo.com
        
        **Q: Aplikasi sering error, bagaimana solusinya?**
        A: Coba restart aplikasi, pastikan menggunakan versi terbaru, dan laporkan masalah melalui tombol "Laporkan Bug".
        """.trimIndent()

        binding.tvFaqContent.text = faqData
    }

    private fun contactSupport() {
        val phoneNumber = "+6281234567890"
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }

        // Perbaiki: tambah pengecekan apakah bisa membuka dialer
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Tidak dapat membuka aplikasi telepon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@sadamoo.com")
            putExtra(Intent.EXTRA_SUBJECT, "Bantuan Aplikasi Sada Moo")
            // Perbaiki: tambah informasi device untuk memudahkan support
            putExtra(Intent.EXTRA_TEXT, """
                Halo tim support,
                
                Saya membutuhkan bantuan terkait:
                
                [Jelaskan masalah Anda di sini]
                
                Device: ${android.os.Build.MODEL}
                Android: ${android.os.Build.VERSION.RELEASE}
                App Version: 1.0
            """.trimIndent())
        }

        if (intent.resolveActivity(packageManager) != null) {
            // Perbaiki: gunakan chooser untuk pilihan email app
            startActivity(Intent.createChooser(intent, "Pilih aplikasi email"))
        } else {
            Toast.makeText(this, "Tidak ada aplikasi email yang tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWhatsApp() {
        val phoneNumber = "+6281234567890"
        val message = "Halo tim support Sada Moo, saya membutuhkan bantuan terkait aplikasi. Terima kasih!"

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(message)}")
            }

            // Perbaiki: cek apakah WhatsApp tersedia, jika tidak buka di browser
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback ke browser jika WhatsApp tidak terinstall
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneNumber"))
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reportBug() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:bugs@sadamoo.com")
            putExtra(Intent.EXTRA_SUBJECT, "Bug Report - Sada Moo App")
            // Perbaiki: template yang lebih lengkap dan informatif
            putExtra(Intent.EXTRA_TEXT, """
                === BUG REPORT ===
                
                Deskripsi masalah:
                [Jelaskan masalah yang terjadi]
                
                Langkah untuk mereproduksi:
                1. [Langkah pertama]
                2. [Langkah kedua] 
                3. [Langkah ketiga]
                
                Yang diharapkan:
                [Apa yang seharusnya terjadi]
                
                Yang terjadi:
                [Apa yang benar-benar terjadi]
                
                ---
                Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
                Android Version: ${android.os.Build.VERSION.RELEASE}
                App Version: 1.0
                Waktu: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())}
                
                Screenshot terlampir (jika ada)
            """.trimIndent())
        }

        if (intent.resolveActivity(packageManager) != null) {
            // Perbaiki: gunakan chooser dan beri feedback ke user
            startActivity(Intent.createChooser(intent, "Pilih aplikasi email"))
            Toast.makeText(this, "Template bug report telah disiapkan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Tidak ada aplikasi email yang tersedia", Toast.LENGTH_SHORT).show()
        }
    }
}
