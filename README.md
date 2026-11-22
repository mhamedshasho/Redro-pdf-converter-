# Redro PDF Converter

Native Android PDF-to-Word converter with editable-text and visual-page output modes.

## Product decisions

- Platform: Android.
- Conversion: editable DOCX and visual-preservation DOCX.
- Priority: maximum practical fidelity.
- Languages: Arabic and English, including mixed RTL/LTR text.
- PDF parsing, page rendering, OCR recognition, and DOCX generation run on-device.
- Internet is used only when the user explicitly downloads the optional Arabic and English OCR language models. PDF documents are never uploaded.

## Implemented

- Kotlin Android application with Arabic and English UI resources.
- PDF selection through Android Storage Access Framework.
- PDF page analysis and selectable-text extraction using PDFBox-Android.
- Editable DOCX package generation with XML escaping, page breaks, and basic Arabic RTL properties.
- Visual DOCX export: renders each PDF page to a PNG and embeds the page images in a Word document.
- Optional Tesseract 5 OCR engine with user-triggered downloads of Arabic and English tessdata_fast models.
- OCR is run locally on pages where no selectable text was extracted.
- GitHub Actions unit tests, debug APK build, and APK artifact upload.

## Known limitations

- Editable export currently preserves extracted text and page breaks, not the exact source typography or page layout.
- OCR currently targets pages with no selectable text. Mixed pages containing both selectable text and text embedded in images are not yet fully reconstructed.
- Tables, independent image placement, charts, columns, equations, and semantic layout reconstruction are not implemented.
- Visual export preserves page appearance as page-sized images, so its text is not independently editable.
- Hybrid editable-plus-image reconstruction is not implemented.
- Large PDFs may require substantial memory and processing time. Visual export currently renders pages in memory.

Do not treat these limitations as successful conversion. The app provides separate editable and visual outputs so users can choose between editability and appearance.

## OCR setup

Choose a PDF. If it contains scanned pages, use Download Arabic + English OCR data once while connected to the Internet. The language models are stored in the app's private files directory. Subsequent OCR recognition runs locally; the PDF itself is not sent anywhere.

## Build

Requirements:
- JDK 17
- Android SDK Platform 36
- Gradle 8.13

Run:

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

The Android CI workflow uploads the debug APK as a downloadable artifact after a successful build.

See [Architecture](docs/ARCHITECTURE.md), [Implementation Plan](docs/IMPLEMENTATION_PLAN.md), and [Test Plan](docs/TEST_PLAN.md).
