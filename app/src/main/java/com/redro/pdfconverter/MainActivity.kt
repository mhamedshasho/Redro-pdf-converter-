package com.redro.pdfconverter

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var statusView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 48, 28, 28)
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

        val chooseButton = Button(this).apply {
            text = getString(R.string.choose_pdf)
            setOnClickListener { openPdfPicker() }
        }

        statusView = TextView(this).apply {
            text = getString(R.string.waiting_for_file)
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }

        root.addView(title, matchWrap())
        root.addView(subtitle, margins(matchWrap(), 12, 8, 12, 28))
        root.addView(chooseButton, margins(matchWrap(), 0, 8, 0, 20))
        root.addView(statusView, margins(matchWrap(), 8, 8, 8, 8))
        setContentView(root)
    }

    private fun openPdfPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(intent, REQUEST_OPEN_PDF)
    }

    @Deprecated("Deprecated by Android, retained for compatibility with the minimum SDK")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_OPEN_PDF) return
        if (resultCode != RESULT_OK) {
            statusView.text = getString(R.string.no_file_selected)
            return
        }

        val uri: Uri = data?.data ?: run {
            statusView.text = getString(R.string.open_failed)
            return
        }

        inspectPdf(uri)
    }

    private fun inspectPdf(uri: Uri) {
        statusView.text = getString(R.string.waiting_for_file)

        try {
            val descriptor = contentResolver.openFileDescriptor(uri, "r")
                ?: throw IllegalStateException("Unable to open selected document")

            descriptor.use { fileDescriptor ->
                PdfRenderer(fileDescriptor).use { renderer ->
                    statusView.text = getString(R.string.pdf_selected, renderer.pageCount)
                }
            }
        } catch (_: Exception) {
            statusView.text = getString(R.string.open_failed)
        }
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
    }
}
