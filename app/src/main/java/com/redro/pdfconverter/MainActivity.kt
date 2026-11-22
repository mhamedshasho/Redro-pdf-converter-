package com.redro.pdfconverter

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var statusView: TextView
    private lateinit var chooseButton: Button
    private lateinit var exportButton: Button
    private var latestAnalysis: PdfAnalysisResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 40, 28, 24)
            setBackgroundColor(0xFF171717.toInt())
        }

        val title = TextView(this).apply {
            text = getString(R.string.screen_title)
            textSize = 26f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(0xFFFFFFFF.toInt())
        }

        val subtitle = TextView(this).apply {
            text = getString(R.string.screen_subtitle)
            textSize = 14f
            setTextColor(0xFFBDBDBD.toInt())
            gravity = Gravity.CENTER
        }

        chooseButton = Button(this).apply {
            text = getString(R.string.choose_pdf)
            setOnClickListener { openPdfPicker() }
        }

        exportButton = Button(this).apply {
            text = getString(R.string.export_editable_docx)
            visibility = View.GONE
            setOnClickListener { createDocx() }
        }

        statusView = TextView(this).apply {
            text = getString(R.string.waiting_for_file)
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(4, 12, 4, 12)
        }

        val scroll = ScrollView(this).apply {
            addView(statusView)
        }

        root.addView(title, matchWrap())
        root.addView(subtitle, margins(matchWrap(), 12, 8, 12, 24))
        root.addView(chooseButton, margins(matchWrap(), 0, 8, 0, 8))
        root.addView(exportButton, margins(matchWrap(), 0, 8, 0, 16))
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        setContentView(root)
    }

    private fun openPdfPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(intent, REQUEST_OPEN_PDF)
    }

    private fun createDocx() {
        if (latestAnalysis == null) return
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = DOCX_MIME
            putExtra(Intent.EXTRA_TITLE, "redro-converted.docx")
        }
        startActivityForResult(intent, REQUEST_CREATE_DOCX)
    }

    @Deprecated("Deprecated by Android, retained for compatibility with the minimum SDK")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CREATE_DOCX) {
            if (resultCode != RESULT_OK || data?.data == null) {
                statusView.text = getString(R.string.export_cancelled)
                return
            }
            saveDocx(data.data!!)
            return
        }

        if (requestCode != REQUEST_OPEN_PDF) return
        if (resultCode != RESULT_OK) {
            statusView.text = getString(R.string.no_file_selected)
            return
        }

        val uri: Uri = data?.data ?: run {
            statusView.text = getString(R.string.open_failed)
            return
        }

        analyzePdf(uri)
    }

    private fun analyzePdf(uri: Uri) {
        chooseButton.isEnabled = false
        exportButton.visibility = View.GONE
        latestAnalysis = null
        statusView.text = getString(R.string.analysis_running)

        Thread {
            try {
                val result = PdfAnalyzer(applicationContext).analyze(uri)
                runOnUiThread {
                    if (isFinishing || isDestroyed) return@runOnUiThread
                    latestAnalysis = result
                    chooseButton.isEnabled = true
                    exportButton.visibility = View.VISIBLE
                    val output = StringBuilder()
                    output.append(getString(R.string.analysis_complete, result.pageCount))
                    output.append("\n\n")
                    result.title?.let {
                        output.append(getString(R.string.analysis_title, it))
                        output.append("\n\n")
                    }

                    result.pages.forEach { page ->
                        val typeLabel = when (page.contentType) {
                            PdfPageContentType.TEXT_BEARING ->
                                getString(R.string.analysis_text_bearing)
                            PdfPageContentType.SCANNED_OR_EMPTY ->
                                getString(R.string.analysis_scanned)
                        }
                        output.append(
                            getString(
                                R.string.analysis_page_text,
                                page.pageNumber,
                                typeLabel,
                                page.widthPoints,
                                page.heightPoints
                            )
                        )
                        output.append("\n")
                    }

                    output.append("\n")
                    output.append(getString(R.string.analysis_preview))
                    output.append("\n\n")
                    val preview = result.pages.firstOrNull()?.text.orEmpty()
                        .take(1800)
                    output.append(
                        preview.ifBlank { getString(R.string.analysis_no_text) }
                    )
                    statusView.text = output.toString()
                }
            } catch (_: Exception) {
                runOnUiThread {
                    if (isFinishing || isDestroyed) return@runOnUiThread
                    chooseButton.isEnabled = true
                    statusView.text = getString(R.string.open_failed)
                }
            }
        }.start()
    }

    private fun saveDocx(uri: Uri) {
        val analysis = latestAnalysis ?: return
        exportButton.isEnabled = false
        statusView.text = getString(R.string.export_running)

        Thread {
            try {
                val output = contentResolver.openOutputStream(uri)
                    ?: throw IllegalStateException("Unable to create output document")
                output.use { stream ->
                    DocxPackageWriter.writeEditable(analysis, stream)
                }
                runOnUiThread {
                    if (isFinishing || isDestroyed) return@runOnUiThread
                    exportButton.isEnabled = true
                    statusView.append("\n\n")
                    statusView.append(getString(R.string.export_complete))
                }
            } catch (_: Exception) {
                runOnUiThread {
                    if (isFinishing || isDestroyed) return@runOnUiThread
                    exportButton.isEnabled = true
                    statusView.append("\n\n")
                    statusView.append(getString(R.string.export_failed))
                }
            }
        }.start()
    }

    private fun matchWrap(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    private fun margins(
        params: LinearLayout.LayoutParams,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): LinearLayout.LayoutParams {
        params.setMargins(left, top, right, bottom)
        return params
    }

    companion object {
        private const val REQUEST_OPEN_PDF = 4101
        private const val REQUEST_CREATE_DOCX = 4102
        private const val DOCX_MIME = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }
}
