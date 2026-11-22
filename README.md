# Redro PDF Converter

Native Android PDF-to-Word application focused on high-fidelity, fully on-device conversion.

## Approved product decisions

- Platform: Android.
- Processing: fully offline; PDF contents are never uploaded.
- Conversion modes: editable DOCX and visual-preservation DOCX.
- Quality priority: maximum practical fidelity, with explicit reporting when an element cannot be reconstructed.
- Languages: Arabic and English, including mixed RTL/LTR documents.

## Current implementation status

The `android-foundation` branch contains the first native Android application foundation:
- Kotlin Android application configuration.
- Arabic and English string resources.
- Local PDF selection through Android Storage Access Framework.
- Local PDF opening and page-count inspection through Android PdfRenderer.
- A starter unit test and Android CI workflow.

This is not yet a PDF-to-Word converter. DOCX generation, text extraction, OCR, image/table reconstruction, and visual-page export remain planned implementation milestones. No APK is declared release-ready until CI and device acceptance tests pass.

## Privacy

The application manifest does not request Internet permission. Conversion is designed to run entirely on the device.

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

See [Architecture](docs/ARCHITECTURE.md), [Implementation Plan](docs/IMPLEMENTATION_PLAN.md), and [Test Plan](docs/TEST_PLAN.md).
