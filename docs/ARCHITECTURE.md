# Architecture

## Product constraints

- Native Android application.
- PDF parsing, OCR recognition, rendering, and DOCX generation run locally.
- Internet is requested only for an explicit user action to download OCR language models; PDF contents are never uploaded.
- Editable and visual-preservation DOCX outputs.
- Arabic, English, and mixed RTL/LTR documents.
- Maximum practical fidelity with explicit limitations.

## Implemented layers

### Android application
- Storage Access Framework for PDF input and DOCX output.
- Bilingual Arabic and English labels.
- Background processing for PDF analysis, OCR, and exports.
- Separate editable and visual export actions.
- User-triggered OCR language model download.

### PDF analysis
- PDFBox-Android opens local PDF files.
- Page-level text extraction, page dimensions, document title, and text-presence classification.
- Temporary source copies are removed after analysis.

### OCR
- Tesseract 5 through Tesseract4Android.
- Arabic and English tessdata_fast models are downloaded only after the user requests OCR setup.
- Models are stored in app-private storage.
- Scanned pages with no extracted text are rendered and recognized locally.
- The PDF document itself is never sent to the model host.

### Visual export
- Android PdfRenderer renders each PDF page to a PNG.
- PNG page images are embedded in an OOXML DOCX package.
- Page order and aspect ratio are retained; visual output is not independently editable.

### DOCX generation
- Editable output currently writes extracted/OCR text as paragraphs, page breaks, and basic Arabic RTL properties.
- Visual output writes page images and image relationships.
- Unit tests validate package entries and XML structure.

## Current data flow

1. User chooses a PDF through Storage Access Framework.
2. PDFBox extracts selectable text and page geometry.
3. Pages with no selectable text are identified as OCR candidates.
4. User may download Arabic and English OCR models.
5. Tesseract recognizes scanned-page text locally.
6. Editable export writes text and page breaks to DOCX, or visual export renders and embeds page images.
7. User selects a destination through Storage Access Framework.

## Known implementation gaps

- Mixed pages with both selectable text and text embedded in images are not fully reconstructed.
- PDF embedded image extraction and independent image placement are not implemented.
- Tables, columns, headings, lists, charts, equations, and semantic layout reconstruction are not implemented.
- Hybrid editable-plus-image output is not implemented.
- Large visual exports currently retain rendered page images in memory; streaming is a required optimization.
- Automated package validation exists in tests but is not yet a runtime release gate.
- Physical-device testing and office-app inspection remain outstanding.

## Privacy boundary

The only network operation currently implemented is downloading the optional OCR model files from the official Tesseract tessdata_fast repository after the user taps the OCR download button. The selected PDF and generated DOCX remain on-device.

## Memory and reliability requirements

- Move toward one-page-at-a-time rendering and writing.
- Avoid retaining all full-resolution bitmaps.
- Release PdfRenderer pages, bitmaps, and Tesseract native resources promptly.
- Use cancellation-safe temporary files.
- Validate generated DOCX packages before reporting success.
