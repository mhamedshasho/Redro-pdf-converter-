package com.redro.pdfconverter

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DocxPackageWriter {
    fun writeEditable(analysis: PdfAnalysisResult, output: File) {
        output.parentFile?.mkdirs()
        FileOutputStream(output).use { stream ->
            writeEditable(analysis, stream)
        }
    }

    fun writeEditable(analysis: PdfAnalysisResult, output: OutputStream) {
        require(analysis.pages.isNotEmpty()) { "PDF contains no pages" }

        ZipOutputStream(BufferedOutputStream(output)).use { zip ->
            addEntry(zip, "[Content_Types].xml", contentTypesXml())
            addEntry(zip, "_rels/.rels", packageRelationshipsXml())
            addEntry(zip, "word/document.xml", documentXml(analysis))
        }
    }


    fun writeVisual(pages: List<VisualPageImage>, output: OutputStream) {
        require(pages.isNotEmpty()) { "PDF contains no pages" }
        val pageWidth = pages.first().widthPoints.coerceAtLeast(1f)
        val pageHeight = pages.first().heightPoints.coerceAtLeast(1f)
        val maxWidth = (pageWidth * 12_700f).toLong().coerceAtLeast(1L)
        val maxHeight = (pageHeight * 12_700f).toLong().coerceAtLeast(1L)
        ZipOutputStream(BufferedOutputStream(output)).use { zip ->
            addEntry(zip, "[Content_Types].xml", visualContentTypesXml())
            addEntry(zip, "_rels/.rels", packageRelationshipsXml())
            addEntry(zip, "word/document.xml", visualDocumentXml(pages, maxWidth, maxHeight, pageWidth, pageHeight))
            addEntry(zip, "word/_rels/document.xml.rels", visualRelationshipsXml(pages.size))
            pages.forEachIndexed { index, page ->
                zip.putNextEntry(ZipEntry("word/media/page-${index + 1}.png"))
                val imageFile = page.pngFile
                if (imageFile != null) {
                    FileInputStream(imageFile).use { input -> input.copyTo(zip) }
                } else {
                    zip.write(page.png)
                }
                zip.closeEntry()
            }
        }
    }

    private fun visualDocumentXml(
        pages: List<VisualPageImage>,
        maxWidth: Long,
        maxHeight: Long,
        pageWidth: Float,
        pageHeight: Float
    ): String {
        val pageWidthTwips = (pageWidth * 20f).toInt().coerceAtLeast(1)
        val pageHeightTwips = (pageHeight * 20f).toInt().coerceAtLeast(1)
        val body = buildString {
            pages.forEachIndexed { index, page ->
                val scale = minOf(maxWidth.toDouble() / page.widthPoints.coerceAtLeast(1f), maxHeight.toDouble() / page.heightPoints.coerceAtLeast(1f))
                val imageWidth = (page.widthPoints * scale).toLong().coerceAtLeast(1L)
                val imageHeight = (page.heightPoints * scale).toLong().coerceAtLeast(1L)
                if (index > 0) append("<w:p><w:r><w:br w:type=\\"page\\"/></w:r></w:p>")
                append("<w:p><w:pPr><w:jc w:val=\\"center\\"/></w:pPr><w:r><w:drawing>")
                append("<wp:inline distT=\\"0\\" distB=\\"0\\" distL=\\"0\\" distR=\\"0\\">")
                append("<wp:extent cx=\\"$imageWidth\\" cy=\\"$imageHeight\\"/>")
                append("<wp:docPr id=\\"${index + 1}\\" name=\\"PDF page ${page.pageNumber}\\"/>")
                append("<a:graphic xmlns:a=\\"http://schemas.openxmlformats.org/drawingml/2006/main\\">")
                append("<a:graphicData uri=\\"http://schemas.openxmlformats.org/drawingml/2006/picture\\">")
                append("<pic:pic xmlns:pic=\\"http://schemas.openxmlformats.org/drawingml/2006/picture\\">")
                append("<pic:nvPicPr><pic:cNvPr id=\\"0\\" name=\\"page-${page.pageNumber}.png\\"/><pic:cNvPicPr/></pic:nvPicPr>")
                append("<pic:blipFill><a:blip r:embed=\\"rId${index + 1}\\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>")
                append("<pic:spPr><a:xfrm><a:off x=\\"0\\" y=\\"0\\"/><a:ext cx=\\"$imageWidth\\" cy=\\"$imageHeight\\"/></a:xfrm><a:prstGeom prst=\\"rect\\"><a:avLst/></a:prstGeom></pic:spPr>")
                append("</pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r></w:p>")
            }
            append("<w:sectPr><w:pgSz w:w=\\"$pageWidthTwips\\" w:h=\\"$pageHeightTwips\\"/><w:pgMar w:top=\\"0\\" w:right=\\"0\\" w:bottom=\\"0\\" w:left=\\"0\\" w:header=\\"0\\" w:footer=\\"0\\" w:gutter=\\"0\\"/></w:sectPr>")
        }
        return "<?xml version=\\"1.0\\" encoding=\\"UTF-8\\" standalone=\\"yes\\"?>" +
            "<w:document xmlns:w=\\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\\" xmlns:r=\\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\\" xmlns:wp=\\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\\"><w:body>$body</w:body></w:document>"
    }

    private fun visualRelationshipsXml(count: Int): String =
        "<?xml version=\\"1.0\\" encoding=\\"UTF-8\\" standalone=\\"yes\\"?>" +
            "<Relationships xmlns=\\"http://schemas.openxmlformats.org/package/2006/relationships\\">" +
            (1..count).joinToString("") { index ->
                "<Relationship Id=\\"rId$index\\" Type=\\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\\" Target=\\"media/page-$index.png\\"/>"
            } + "</Relationships>"

    private fun visualContentTypesXml(): String =
        "<?xml version=\\"1.0\\" encoding=\\"UTF-8\\" standalone=\\"yes\\"?>" +
            "<Types xmlns=\\"http://schemas.openxmlformats.org/package/2006/content-types\\">" +
            "<Default Extension=\\"rels\\" ContentType=\\"application/vnd.openxmlformats-package.relationships+xml\\"/>" +
            "<Default Extension=\\"xml\\" ContentType=\\"application/xml\\"/>" +
            "<Default Extension=\\"png\\" ContentType=\\"image/png\\"/>" +
            "<Override PartName=\\"/word/document.xml\\" ContentType=\\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\\"/>" +
            "</Types>"

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
        val firstPage = analysis.pages.first()
        val pageWidthTwips = (firstPage.widthPoints * 20f).toInt().coerceAtLeast(1)
        val pageHeightTwips = (firstPage.heightPoints * 20f).toInt().coerceAtLeast(1)
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
