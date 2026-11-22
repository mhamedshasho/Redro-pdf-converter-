# Implementation Plan

## Product decisions

- Platform: Android with Kotlin.
- Conversion modes: editable DOCX and visual-preservation DOCX.
- Arabic and English support, including mixed RTL/LTR text.
- PDF contents remain on-device. Internet is used only for an explicit download of OCR language model files.
- Maximum practical fidelity is prioritized over conversion speed.

## Milestone status

### M0 — Requirements and test design
**Status: Completed planning baseline**
- Product decisions and release gates documented.
- Test corpus requirements defined.

### M1 — Android foundation
**Status: Implemented**
- Native Kotlin Android project and bilingual resources.
- Storage Access Framework PDF selection and DOCX save.
- CI unit tests, debug APK build, and artifact upload.

### M2 — PDF analysis and extraction
**Status: Partially implemented**
- PDFBox-Android document loading and page-by-page text extraction.
- Page dimensions and document title extraction.
- Text presence classification.
- Still required: image XObject extraction, positioned text runs, font metadata, mixed-page classification, robust encrypted-file UX.

### M3 — DOCX foundation
**Status: Partially implemented**
- OOXML ZIP package generation.
- Editable text paragraphs, page breaks, XML escaping, basic Arabic RTL properties.
- Visual DOCX package with embedded page PNGs and relationships.
- Still required: runtime package validation, styles, tables, independent images, and per-section page dimensions.

### M4 — OCR
**Status: Initial implementation**
- Tesseract4Android 5 engine.
- User-triggered download of official Arabic and English tessdata_fast models.
- Local OCR for pages without extracted selectable text and supplemental OCR for pages with detected image XObjects.
- Still required: real-device validation, labelled accuracy corpus, progress/cancellation, preprocessing, mixed-page OCR, and confidence reporting.

### M5 — Layout and complex content
**Status: Not implemented**
- Columns, headings, lists, semantic tables, charts, diagrams, equations, and independent image placement.
- Hybrid editable/image-backed reconstruction.

### M6 — Output modes
**Status: Partially implemented**
- Editable text DOCX.
- Visual page-image DOCX preserving page order and aspect ratio.
- Still required: full page-specific section sizing and hybrid reconstruction.

### M7 — UX, performance, and accessibility
**Status: Partially implemented**
- Background PDF analysis, OCR, and export.
- Visual pages are spooled to temporary PNG files instead of retaining all rendered bitmaps in memory.
- Still required: lifecycle-safe jobs, cancellation, bounded file-size handling, detailed progress, accessibility review, and temporary-file recovery.

### M8 — Release verification
**Status: CI foundation implemented**
- Unit tests and debug APK CI.
- APK artifact upload after successful build.
- Still required: latest CI confirmation after OCR/visual changes, physical Android device tests, airplane-mode OCR validation, and office-app inspection.

## Current release blockers

1. Confirm all automated tests and APK build pass after the OCR and visual-export changes.
2. Validate OCR models download and initialize on a real Android device.
3. Verify scanned Arabic and English conversion with labelled samples.
4. Open both DOCX output types in a compatible office application.
5. Test large PDFs, memory use, and error recovery.
6. Implement mixed-page OCR, tables, images, and layout reconstruction before claiming complete fidelity.

## Non-goals

- Bypassing PDF passwords or access restrictions.
- Claiming pixel-perfect editable reconstruction for every PDF.
- Silently discarding elements that cannot be parsed.
