package com.redro.pdfconverter

enum class PdfPageContentType {
    TEXT_BEARING,
    SCANNED_OR_EMPTY,
    MIXED_CONTENT
}

data class PdfPageAnalysis(
    val pageNumber: Int,
    val widthPoints: Float,
    val heightPoints: Float,
    val text: String,
    val contentType: PdfPageContentType,
    val hasImages: Boolean = false
)

data class PdfAnalysisResult(
    val title: String?,
    val pageCount: Int,
    val pages: List<PdfPageAnalysis>
)

object PdfPageClassifier {
    fun classify(extractedText: String, hasImages: Boolean = false): PdfPageContentType =
        when {
            extractedText.any { !it.isWhitespace() } && hasImages -> PdfPageContentType.MIXED_CONTENT
            extractedText.any { !it.isWhitespace() } -> PdfPageContentType.TEXT_BEARING
            else -> PdfPageContentType.SCANNED_OR_EMPTY
        }
}
