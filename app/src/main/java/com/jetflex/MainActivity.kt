package com.jetflex

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jetflex.databinding.ActivityMainBinding
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "JetFlexPrefs"
        private const val KEY_IS_ARCHIVE_EXTRACTED = "isArchiveExtracted"
    }

    private lateinit var binding: ActivityMainBinding
    private var progressDialog: Dialog? = null
    private var isArchiveExtracted = false

    private val openDocument =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { extractArchive(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadPrefs()
        updateButtonsState()
        enableEdgeToEdge()
        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        binding.selectArchiveButton.setOnClickListener {
            openDocument.launch(arrayOf("application/gzip"))
        }
        binding.continueButton.setOnClickListener {
            val intent = Intent(this@MainActivity, JetFlexActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateButtonsState() {
        binding.selectArchiveButton.isEnabled = !isArchiveExtracted
        binding.continueButton.isEnabled = isArchiveExtracted
    }

    private fun loadPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isArchiveExtracted = prefs.getBoolean(KEY_IS_ARCHIVE_EXTRACTED, false)
    }

    private fun savePrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_ARCHIVE_EXTRACTED, isArchiveExtracted).apply()
    }

    private fun extractArchive(uri: Uri) {
        progressDialog =
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.wait)
                .setView(R.layout.dialog_progress)
                .setCancelable(false)
                .create()
                .also { it.show() }

        lifecycleScope.launch(Dispatchers.IO) {
            contentResolver.openInputStream(uri).use { inputStream ->
                GzipCompressorInputStream(inputStream).use { gzipInputStream ->
                    TarArchiveInputStream(gzipInputStream).use { tarInputStream ->
                        val outputDir = File(applicationInfo.dataDir, "idea").apply { mkdirs() }
                        var entry = tarInputStream.nextTarEntry

                        while (entry != null) {
                            val outputFile = File(outputDir, entry.name)

                            if (entry.isDirectory) {
                                outputFile.mkdirs()
                            } else {
                                outputFile.parentFile?.mkdirs()
                                BufferedOutputStream(FileOutputStream(outputFile)).use { output ->
                                    tarInputStream.copyTo(output)
                                }
                            }
                            entry = tarInputStream.nextTarEntry
                        }
                    }
                }

                copyAsset("libjnidispatch.so", "idea/idea-IC-243.25659.39/lib/jna/aarch64")
                copyAsset("libpty.so", "idea/idea-IC-243.25659.39/lib/pty4j/linux/aarch64")
                copyAsset("libsqliteij.so", "idea/idea-IC-243.25659.39/lib/native/linux-aarch64")
                copyAsset("pty4j.jar", "idea/idea-IC-243.25659.39/lib")
                copyAsset("e2fsprogs-lib.tar.xz", "idea/idea-IC-243.25659.39")

                withContext(Dispatchers.Main) {
                    isArchiveExtracted = true
                    savePrefs()
                    updateButtonsState()
                    progressDialog?.dismiss()
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle(R.string.success)
                        .setMessage(R.string.success_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
        }
    }

    private fun copyAsset(assetFileName: String, subDir: String) {
        val destinationDir = File(applicationInfo.dataDir, subDir).apply { mkdirs() }

        if (assets.list(assetFileName) != null) {
            assets.list(assetFileName)?.forEach { file ->
                copyAsset("$assetFileName/$file", "$subDir/$file")
            }
        } else {
            if (assetFileName.endsWith(".tar.xz")) {
                extractTarXz(assetFileName, destinationDir)
            } else {
                val destinationFile = File(destinationDir, File(assetFileName).name)
                assets.open(assetFileName).use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }

    private fun extractTarXz(assetFileName: String, destinationDir: File) {
        assets.open(assetFileName).use { inputStream ->
            XZCompressorInputStream(inputStream).use { xzInputStream ->
                TarArchiveInputStream(xzInputStream).use { tarInputStream ->
                    var entry = tarInputStream.nextTarEntry
                    while (entry != null) {
                        val outputFile = File(destinationDir, entry.name)

                        if (entry.isDirectory) {
                            outputFile.mkdirs()
                        } else {
                            outputFile.parentFile?.mkdirs()
                            BufferedOutputStream(FileOutputStream(outputFile)).use { output ->
                                tarInputStream.copyTo(output)
                            }
                        }
                        entry = tarInputStream.nextTarEntry
                    }
                }
            }
        }
    }
}
