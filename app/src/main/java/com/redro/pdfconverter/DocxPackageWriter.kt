package com.redro.pdfconverter

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DocxPackageWriter {
    fun writeEditable(analysis: PdfAnalysisResult, output: File) {
        require(analysis.pages.isNotEmpty()) { "PDF contains no pages" }
        output.parentFile?.mkdirs()

        ZipOutputStream(BufferedOutputStream(FileOutputStream(output))).use { zip ->
            addEntry(zip, "[Content_Types].xml", contentTypesXml())
            addEntry(zip, "_rels/.rels", packageRelationshipsXml())
            addEntry(zip, "word/document.xml", documentXml(analysis))
        }
    }

    internal fun escapeXml(value: String): String = buildString(value.length) {
        value.forEach { char ->
            append(
                when (char) {
                    '&' -> "&amp;"
                    '<' -> "&lt;"
                    '>' -> "&gt;"
                    '"' -> "&quot;"
                    '\'' -> "&apos;"
                    else -> char.toString()
                }
            )
        }
    }

    internal fun containsArabic(value: String): Boolean =
        value.any { it.code in 0x0600..0x06FF || it.code in 0x0750..0x077F || it.code in 0x08A0..0x08FF }

    private fun documentXml(analysis: PdfAnalysisResult): String {
        val body = buildString {
            analysis.pages.forEachIndexed { pageIndex, page ->
                if (pageIndex > 0) {
                    append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>")
                }

                val lines = page.text.split(Regex("\\r?\\n"))
                if (lines.isEmpty()) {
                    append("<w:p/>")
                } else {
                    lines.forEach { line ->
                        appendParagraph(line)
                    }
                }
            }

            append(
                "<w:sectPr>" +
                    "<w:pgSz w:w=\"12240\" w:h=\"15840\"/>" +
                    "<w:pgMar w:top=\"720\" w:right=\"720\" w:bottom=\"720\" w:left=\"720\" w:header=\"360\" w:footer=\"360\" w:gutter=\"0\"/>" +
                    "</w:sectPr>"
            )
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">" +
            "<w:body>$body</w:body></w:document>"
    }

    private fun StringBuilder.appendParagraph(text: String) {
        val rtl = containsArabic(text)
        append("<w:p>")
        if (rtl) append("<w:pPr><w:bidi/></w:pPr>")
        append("<w:r>")
        if (rtl) append("<w:rPr><w:rtl/></w:rPr>")
        append("<w:t xml:space=\"preserve\">")
        append(escapeXml(text))
        append("</w:t></w:r></w:p>")
    }

    private fun contentTypesXml(): String =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
            "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>" +
            "</Types>"

    private fun packageRelationshipsXml(): String =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>" +
            "</Relationships>"

    private fun addEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }
}
