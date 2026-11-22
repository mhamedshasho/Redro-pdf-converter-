# Architecture

## Components
- Android UI: file selection, destination selection, language, progress, cancellation, and result reporting.
- Conversion coordinator: owns conversion lifecycle and maps core errors to user-facing states.
- PDF text extractor: reads selected PDF content locally and emits page-level text/layout observations.
- Text normalizer: groups extracted text into lines and paragraphs while retaining page boundaries.
- DOCX writer: creates the minimal Office Open XML ZIP package and document XML.
- Validation layer: checks required package parts and well-formed XML.

## Data flow
1. User selects a PDF through Android Storage Access Framework.
2. App reads the selected document through its content URI.
3. Extractor processes one page at a time.
4. Normalizer creates paragraphs and direction metadata.
5. DOCX writer writes a temporary output package.
6. Validator checks package structure.
7. User chooses a destination and the app writes the DOCX there.

Document bytes remain on-device. No document content is sent to a server.

## Technology decisions
- Kotlin and native Android APIs for platform integration.
- Apache PDFBox Android fork for PDF text extraction, subject to dependency and device compatibility tests.
- DOCX generation through a small explicit Open XML package writer rather than a large desktop-oriented office library.
- JUnit for pure conversion-core tests.
- AndroidX instrumentation tests for device integration.

## Important limitations
PDF is a page-description format, not a semantic word-processing format. Text extraction does not guarantee original formatting, columns, tables, or reading order. The initial converter preserves readable text and basic paragraph structure; it does not promise exact visual reproduction.

Scanned PDFs require OCR. OCR is outside the initial milestone and will be evaluated after the text-based pipeline is stable.
