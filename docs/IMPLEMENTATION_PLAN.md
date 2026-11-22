# Implementation Plan

## Product decisions

- Android application written in Kotlin.
- Fully offline processing; no network transfer of document contents.
- Two required output modes: editable DOCX and visual-preservation DOCX.
- Hybrid reconstruction is an optional internal strategy for difficult elements, not a replacement for the two user-facing modes.
- Maximum practical fidelity is prioritized over conversion speed.
- Arabic, English, and mixed RTL/LTR content are required.

## Milestones

### M0 — Requirements and test design
- Completed planning baseline.
- Define a redistributable PDF test corpus and explicit acceptance gates.
- Keep known limitations visible; never silently drop content.

### M1 — Android foundation
- Kotlin Android application and Gradle configuration.
- Arabic and English resources.
- Storage Access Framework PDF picker.
- Open a selected PDF locally and inspect its page count.
- No Internet permission or upload path.
- Add CI build and unit-test workflow.

### M2 — PDF analysis and extraction
- Evaluate and integrate a maintained Android-compatible PDF parsing library.
- Extract text, font metadata, images, page dimensions, and positioned text runs page-by-page.
- Detect pages with no selectable text.
- Preserve source coordinates and page boundaries.
- Classify pages as text, image-only, or mixed.
- Handle encrypted, malformed, and unsupported files with explicit errors.

### M3 — DOCX foundation
- Generate valid Office Open XML packages.
- Write paragraphs, runs, page breaks, styles, relationships, and embedded media.
- Preserve Unicode and XML escaping.
- Implement RTL paragraph and run properties for Arabic.
- Validate ZIP entries and XML before reporting success.

### M4 — OCR
- Integrate local OCR with bundled language data for Arabic and English.
- Evaluate Tesseract and other on-device engines for accuracy, APK size, licensing, and device performance before selecting the production engine.
- Preprocess scans: orientation, deskew, contrast, denoise, and resolution.
- Preserve OCR bounding boxes and confidence values.
- Do not replace extracted text with OCR unless the page or region requires it.

### M5 — Layout and complex content
- Detect columns, paragraphs, headings, lists, and reading order.
- Detect and reconstruct tables with merged cells where reliably identified.
- Extract and position images.
- Preserve charts, diagrams, signatures, and unsupported visual elements as embedded images when semantic reconstruction is not reliable.
- Evaluate mathematical OCR; retain a faithful image fallback for equations that cannot be reconstructed.
- Add hybrid reconstruction for difficult regions.

### M6 — Output modes
- Editable mode: editable paragraphs, tables, and independent images wherever possible.
- Visual mode: render each PDF page at a selected high resolution and insert it as a page-sized image in DOCX.
- Hybrid internal fallback: editable content plus image-backed regions for elements that cannot be reconstructed reliably.
- Keep page order, page dimensions, and orientation.

### M7 — UX, performance, and accessibility
- Preview and conversion settings.
- Progress, cancellation, and recoverable errors.
- Process pages incrementally to bound memory use.
- Clean temporary files safely.
- Explain OCR uncertainty and unsupported elements.

### M8 — Release verification
- Unit tests, package validation, Android build, instrumentation tests, and physical-device tests.
- Inspect generated DOCX in compatible office applications.
- Test airplane-mode operation.
- Record actual CI and device results before publishing an APK.

## Release gates

1. All automated tests pass.
2. Debug APK builds successfully in CI.
3. Generated DOCX packages pass structural and XML validation.
4. Arabic, English, and mixed-script outputs are manually inspected.
5. Scanned PDF OCR is tested with a labelled corpus and measured accuracy.
6. Large-file tests show bounded memory use and no unexplained crashes.
7. Airplane-mode conversion succeeds after installation.
8. No document contents are sent over the network.

## Non-goals

- Bypassing PDF passwords or access restrictions.
- Claiming pixel-perfect editable reconstruction for every PDF.
- Silently discarding elements that cannot be parsed.
