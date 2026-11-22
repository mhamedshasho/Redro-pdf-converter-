# Architecture

## Product constraints

- Native Android application.
- Fully local processing; no network permission or remote conversion endpoint.
- Editable and visual-preservation DOCX output modes.
- Arabic, English, and mixed RTL/LTR documents.
- Maximum practical fidelity; report uncertainty rather than silently dropping content.

## Layered design

### Android application layer
- Storage Access Framework input and output selection.
- Preview, mode selection, progress, cancellation, and result reporting.
- Lifecycle-aware orchestration.
- Temporary-file lifecycle and storage permissions.

### Conversion coordinator
- Owns one conversion job and its cancellation state.
- Selects page analysis and processing paths.
- Reports page-level progress and recoverable warnings.
- Never reports success before output validation.

### PDF analysis layer
- PDF document inspection and page geometry.
- Positioned text runs, font information, images, and metadata.
- Page classification: text, image-only, or mixed.
- Coordinates normalized to a common page coordinate system.
- Parsing errors represented as typed results.

### OCR and image layer
- Local OCR for Arabic and English.
- Image preprocessing and orientation correction.
- OCR word/line boxes and confidence values.
- OCR runs only for scanned pages or regions without usable text.

### Layout reconstruction layer
- Reading order, paragraphs, columns, headings, lists, and tables.
- Image and diagram placement.
- Mathematical-content detection with image fallback.
- Source coordinates retained through reconstruction.

### DOCX generation layer
- Explicit Office Open XML package writer.
- Paragraphs, runs, tables, styles, relationships, media, and page settings.
- RTL paragraph and run properties.
- Package validation before output is committed.

## Data flow

1. User chooses a PDF through Storage Access Framework.
2. App opens it locally and inspects document/page metadata.
3. Each page is classified and analyzed.
4. Selectable text and embedded images are extracted.
5. OCR is applied only where required.
6. Layout reconstruction creates a page-level intermediate representation.
7. The selected output strategy builds a DOCX package.
8. The package validator checks ZIP entries, relationships, and XML.
9. The user chooses an output destination and the validated DOCX is written.

## Output strategies

- Editable: native Word text, tables, and independent image elements where reconstruction is reliable.
- Visual: each source page is rendered locally and inserted as a page-sized image in DOCX.
- Hybrid: editable elements with image-backed fallbacks for difficult regions. This is an implementation strategy and may later be exposed as an optional mode.

## Offline and privacy boundary

The Android manifest must not request Internet permission. All parsing, OCR, rendering, and DOCX generation happen on-device. Dependencies and OCR language data are packaged with the application; no document data is uploaded.

## Memory strategy

- Process one page or a small bounded batch at a time.
- Avoid retaining full-resolution bitmaps for all pages.
- Stream extracted media into temporary files.
- Release page renderers and bitmaps promptly.
- Use cancellation-safe temporary output and cleanup.
- Validate available storage before large exports where possible.

## Current implementation

The Android foundation currently provides the application shell, bilingual strings, SAF PDF selection, and page-count inspection using Android PdfRenderer. It does not yet perform text extraction, OCR, DOCX generation, or visual export.

## Technical risks

- PDF reading order may differ from content stream order.
- Font encodings and embedded fonts can make text extraction incomplete.
- PDF tables may be visually aligned text rather than semantic tables.
- Arabic shaping and bidirectional punctuation need DOCX-level verification.
- OCR accuracy varies with scan quality, fonts, skew, and language mix.
- Exact editable layout reconstruction is not guaranteed for arbitrary PDFs.
