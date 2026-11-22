package com.redro.pdfconverter

enum class PdfPageContentType {
    TEXT_BEARING,
    SCANNED_OR_EMPTY
}

data class PdfPageAnalysis(
    val pageNumber: Int,
    val widthPoints: Float,
    val heightPoints: Float,
    val text: String,
    val contentType: PdfPageContentType
)

data class PdfAnalysisResult(
    val title: String?,
    val pageCount: Int,
    val pages: List<PdfPageAnalysis>
)

object PdfPageClassifier {
    fun classify(extractedText: String): PdfPageContentType =
        if (extractedText.any { !it.isWhitespace() }) {
            PdfPageContentType.TEXT_BEARING
        } else {
            PdfPageContentType.SCANNED_OR_EMPTY
        }
}
