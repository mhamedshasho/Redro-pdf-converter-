package com.redro.pdfconverter

import org.junit.Assert.assertEquals
import org.junit.Test

class PdfPageClassifierTest {
    @Test
    fun classifiesExtractableText() {
        assertEquals(
            PdfPageContentType.TEXT_BEARING,
            PdfPageClassifier.classify("Redro PDF Converter")
        )
    }

    @Test
    fun classifiesArabicText() {
        assertEquals(
            PdfPageContentType.TEXT_BEARING,
            PdfPageClassifier.classify("مرحبا بالعالم")
        )
    }

    @Test
    fun classifiesWhitespaceAsScannedOrEmpty() {
        assertEquals(
            PdfPageContentType.SCANNED_OR_EMPTY,
            PdfPageClassifier.classify(" \n\t ")
        )
    }

    @Test
    fun classifiesEmptyTextAsScannedOrEmpty() {
        assertEquals(
            PdfPageContentType.SCANNED_OR_EMPTY,
            PdfPageClassifier.classify("")
        )
    }
}
