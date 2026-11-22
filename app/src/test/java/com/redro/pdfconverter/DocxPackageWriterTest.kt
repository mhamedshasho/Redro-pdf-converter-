package com.redro.pdfconverter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

class DocxPackageWriterTest {
    @Test
    fun escapesXmlSpecialCharacters() {
        assertEquals(
            "&lt;tag&gt; &amp; &quot;quote&quot; &apos;apostrophe&apos;",
            DocxPackageWriter.escapeXml("<tag> & \"quote\" 'apostrophe'")
        )
    }

    @Test
    fun detectsArabicCharacters() {
        assertTrue(DocxPackageWriter.containsArabic("مرحبا"))
    }

    @Test
    fun createsValidDocxPackageWithArabicAndPageBreak() {
        val output = File.createTempFile("redro-test-", ".docx")
        try {
            val analysis = PdfAnalysisResult(
                title = "Test",
                pageCount = 2,
                pages = listOf(
                    PdfPageAnalysis(1, 595f, 842f, "Hello & welcome\nمرحبا", PdfPageContentType.TEXT_BEARING),
                    PdfPageAnalysis(2, 595f, 842f, "Second page", PdfPageContentType.TEXT_BEARING)
                )
            )

            DocxPackageWriter.writeEditable(analysis, output)
            DocxPackageValidator.validate(output)

            ZipFile(output).use { zip ->
                assertTrue(zip.getEntry("[Content_Types].xml") != null)
                assertTrue(zip.getEntry("_rels/.rels") != null)
                assertTrue(zip.getEntry("word/document.xml") != null)

                val xml = zip.getInputStream(zip.getEntry("word/document.xml"))
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }

                assertTrue(xml.contains("Hello &amp; welcome"))
                assertTrue(xml.contains("مرحبا"))
                assertTrue(xml.contains("<w:bidi/>"))
                assertTrue(xml.contains("w:type=\"page\""))

                val factory = DocumentBuilderFactory.newInstance()
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                factory.newDocumentBuilder().parse(
                    zip.getInputStream(zip.getEntry("word/document.xml"))
                )
            }
        } finally {
            output.delete()
        }
    }
}
