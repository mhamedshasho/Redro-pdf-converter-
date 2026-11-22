package com.redro.pdfconverter

import java.io.File
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

object DocxPackageValidator {
    private const val RELATIONSHIPS_NS = "http://schemas.openxmlformats.org/package/2006/relationships"
    private const val OFFICE_REL_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"

    fun validate(file: File) {
        require(file.isFile && file.length() > 0L) { "DOCX output is empty" }
        ZipFile(file).use { zip ->
            val contentTypes = requireNotNull(zip.getEntry("[Content_Types].xml")) {
                "DOCX content types are missing"
            }
            val rootRelationships = requireNotNull(zip.getEntry("_rels/.rels")) {
                "DOCX package relationships are missing"
            }
            val document = requireNotNull(zip.getEntry("word/document.xml")) {
                "DOCX main document is missing"
            }
            parse(zip.getInputStream(contentTypes).use { it.readBytes() })
            val rootXml = parse(zip.getInputStream(rootRelationships).use { it.readBytes() })
            val rootRelations = rootXml.getElementsByTagNameNS(RELATIONSHIPS_NS, "Relationship")
            require(rootRelations.length > 0) { "DOCX has no package relationship" }
            var hasDocumentRelationship = false
            for (index in 0 until rootRelations.length) {
                val relation = rootRelations.item(index) as org.w3c.dom.Element
                if (relation.getAttribute("Target") == "word/document.xml") {
                    hasDocumentRelationship = true
                }
            }
            require(hasDocumentRelationship) { "DOCX main document relationship is missing" }

            val documentXml = parse(zip.getInputStream(document).use { it.readBytes() })
            val relationshipEntry = zip.getEntry("word/_rels/document.xml.rels")
            if (relationshipEntry != null) {
                val relationXml = parse(zip.getInputStream(relationshipEntry).use { it.readBytes() })
                val relations = relationXml.getElementsByTagNameNS(RELATIONSHIPS_NS, "Relationship")
                val targets = HashMap<String, String>()
                for (index in 0 until relations.length) {
                    val relation = relations.item(index) as org.w3c.dom.Element
                    val id = relation.getAttribute("Id")
                    val target = relation.getAttribute("Target")
                    require(id.isNotBlank() && target.isNotBlank()) { "Invalid DOCX relationship" }
                    targets[id] = target
                    val resolved = if (target.startsWith("/")) target.removePrefix("/")
                    else "word/$target"
                    require(zip.getEntry(resolved) != null) { "Missing DOCX relationship target: $target" }
                }
                val images = documentXml.getElementsByTagNameNS(
                    "http://schemas.openxmlformats.org/drawingml/2006/main",
                    "blip"
                )
                for (index in 0 until images.length) {
                    val image = images.item(index) as org.w3c.dom.Element
                    val relationId = image.getAttributeNS(OFFICE_REL_NS, "embed")
                    require(relationId.isNotBlank() && targets.containsKey(relationId)) {
                        "DOCX image relationship is missing"
                    }
                }
            }
        }
    }

    private fun parse(bytes: ByteArray): org.w3c.dom.Document {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            try { setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) } catch (_: Exception) { }
            try { setFeature("http://xml.org/sax/features/external-general-entities", false) } catch (_: Exception) { }
            try { setFeature("http://xml.org/sax/features/external-parameter-entities", false) } catch (_: Exception) { }
            isExpandEntityReferences = false
        }
        return factory.newDocumentBuilder().parse(bytes.inputStream())
    }
}
