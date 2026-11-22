package com.redro.pdfconverter

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.FileOutputStream

class PdfAnalyzer(context: Context) {
    private val appContext = context.applicationContext

    init {
        PDFBoxResourceLoader.init(appContext)
    }

    fun analyze(uri: Uri): PdfAnalysisResult {
        val source = File.createTempFile("redro-source-", ".pdf", appContext.cacheDir)

        try {
            val input = appContext.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Unable to read selected PDF")

            input.use { inputStream ->
                FileOutputStream(source).use { output ->
                    inputStream.copyTo(output)
                }
            }

            PDDocument.load(source).use { document ->
                val pages = ArrayList<PdfPageAnalysis>(document.numberOfPages)

                for (index in 0 until document.numberOfPages) {
                    val page = document.getPage(index)
                    val stripper = PDFTextStripper().apply {
                        startPage = index + 1
                        endPage = index + 1
                        sortByPosition = true
                    }
                    val text = stripper.getText(document).trimEnd()

                    pages += PdfPageAnalysis(
                        pageNumber = index + 1,
                        widthPoints = page.mediaBox.width,
                        heightPoints = page.mediaBox.height,
                        text = text,
                        contentType = PdfPageClassifier.classify(text)
                    )
                }

                val title = document.documentInformation.title
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }

                return PdfAnalysisResult(
                    title = title,
                    pageCount = document.numberOfPages,
                    pages = pages
                )
            }
        } finally {
            source.delete()
        }
    }
}
